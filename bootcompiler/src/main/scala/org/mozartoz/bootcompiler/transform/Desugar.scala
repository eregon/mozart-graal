package org.mozartoz.bootcompiler
package transform

import ast._
import oz._
import symtab._
import ast.Node.Pos

object Desugar extends Transformer with TreeDSL {

  var procName: VariableOrRaw = RawVariable("")(Node.noPos)

  private def withProcName[A](name: Option[VariableOrRaw])(f: => A) = {
    val oldName = procName
    procName = name.get
    try f
    finally procName = oldName
  }

  private def genProcName(kind: String, pos: Pos) =
    Some(RawVariable(kind + " in " + procName.name)(pos))

  override def transformStat(statement: Statement) = statement match {
    case assign @ BinaryOpStatement(lhs, ":=", rhs) =>
      builtins.catAssign call (transformExpr(lhs), transformExpr(rhs)) at statement

    case DotAssignStatement(left, center, right) =>
      builtins.dotAssign call (transformExpr(left), transformExpr(center),
          transformExpr(right)) at statement

    case ifStat @ IfStatement(cond, trueStat, falseStat:NoElseStatement) =>
      transformStat {
        treeCopy.IfStatement(ifStat, cond, trueStat,
            treeCopy.SkipStatement(falseStat))
      }

    case thread @ ThreadStatement(body) =>
      val proc = PROC(thread, genProcName("thread", thread), Nil) {
        transformStat(body)
      }

      builtins.createThread call (proc) at thread

    case lockStat @ LockStatement(lock, body) =>
      val proc = PROC(lockStat, genProcName("lock", lock), Nil) {
        transformStat(body)
      }

      baseEnvironment("LockIn")(lockStat) call (transformExpr(lock), proc) at lockStat

    case TryFinallyStatement(body, finallyBody) =>
      transformStat {
        statementWithTemp(statement) { tempX =>
          val tempY = Variable.newSynthetic(capture = true)(statement)

          (LOCAL (tempY) IN {
            (tempX === TryExpression(body ~> UnitVal(),
                tempY, Tuple(Constant(OzAtom("ex"))(body), Seq(tempY)))(body))
          }) ~
          finallyBody ~
          (IF (tempX =?= UnitVal()) THEN {
            SkipStatement()(finallyBody)
          } ELSE {
            RaiseStatement(tempX dot OzInt(1))(finallyBody)
          })
        }
      }

    case _ =>
      super.transformStat(statement)
  }

  override def transformExpr(expression: Expression) = expression match {
    case fun @ FunExpression(name, args, body, flags) =>
      val result = Variable.newSynthetic("<Result>", formal = true)(fun)

      val isLazy = flags contains "lazy"
      val newFlags =
        if (isLazy) flags filterNot "lazy".==
        else flags

      PROC(fun, name, args :+ result, newFlags) {
        withProcName(name) {
          val newBody =  transformExpr(body)
          if (isLazy) {
            val result2 = Variable.newSynthetic("<Result2>")(fun)
            transformStat {
              LOCAL (result2) IN {
                THREAD(fun) {
                  (builtins.waitNeeded call (result2) at fun) ~
                  exprToBindStatement(result2, newBody)
                } ~
                (builtins.unaryOpToBuiltin("!!") call (result2, result) at fun)
              }
            }
          } else {
            exprToBindStatement(result, newBody)
          }
        }
      }

    case proc @ ProcExpression(name, args, body, flags) =>
      withProcName(name) {
        super.transformExpr(proc)
      }

    case thread @ ThreadExpression(body) =>
      expressionWithTemp(thread) { temp =>
        transformStat(
          THREAD(thread)(temp === body)
        ) ~> temp
      }

    case lockExpr @ LockExpression(lock, body) =>
      expressionWithTemp(lockExpr) { temp =>
        transformStat(
          LockStatement(lock, temp === body)(lockExpr)
        ) ~> temp
      }

    case TryFinallyExpression(body, finallyBody) =>
      transformExpr {
        expressionWithTemp(expression) { tempX =>
          val tempY = Variable.newSynthetic(capture = true)(expression)

          (LOCAL (tempY) IN {
            (tempX === TryExpression(
                Tuple(Constant(OzAtom("ok"))(body), Seq(body)),
                tempY, Tuple(Constant(OzAtom("ex"))(body), Seq(tempY)))(body))
          }) ~
          finallyBody ~>
          (IF ((builtins.label callExpr (tempX) at finallyBody) =?= OzAtom("ok")) THEN {
            tempX dot OzInt(1)
          } ELSE {
            RaiseExpression(tempX dot OzInt(1))(finallyBody)
          })
        }
      }

    case DotAssignExpression(left, center, right) =>
      transformExpr(builtins.dotExchange callExpr (left, center, right) at expression)

    case UnaryOp(op, arg) =>
      transformExpr(builtins.unaryOpToBuiltin(op) callExpr (arg) at expression)

    case BinaryOp(module @ Variable(sym), ".", rhs) if !program.eagerLoad && sym.isImport =>
      transformExpr(
        baseEnvironment("ByNeedDot")(expression) callExpr (module, rhs) at expression)

    case Record(label, fields) =>
      val fieldsNoAuto = fillAutoFeatures(fields)
      val newRecord = treeCopy.Record(expression, label, fieldsNoAuto)
      super.transformExpr(newRecord)

    case OpenRecordPattern(label, fields) =>
      val fieldsNoAuto = fillAutoFeatures(fields)
      val newPattern = treeCopy.OpenRecordPattern(
          expression, label, fieldsNoAuto)
      super.transformExpr(newPattern)

    case _ =>
      super.transformExpr(expression)
  }
  
  def exprToBindStatement(result: Variable, expr: Expression): Statement = expr match {
    case StatAndExpression(statement, expression) =>
      treeCopy.CompoundStatement(expr, Seq(statement, exprToBindStatement(result, expression)))
      
    case ShortCircuitBinaryOp(left, "andthen", right) =>
      treeCopy.IfStatement(expr, left, exprToBindStatement(result, right), result === False())
      
    case ShortCircuitBinaryOp(left, "orelse", right) =>
      treeCopy.IfStatement(expr, left, result === True(), exprToBindStatement(result, right))
      
    case LocalExpression(declarations, expression) =>
      treeCopy.LocalStatement(expr, declarations, exprToBindStatement(result, expression))
      
    case IfExpression(condition, trueExpression, falseExpression) =>
      treeCopy.IfStatement(expr, condition, exprToBindStatement(result, trueExpression), exprToBindStatement(result, falseExpression))
      
    case NoElseExpression() =>
      treeCopy.NoElseStatement(expr)
      
    case CallExpression(callable, args) =>
      treeCopy.CallStatement(expr, callable, Unnester.putVarInArgs(args, result))
      
    case MatchExpression(value, clauses, elseExpression) =>
      treeCopy.MatchStatement(expr, value,
          clauses map(c => matchExpressionClauseToBindStatement(result, c)),
          exprToBindStatement(result, elseExpression))

    case Record(label, fields) =>
      var tailExpression: Option[(Variable, Expression)] = None
      def defer(expr: Expression) = {
        val newVar = Variable.newSynthetic("RecordTailCall")(expr)
        tailExpression = Some((newVar, expr))
        newVar
      }

      val newFields = fields.reverse.map { field =>
        if (tailExpression.isEmpty && hasSideEffects(field))
          treeCopy.RecordField(field, field.feature, defer(field.value))
        else
          field
      }.reverse
      
      if (tailExpression.isEmpty)
        return result === expr
      val (newVar, value) = tailExpression.get
      LOCAL (newVar) IN treeCopy.CompoundStatement(expr, Seq(
          result === Record(label, newFields)(expr),
          exprToBindStatement(newVar, value)))
      
    case _ =>
      treeCopy.BindStatement(expr, result, expr)
  }
  
  def matchExpressionClauseToBindStatement(result: Variable, clause: MatchExpressionClause
      ): MatchStatementClause = clause match {
    case MatchExpressionClause(pattern, guard, body) =>
      treeCopy.MatchStatementClause(clause, pattern, guard, exprToBindStatement(result, body))
  }

  def hasSideEffects(node: Node): Boolean = node match {
    case Variable(sym)               => false
    case Constant(value)             => false
    case RecordField(feature, value) => hasSideEffects(feature) || hasSideEffects(value)
    case Record(label, fields)       => hasSideEffects(label) || fields.exists(hasSideEffects(_))
    case _                           => true
  }

  private def fillAutoFeatures(fields: Seq[RecordField]) = {
    if (fields forall (!_.hasAutoFeature)) {
      // Trivial case: all features are non-auto
      fields
    } else if (fields forall (_.hasAutoFeature)) {
      // Next-to-trivial case: all features are auto
      for ((field, index) <- fields.zipWithIndex)
        yield treeCopy.RecordField(field, OzInt(index+1), field.value)
    } else {
      // Complex case: mix of auto and non-auto features

      // Collect used integer features
      val usedFeatures = (for {
        RecordField(Constant(OzInt(feature)), _) <- fields
      } yield feature).toSet

      // Actual filling
      var nextFeature: Long = 1

      for (field @ RecordField(feature, value) <- fields) yield {
        if (field.hasAutoFeature) {
          while (usedFeatures contains nextFeature)
            nextFeature += 1
          nextFeature += 1

          val newFeature = treeCopy.Constant(feature, OzInt(nextFeature-1))
          treeCopy.RecordField(field, newFeature, value)
        } else {
          field
        }
      }
    }
  }
}
