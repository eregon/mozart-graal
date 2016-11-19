package org.mozartoz.bootcompiler
package transform

import scala.collection.mutable.ListBuffer

import oz._
import ast._
import symtab._

object DesugarFunctor extends Transformer with TreeDSL {
  override def transformExpr(expression: Expression) = expression match {
    case functor @ FunctorExpression(name, require, prepare,
        imports, define, exports) if (!require.isEmpty || !prepare.isEmpty) =>
      /* In the boot compiler, require/prepare is no different than
       * import/define.
       * -> merge them
       */

      val mergedRequireImport = require ++ imports

      val mergedPrepareDefine = {
        if (prepare.isEmpty) define
        else if (define.isEmpty) prepare
        else {
          val LocalStatement(prepareDecls, prepareStat) = prepare.get
          val LocalStatement(defineDecls, defineStat) = define.get
          Some(LocalStatement(prepareDecls ++ defineDecls,
              prepareStat ~ defineStat)(Node.extend(prepare.get, define.get)))
        }
      }

      transformExpr {
        FunctorExpression(name, Nil, None, mergedRequireImport,
            mergedPrepareDefine, exports)(functor)
      }

    case functor @ FunctorExpression(name, Nil, None,
        imports, define, exports) =>

      val importsRec = makeImportsRec(functor, imports)
      val exportsRec = makeExportsRec(functor, exports)
      val applyFun = makeApplyFun(functor, define, imports, exports)

      val functorRec = Record(OzAtom("functor"), Seq(
        RecordField(OzAtom("import"), importsRec)(importsRec),
        RecordField(OzAtom("export"), exportsRec)(exportsRec),
        RecordField(OzAtom("apply"), applyFun)(applyFun)))(functor)

      transformExpr(functorRec)

    case _ =>
      super.transformExpr(expression)
  }

  def makeImportsRec(functor: FunctorExpression, imports: Seq[FunctorImport]): Expression = {
    val resultFields = for {
      fimport @ FunctorImport(Variable(module), aliases, location) <- imports
    } yield {
      val modName = module.name

      val typeField = {
        val requiredFeatures =
          for (AliasedFeature(feat, _) <- aliases)
            yield feat.value

        RecordField(OzAtom("type"), OzList(requiredFeatures))(fimport)
      }

      val fromField = {
        val loc = {
          if (location.isDefined) location.get
          else if (!SystemModules.isSystemModule(modName)) modName + ".ozf"
          else "x-oz://system/" + modName + ".ozf"
        }
        RecordField(OzAtom("from"), OzAtom(loc))(fimport)
      }

      val info = Record(OzAtom("info"), Seq(typeField, fromField))(fimport)

      RecordField(OzAtom(modName), info)(info)
    }

    Record(OzAtom("import"), resultFields)(functor)
  }

  def makeExportsRec(functor: FunctorExpression, exports: Seq[FunctorExport]): Expression = {
    val resultFields = for {
      export @ FunctorExport(Constant(feature:OzFeature), _) <- exports
    } yield {
      RecordField(feature, OzAtom("value"))(export)
    }

    Record(OzAtom("export"), resultFields)(Node.posFromSeq(resultFields, functor))
  }

  def makeApplyFun(functor: FunctorExpression,
      define: Option[LocalStatementOrRaw],
      imports: Seq[FunctorImport],
      exports: Seq[FunctorExport]): Expression = {
    val importsParam = Variable.newSynthetic("<Imports>", formal = true)(functor)

    val importedDecls = extractAllImportedDecls(imports)

    val (definedDecls, defineStat) = (define: @unchecked) match {
      case Some(LocalStatement(decls, stat)) => (decls, stat)
      case None => (Nil, SkipStatement()(functor))
    }

    val (utilsDecls, importsDot) = {
      if (program.eagerLoad) {
        val regularDot = Constant(OzBuiltin(builtins.binaryOpToBuiltin(".")))(functor)
        (None, regularDot)
      } else {
        val byNeedDot = Variable.newSynthetic("ByNeedDot")(functor)
        (Some(byNeedDot), byNeedDot)
      }
    }

    val allDecls = importedDecls ++ definedDecls ++ utilsDecls

    FUN(functor, Some(RawVariable(functor.fullName)(functor)), Seq(importsParam)) {
      LOCAL (allDecls:_*) IN {
        val statements = new ListBuffer[Statement]
        def exec(statement: Statement) = statements += statement

        if (!program.eagerLoad)
          exec(importsDot === baseEnvironment("ByNeedDot")(functor))

        for (FunctorImport(module:Variable, aliases, _) <- imports) {
          exec(module === (importsParam dot OzAtom(module.symbol.name)))

          for (AliasedFeature(feature, Some(variable:Variable)) <- aliases) {
            exec(variable === (importsDot callExpr (module, feature) at functor))
          }
        }

        // Of course execute the actual define statements
        exec(defineStat)

        // Now compute the export record
        val exportFields = for {
          export @ FunctorExport(feature, value) <- exports
        } yield {
          RecordField(feature, value)(export)
        }

        val exportRec = Record(OzAtom("export"), exportFields)(functor)

        // Final body
        CompoundStatement(statements)(functor) ~>
        exportRec
      }
    }
  }

  def extractAllImportedDecls(imports: Seq[FunctorImport]) = {
    val result = new ListBuffer[Variable]

    for (FunctorImport(module:Variable, aliases, _) <- imports) {
      result += module

      for (AliasedFeature(_, Some(variable:Variable)) <- aliases)
        result += variable
    }

    result
  }
}
