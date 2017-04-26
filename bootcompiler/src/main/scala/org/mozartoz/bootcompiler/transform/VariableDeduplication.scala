package org.mozartoz.bootcompiler.transform

import org.mozartoz.bootcompiler.ast._

object VariableDeduplication extends Transformer with TreeDSL {
  override def transformExpr(expression: Expression) = expression match {
    case v @ Variable(sym) =>
      treeCopy.Variable(v, sym)
      
    case _ => super.transformExpr(expression)
  }
}