package org.mozartoz.bootcompiler.util

import org.mozartoz.bootcompiler.ast._

object WrapperUtil {
  def getInnerExpr(expression: Expression): Expression = expression match {
    case ClearVarsExpression(expr, _, _) =>
      expr
    
    case _ => expression
  }
  
  def getInnerStat(statement: Statement): Statement = statement match {
    case ClearVarsStatement(stat, _, _) =>
      stat
    
    case _ => statement
  }
}