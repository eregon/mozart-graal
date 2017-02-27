package org.mozartoz.bootcompiler
package transform

import ast._
import oz._
import symtab._
import scala.collection._
import com.sun.javafx.fxml.expression.BinaryExpression

sealed class VarState(val onStack: Boolean, val locked: Boolean) {
}
object Declared      extends VarState(false, false)
object Captured      extends VarState(false, false)
object BoundFirst    extends VarState(true, true)
object ToInstantiate extends VarState(false, true)

case class VarAndState(v: Variable, state: VarState)

/**
 * Looks for declarations and the usages of those declarations.
 * The Transform methods detect local statements and add declared variables to the environment
 * The Walk methods are collecting their usages and tagging them
 * 
 * If a variable is bound after its declaration, we can shortcut it
 * 
 * When a proc is declared, it is difficult to track where it will end up. So if a call occurs,
 * by precaution, all captured variable that were not bound should be instantiated
 * 	This means that any A = {IntToFloat 3} will instantiate because it results in {IntToFloat 3 A}
 *  that could potentially call end up calling A
 *  
 * When having two branches, if a variable does not end in the same state, it it better to instantiate it
 * 
 * If a variable is encountered, it is always put into the modified set (if any) except if it was already
 * in a BoundFirst or a ToInstantiate state.
 * 
 * In try-catch or andthen/orelse nodes, variables that have not been bound yet end up in ToInstantiate state
 *  
 * The basic idea is that if no usage appears before binding, no need to create a variable.
 */
class OnStackMarking(val declVars: mutable.HashMap[Symbol, VarAndState],
    val modified: Option[mutable.Set[Variable]]) extends Transformer with Walker {
  def this(declVars: mutable.HashMap[Symbol, VarAndState]) = this(declVars, None)
  def this() = this(mutable.HashMap())
  
  def tagVariable(v: Variable, state: VarState) = {
    declVars.put(v.symbol, VarAndState(v, state))
    modified.foreach { set => set.add(v) }
  }
  
  /**
   * Looks for affected variables using a sandboxed marker
   */
  def affectedVariables(affectedSet: mutable.Set[Variable], marking: (OnStackMarking)=>Unit): mutable.Set[Variable] = {
    val affected = if (affectedSet == null) mutable.Set[Variable]() else affectedSet
    val marker = new OnStackMarking(declVars.clone(), Some(affected)) {
      // do nothing when encountering a "bond on stack" node. Bound and Used variables will get the same treatment
      override def onStackBind(bind: BindCommon, v: Variable) = {}
    }
    marking(marker)
    affected
  }
  
  /**
   * Runs the algorithm in sandboxed branches and merges the result in current marker
   */
  def conjugate[A](collector: mutable.HashMap[Symbol, VarAndState], branches: ((OnStackMarking)=>Unit)*): Unit = {
    val collectorClone = collector.clone()
    val modified = mutable.Set[Variable]()
    val bindNodes = mutable.HashMap[Symbol, Seq[BindCommon]]()
    for ((branch, i) <- branches.zipWithIndex) {
      val current = if (i == 0) collector else collectorClone.clone() 
    	val marker = new OnStackMarking(current, Some(modified)) {
        // In the sandboxed environment, we collect "bind on stack" nodes to defer decision
    	  override def onStackBind(bind: BindCommon, v: Variable) =
    	    bindNodes.put(v.symbol, bindNodes.getOrElse(v.symbol, Seq()) :+ bind)
    	}
      branch(marker)
      if (i != 0) {
        for (v <- modified) {
          val collectorVS = collector.get(v.symbol).get
          val currentVS = current.get(v.symbol).get
          if (collectorVS.state != currentVS.state && collectorVS.state != ToInstantiate) {
            collector.put(v.symbol, VarAndState(collectorVS.v, ToInstantiate))
          }
        }
      }
    }
    this.modified.foreach { _ ++= modified }
    for ((sym, bindNodes) <- bindNodes) {
      val vas = collector.get(sym).get
      if (vas.state == BoundFirst) {
        for (bind <- bindNodes) {
          // Now, we confirm the fact those bindNodes can work on the stack
          onStackBind(bind, vas.v)
        }
      }
    }
  }
  
  /**
   * The toplevel OnStack markers modify the bind statements and expressions
   */
  def onStackBind(bind: BindCommon, v: Variable) = {
    bind.onStack = true
  }
  
  def bindBindCommon(bind: BindCommon, sym: Symbol, right: Expression) = {
		  walkExpr(right)
		  // If we bind and it has not been used, put it on stack
		  for (decl <- declVars.get(sym) if !decl.state.locked) {
		    tagVariable(decl.v, BoundFirst)
		    onStackBind(bind, decl.v)
		  }
  }
  
  override def walkStat(statement: Statement): Unit = statement match {
    case bind @ BindStatement(left @ Variable(sym), right) =>
      bindBindCommon(bind, sym, right)
      
    case CallStatement(callable, args) =>
      walkExpr(callable)
      args map walkExpr
      for ((sym, decl) <- declVars if decl.state == Captured) {
        tagVariable(decl.v, ToInstantiate)
      }
      
    case IfStatement(cond, trueStat, elseStat) =>
      walkExpr(cond)
      conjugate(declVars, _.walkStat(trueStat), _.walkStat(elseStat))
      
    case MatchStatement(value, clauses, elseStatement) =>
      walkExpr(value)
      conjugate(declVars, (clauses.map {
        clause =>
          m: OnStackMarking =>
           clause.guard foreach {g => m.walkExpr(g)}
           m.walkStat(clause.body)
      } :+ {m: OnStackMarking => m.walkStat(elseStatement)}) :_*)
      
    case TryStatement(body, exceptionVar, catchBody) =>
      affectedVariables(null, { m =>
        m.walkStat(body)
        m.walkStat(catchBody)
      }) foreach (tagVariable(_, ToInstantiate))
      
    case _ => super.walkStat(statement)
  }
  
  override def walkExpr(expression: Expression): Unit = expression match {
    case bind @ BindExpression(left @ Variable(sym), right) =>
      bindBindCommon(bind, sym, right)
      
    case Variable(sym) =>
      for (decl <- declVars.get(sym) if !decl.state.locked) {
        tagVariable(decl.v, ToInstantiate)
      }
      
    case ProcExpression(name, args, body, flags) =>
      affectedVariables(null, { m =>
        m.walkStat(body)
      }) foreach (tagVariable(_, Captured))
      
    case IfExpression(cond, trueExpr, elseExpr) =>
      walkExpr(cond)
      conjugate(declVars, _.walkExpr(trueExpr), _.walkExpr(elseExpr))
      
    case MatchExpression(value, clauses, elseExpression) =>
      walkExpr(value)
      conjugate(declVars, (clauses.map { clause =>
        m: OnStackMarking =>
         clause.guard foreach {g => m.walkExpr(g)}
         m.walkExpr(clause.body)
      } :+ {m: OnStackMarking => m.walkExpr(elseExpression)}):_*)
      
    case TryExpression(body, exceptionVar, catchBody) =>
      affectedVariables(null, { m =>
        m.walkExpr(body)
        m.walkExpr(catchBody)
      }) foreach (tagVariable(_, ToInstantiate))
      
    case ShortCircuitBinaryOp(left, op, right) =>
      affectedVariables(null, { m =>
        m.walkExpr(left)
        m.walkExpr(right)
      }) foreach (tagVariable(_, ToInstantiate))
      
    case _ => super.walkExpr(expression)
  }
  
  def inspectLocalCommon(decls: Seq[Variable], statOrExpr: StatOrExpr) = {
      for (decl <- decls if !decl.symbol.isCapture) {
        declVars.put(decl.symbol, VarAndState(decl, Declared))
      }
      statOrExpr match {
        case stat: Statement  => walkStat(stat)
        case expr: Expression => walkExpr(expr)
      }
      for (decl <- decls if declVars.contains(decl.symbol)) {
        if (declVars.get(decl.symbol).get.state.onStack) {
        	decl.onStack = true
        }
        declVars.remove(decl.symbol)
      }
  }
  
  override def transformStat(statement: Statement) = statement match {
    case LocalStatement(decls, stat) =>
      inspectLocalCommon(decls, stat)
      treeCopy.LocalStatement(statement, decls, transformStat(stat))
      
    case _ => super.transformStat(statement)
  }
  
  override def transformExpr(expression: Expression) = expression match {
    case LocalExpression(decls, expr) =>
      inspectLocalCommon(decls, expr)
      treeCopy.LocalExpression(expression, decls, transformExpr(expr))
      
    case _ => super.transformExpr(expression)
  }
}
