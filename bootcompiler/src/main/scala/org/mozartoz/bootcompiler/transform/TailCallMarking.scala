package org.mozartoz.bootcompiler
package transform

import ast._
import oz._
import symtab._

object TailCallMarking extends Transformer {

  val SELF_TAIL_CALLS = System.getProperty("oz.tail.selfcalls", "true") == "true";

  var procExpr: ProcExpression = null

  private def withProc[A](proc: ProcExpression)(f: => A) = {
    val oldProc = procExpr
    procExpr = proc
    try f
    finally procExpr = oldProc
  }
  
  def markTailCalls(statement: Statement): Statement = statement match {
    case call @ CallStatement(callable, args) =>
      if (SELF_TAIL_CALLS && Some(callable) == procExpr.name) {
        call.kind = CallKind.SelfTail
      } else {
        call.kind = CallKind.Tail
      }
      call

    case CompoundStatement(stats) =>
      val last = markTailCalls(stats.last)
      treeCopy.CompoundStatement(statement, stats.dropRight(1) :+ last)
      
    case IfStatement(condition, trueStatement, falseStatement) =>
      treeCopy.IfStatement(statement, condition,
          markTailCalls(trueStatement),
          markTailCalls(falseStatement))
          
    case MatchStatement(value, clauses, elseStatement) =>
      treeCopy.MatchStatement(statement, value,
          clauses.map(c => markMatchStatementClause(c)),
          markTailCalls(elseStatement))
      
    case LocalStatement(declarations, stat) =>
      treeCopy.LocalStatement(statement, declarations, markTailCalls(stat))
      
    case _ => statement
  }
  
  def markMatchStatementClause(clause: MatchStatementClause) =
    treeCopy.MatchStatementClause(clause, clause.pattern, clause.guard, markTailCalls(clause.body))
  
  override def transformExpr(expression: Expression) = expression match {
    case proc @ ProcExpression(name, args, body, flags) =>
      withProc(proc) {
        treeCopy.ProcExpression(expression, name, args, markTailCalls(transformStat(body)), flags)
      }
    
    case _ => super.transformExpr(expression)
  }
}
