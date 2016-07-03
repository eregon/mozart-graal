package org.mozartoz.bootcompiler
package transform

import ast._
import oz._
import symtab._

object Simplify extends Transformer with TreeDSL {
  override def transformStat(statement: Statement) = statement match {
    case matchStat @ MatchStatement(value, _, _) if !value.isInstanceOf[Variable] =>
      transformStat {
        assignMatchValue(matchStat)
      }

    case _ =>
      super.transformStat(statement)
  }

  override def transformExpr(expression: Expression) = expression match {
    case matchExpr @ MatchExpression(value, _, _) if !value.isInstanceOf[Variable] =>
      transformExpr {
        assignMatchValue(matchExpr)
      }

    case CallExpression(callable, args) =>
      transformExpr {
        expressionWithTemp { result =>
          val newArgs = putVarInArgs(args, result)
          treeCopy.CallStatement(expression, callable, newArgs) ~> result
        }
      }

    case _ =>
      super.transformExpr(expression)
  }

  private def assignMatchValue(matchStat: MatchStatement) = {
    matchStat match {
      case MatchStatement(value, clauses, elseStat) =>
        statementWithTemp { temp =>
          transformStat(temp === value) ~
            treeCopy.MatchStatement(matchStat, temp, clauses, elseStat)
        }
    }
  }

  private def assignMatchValue(matchExpr: MatchExpression) = {
    matchExpr match {
      case MatchExpression(value, clauses, elseStat) =>
        expressionWithTemp { temp =>
          transformStat(temp === value) ~>
            treeCopy.MatchExpression(matchExpr, temp, clauses, elseStat)
        }
    }
  }

  private def putVarInArgs(args: Seq[Expression], v: Variable) = {
    var nestingMarkerFound = false

    def replaceNestingMarkerIn(expr: Expression): Expression = expr match {
      case NestingMarker() =>
        if (nestingMarkerFound) {
          program.reportError("Duplicate nesting marker", expr)
          treeCopy.UnboundExpression(expr)
        } else {
          nestingMarkerFound = true
          v
        }

      case Record(label, fields) =>
        val newFields = for {
          field @ RecordField(feature, value) <- fields
        } yield {
          treeCopy.RecordField(field, feature, replaceNestingMarkerIn(value))
        }
        treeCopy.Record(expr, label, newFields)

      case _ =>
        expr
    }

    val newArgs = args map replaceNestingMarkerIn

    if (nestingMarkerFound) newArgs
    else newArgs :+ v
  }
}
