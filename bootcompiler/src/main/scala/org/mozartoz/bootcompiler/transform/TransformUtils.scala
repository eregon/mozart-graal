package org.mozartoz.bootcompiler
package transform

import ast._
import ast.Node.Pos

trait TransformUtils {
  def statementsToStatement(statements: Seq[Statement])(pos: Pos) = statements match {
    case Seq() => SkipStatement()(pos)
    case Seq(stat) => stat
    case _ => CompoundStatement(statements)(Node.extend(statements))
  }

  def statsAndStatToStat(statements: Seq[Statement], statement: Statement) =
    if (statements.isEmpty) statement
    else CompoundStatement(statements :+ statement)(Node.extend(statements(0), statement))

  def statsAndExprToExpr(statements: Seq[Statement], expression: Expression) =
    if (statements.isEmpty) expression
    else StatAndExpression(statementsToStatement(statements)(expression), expression)(Node.extend(statements(0), expression))
}
