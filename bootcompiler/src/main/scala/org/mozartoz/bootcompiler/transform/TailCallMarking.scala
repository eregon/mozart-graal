package org.mozartoz.bootcompiler
package transform

import ast._
import oz._
import symtab._

object TailCallMarking extends Transformer {
  def markTailCalls(name: Symbol, statement: Statement): Statement = statement match {
    case call @ CallStatement(callable @ Variable(sym), args) =>
      treeCopy.TailMarkerStatement(statement, call)

    case CompoundStatement(stats) =>
      val last = markTailCalls(name, stats.last)
      treeCopy.CompoundStatement(statement, stats.dropRight(1) :+ last)
      
    case IfStatement(condition, trueExpression, falseExpression) =>
      treeCopy.IfStatement(statement, condition,
          markTailCalls(name, trueExpression),
          markTailCalls(name, falseExpression))
          
    case MatchStatement(value, clauses, elseStatement) =>
      treeCopy.MatchStatement(statement, value,
          clauses.map(c => markMatchStatementClause(name, c)),
          markTailCalls(name, elseStatement))
      
    case LocalStatement(declarations, stat) =>
      treeCopy.LocalStatement(statement, declarations, markTailCalls(name, stat))
      
    case _ => statement
  }
  
  def markMatchStatementClause(name: Symbol, clause: MatchStatementClause) =
    treeCopy.MatchStatementClause(clause, clause.pattern, clause.guard, markTailCalls(name, clause.body))
  
  override def transformExpr(expression: Expression) = expression match {
    case ProcExpression(name @ Some(Variable(sym)), args, body, flags) =>
      treeCopy.ProcExpression(expression, name, args, markTailCalls(sym, transformStat(body)), flags)
    
    case _ => super.transformExpr(expression)
  }
}
