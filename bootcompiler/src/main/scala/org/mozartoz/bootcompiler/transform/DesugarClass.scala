package org.mozartoz.bootcompiler
package transform

import scala.collection.mutable.ListBuffer

import ast._
import oz._
import symtab._

/** Desugars class definitions */
object DesugarClass extends Transformer with TreeDSL {
  case class MethodInfo(symbol: Symbol, label: Expression, proc: Expression)

  var selfSymbol: Option[Symbol] = None

  private def withSelf[A](newSelf: Symbol)(f: => A) = {
    val oldSelf = selfSymbol
    selfSymbol = Some(newSelf)
    try f
    finally selfSymbol = oldSelf
  }

  override def transformStat(statement: Statement) = statement match {
    case LockObjectStatement(body) if selfSymbol.isDefined =>
      val getObjLock = baseEnvironment("OoExtensions")(statement) dot OzAtom("getObjLock").at(statement)
      val lock = getObjLock callExpr (Variable(selfSymbol.get)(statement)) at statement
      transformStat {
        LockStatement(lock, body)(statement)
      }

    case LockObjectStatement(body) if !selfSymbol.isDefined =>
      program.reportError(
          "Illegal use of lock-object outside of class definition",
          statement)

      // Some dummy statement
      transformStat(
          LockStatement(UnboundExpression()(statement), body)(statement))

    case BinaryOpStatement(lhs, "<-", rhs) if selfSymbol.isDefined =>
      transformStat {
        builtins.attrPut(statement) call (Variable(selfSymbol.get)(statement), lhs, rhs) at statement
      }

    case BinaryOpStatement(lhs, "<-", rhs) if !selfSymbol.isDefined =>
      program.reportError("Illegal use of <- outside of class definition",
          statement)

      // Some dummy statement
      transformStat(BinaryOpStatement(lhs, ":=", rhs)(statement))

    case BinaryOpStatement(lhs, ":=", rhs) if selfSymbol.isDefined =>
      transformStat {
        builtins.catAssignOO(statement) call (Variable(selfSymbol.get)(statement), lhs, rhs) at statement
      }

    case BinaryOpStatement(lhs, ",", rhs) if selfSymbol.isDefined =>
      transformStat {
        val applyProc = (lhs dot baseEnvironment("`ooFallback`")(statement) dot OzAtom("apply").at(statement))
        applyProc call (rhs, Self()(statement), lhs) at statement
      }

    case BinaryOpStatement(lhs, ",", rhs) if !selfSymbol.isDefined =>
      program.reportError("Illegal use of , outside of class definition",
          statement)

      // Some dummy statement
      transformStat(BinaryOpStatement(lhs, ":=", rhs)(statement))

    case _ =>
      super.transformStat(statement)
  }

  override def transformExpr(expression: Expression) = expression match {
    case LockObjectExpression(body) if selfSymbol.isDefined =>
      val getObjLock = baseEnvironment("OoExtensions")(expression) dot OzAtom("getObjLock").at(expression)
      val lock = getObjLock callExpr (Variable(selfSymbol.get)(expression)) at expression
      transformExpr {
        LockExpression(lock, body)(expression)
      }

    case LockObjectExpression(body) if !selfSymbol.isDefined =>
      program.reportError(
          "Illegal use of lock-object outside of class definition",
          expression)

      // Some dummy expression
      transformExpr(
          LockExpression(UnboundExpression()(expression), body)(expression))

    case clazz @ ClassExpression(name, parents, features, attributes,
        properties, methods) =>
      val methodsInfo = makeMethods(name, methods)

      transformExpr {
        LOCAL ((methodsInfo map (info => Variable(info.symbol)(info.label))):_*) IN {
          val createMethodProcs = CompoundStatement(for {
            MethodInfo(symbol, _, proc) <- methodsInfo
          } yield {
            Variable(symbol)(proc) === proc
          })(clazz)

          val newName = Constant(OzAtom(name))(clazz)
          val newParents = transformParents(clazz, parents)
          val newFeatures = transformFeatOrAttr(clazz, "feat", features)
          val newAttributes = transformFeatOrAttr(clazz, "attr", attributes)
          val newProperties = transformProperties(clazz, properties)
          val newMethods = transformMethods(clazz, methodsInfo)

          val newFullClass = baseEnvironment("OoExtensions")(clazz) dot OzAtom("class").at(clazz)

          createMethodProcs ~>
          newFullClass callExpr (newParents, newMethods, newAttributes,
              newFeatures, newProperties, newName) at clazz
        }
      }

    case Self() if selfSymbol.isDefined =>
      treeCopy.Variable(expression, selfSymbol.get)

    case Self() if !selfSymbol.isDefined =>
      program.reportError("Illegal use of self outside of class definition",
          expression)

      // Some dummy expression
      treeCopy.Constant(expression, OzAtom("self"))

    case UnaryOp("@", rhs) if selfSymbol.isDefined =>
      transformExpr {
        builtins.catAccessOO(expression) callExpr (Variable(selfSymbol.get)(expression), rhs) at expression
      }

    case BinaryOp(lhs, "<-", rhs) if selfSymbol.isDefined =>
      transformExpr {
        builtins.attrExchangeFun(expression) callExpr (Variable(selfSymbol.get)(expression), lhs, rhs) at expression
      }

    case BinaryOp(lhs, "<-", rhs) if !selfSymbol.isDefined =>
      program.reportError("Illegal use of <- outside of class definition",
          expression)

      transformExpr {
        BinaryOp(lhs, ":=", rhs)(expression)
      }

    case BinaryOp(lhs, ":=", rhs) if selfSymbol.isDefined =>
      transformExpr {
        builtins.catExchangeOO(expression) callExpr (Variable(selfSymbol.get)(expression), lhs, rhs) at expression
      }

    case BinaryOp(lhs, ",", rhs) if selfSymbol.isDefined =>
      transformExpr {
        val applyProc = (lhs dot baseEnvironment("`ooFallback`")(expression) dot OzAtom("apply").at(expression))
        applyProc callExpr (rhs, Self()(expression), lhs) at expression
      }

    case BinaryOp(lhs, ",", rhs) if !selfSymbol.isDefined =>
      program.reportError("Illegal use of , outside of class definition",
          expression)

      // Some dummy expression
      transformExpr(BinaryOp(lhs, ":=", rhs)(expression))

    case _ =>
      super.transformExpr(expression)
  }

  def transformParents(clazz: ClassExpression, parents: Seq[Expression]): Expression = {
    exprListToListExpr(parents)(clazz)
  }

  def transformFeatOrAttr(clazz: ClassExpression, label: String,
      featOrAttrs: Seq[FeatOrAttr]): Expression = {
    val specs = for {
      featOrAttr @ FeatOrAttr(name, value) <- featOrAttrs
    } yield {
      val newValue = value getOrElse baseEnvironment("`ooFreeFlag`")(featOrAttr)
      treeCopy.RecordField(featOrAttr, name, newValue)
    }

    Record(Constant(OzAtom(label))(clazz), specs)(Node.posFromSeq(specs, clazz))
  }

  def transformProperties(clazz: ClassExpression, properties: Seq[Expression]) = {
    exprListToListExpr(properties)(clazz)
  }

  def transformMethods(clazz: ClassExpression, methods: Seq[MethodInfo]): Expression = {
    val newMethods = for {
      MethodInfo(symbol, name, label) <- methods
    } yield {
      sharp(Seq(name, Variable(symbol)(label)))(label)
    }

    sharp(newMethods)(clazz)
  }

  def makeMethods(className: String,
      methods: Seq[MethodDef]): Seq[MethodInfo] = {
    for {
      method @ MethodDef(MethodHeader(name, _, _), _, _) <- methods
    } yield {
      val procName = "%s,%s" format (className, nameOf(name))
      val symbol = new Symbol(procName)
      val proc = makeProcForMethod(symbol, method)

      MethodInfo(symbol, name, proc)
    }
  }

  def nameOf(expr: Expression) = expr match {
    case Variable(sym) => sym.name
    case _             => expr.toString()
  }

  def makeProcForMethod(name: Symbol, method: MethodDef): Expression = {
    val MethodDef(MethodHeader(_, params, open), messageVar, body) = method

    val selfParam = Variable(new Symbol("self", formal = true))(method.header)
    val msgParam = Variable(new Symbol("<M>", formal = true))(method.header)

    val paramVars = new ListBuffer[Variable]
    var resultVar: Option[Variable] = None

    val fetchParamStats = new ListBuffer[Statement]
    var nextFeature: Long = 1

    for (MethodParam(feature, name, default) <- params) {
      val actualFeature = feature match {
        case AutoFeature() =>
          val actual = treeCopy.Constant(feature, OzInt(nextFeature))
          nextFeature += 1
          actual

        case _ =>
          feature
      }

      val paramVar = (name: @unchecked) match {
        case paramVar:Variable =>
          paramVars += paramVar
          Some(paramVar)

        case NestingMarker() =>
          if (resultVar.isDefined) {
            program.reportError("Duplicate nesting marker", name)
            None
          } else {
            val resVar = Variable.newSynthetic("<Result>")(name)
            resultVar = Some(resVar)
            paramVars += resVar
            Some(resVar)
          }

        case UnboundExpression() =>
          None
      }

      if (paramVar.isDefined) {
        val getIt = (paramVar.get === (msgParam dot actualFeature))

        fetchParamStats += {
          if (default.isEmpty) getIt
          else {
            IF (builtins.hasFeature(msgParam) callExpr (msgParam, actualFeature) at msgParam) THEN {
              getIt
            } ELSE {
              paramVar.get === default.get
            }
          }
        }
      }
    }

    if (messageVar.isDefined) {
      paramVars += messageVar.get.asInstanceOf[Variable]
      fetchParamStats += {
        messageVar.get === msgParam
      }
    }

    PROC (method, Some(Variable(name)(method)), Seq(selfParam, msgParam)) {
      LOCAL (paramVars:_*) IN {
        withSelf(selfParam.symbol) {
          transformStat {
            CompoundStatement(fetchParamStats)(method) ~ {
              if (resultVar.isDefined) {
                resultVar.get === body.asInstanceOf[Expression]
              } else {
                body.asInstanceOf[Statement]
              }
            }
          }
        }
      }
    }
  }
}
