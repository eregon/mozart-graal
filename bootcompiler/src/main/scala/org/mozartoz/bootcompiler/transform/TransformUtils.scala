package org.mozartoz.bootcompiler
package transform

import ast._

trait TransformUtils {
  def statementsToStatement(statements: Seq[Statement]) = statements match {
    case Seq() => SkipStatement()
    case Seq(stat) => stat
    case _ => CompoundStatement(statements)
  }

  def statsAndStatToStat(statements: Seq[Statement], statement: Statement) =
    if (statements.isEmpty) statement
    else CompoundStatement(statements :+ statement)

  def statsAndExprToExpr(statements: Seq[Statement], expression: Expression) =
    if (statements.isEmpty) expression
    else StatAndExpression(statementsToStatement(statements), expression)
}
