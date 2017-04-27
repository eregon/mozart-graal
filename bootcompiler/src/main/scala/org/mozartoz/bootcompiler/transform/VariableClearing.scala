package org.mozartoz.bootcompiler.transform

import java.util.IdentityHashMap

import scala.collection.mutable.ArrayBuffer
import scala.collection.mutable.Buffer
import scala.collection.mutable.HashMap
import scala.collection.mutable.HashSet

import org.mozartoz.bootcompiler.ast._
import org.mozartoz.bootcompiler.oz._
import org.mozartoz.bootcompiler.symtab._


/**
 * Designates a node a variable should be cleared at.
 */
case class ClearSite(node: StatOrExpr, before: Boolean)
/**
 * The complementary, a variable a node should clear. These two could be merged, but the separated
 * approach allows to express sites without decided symbols more clearly.
 */
case class Clear(sym: Symbol, before: Boolean)

/**
 * Variables cleared in branches. Must also designate which node should clear variables cleared
 * from other branches.
 */
case class BranchClearsMap(clearsMap: HashMap[Symbol, Seq[ClearSite]], default: ClearSite)

object VariableClearing extends Transformer with ReverseWalker with TreeDSL {
  type ClearsMap = HashMap[Symbol, Seq[ClearSite]]
  
  // Map of variables and the nodes eliminating them
  var clearsMap: ClearsMap = new HashMap();
  
  def undecided(): ClearsMap = {
    val ret = HashMap[Symbol, Seq[ClearSite]]()
    for ((k, v) <- clearsMap if v == null) {
      ret.put(k, null)
    }
    ret
  }
  
  def withClearsMap[A](clearsMap: ClearsMap)(f: => A): A = {
    val oldClearsMap = this.clearsMap
    this.clearsMap = clearsMap
    try {
      return f
    } finally {
      this.clearsMap = oldClearsMap
    }
  }
  
  def merge(branches: BranchClearsMap*) = {
    val localClearsMap = new HashMap[Symbol, Buffer[ClearSite]]()
    val localTouched = new HashSet[Symbol]()
    
    val branchClearsMaps = branches.map(_.clearsMap) // Actual clears from branches
    val branchDefault = branches.map(_.default) // ClearSite for missing variable clears
    
    // ClearVars from branches must be propagated
    for ((branchClearsMap, i) <- branchClearsMaps.zipWithIndex) {
      for ((sym, nodes) <- branchClearsMap) {
        if (nodes != null) {
          localTouched.add(sym)
        	val buffer = localClearsMap.getOrElseUpdate(sym, ArrayBuffer[ClearSite]())
    			buffer ++= nodes
        }
      }
    }
    
    // Variables that are cleared by a branch but not by another should be in the other ones as well
    // But only if they have been declared outside the branch!
    for((branchClearsMap, i) <- branchClearsMaps.zipWithIndex) {
      for (sym <- localTouched if branchClearsMap.get(sym) == Some(null)) {
        	val buffer = localClearsMap.getOrElseUpdate(sym, ArrayBuffer[ClearSite]())
      	  buffer += branchDefault(i)
      }
    }
    
    // Changes are then to be made global
    for ((sym, clears) <- localClearsMap) {
      clearsMap.put(sym, clears)
    }
  }
  
  override def walkStat(statement: Statement) = statement match {
    case LocalStatement(decls, stat) =>
      decls.foreach { decl =>
        clearsMap.put(decl.symbol, null)
      }
      walkStat(stat)
      
    case IfStatement(condition, trueStat, falseStat) =>
      merge(withClearsMap(undecided) {
        walkStat(trueStat)
        BranchClearsMap(this.clearsMap, ClearSite(trueStat, true))
      }, withClearsMap(undecided) {
    	  walkStat(falseStat)
        BranchClearsMap(this.clearsMap, ClearSite(falseStat, true))
      })
      walkExpr(condition)
      
    case MatchStatement(value, clauses, elseStatement) =>
      /* If walkExpr was used, the variable would be cleared in-place, which is not desirable because
       * it will be copied in the final AST, and so might clear itself before being read again */
      def clearVal(stat: Statement) = {
        val sym = value.asInstanceOf[Variable].symbol
        if (this.clearsMap.get(sym) == Some(null))
          this.clearsMap.put(sym, Seq(ClearSite(stat, true)))
      }
      
      merge((clauses.map { clause =>
        withClearsMap(undecided) {
        	walkStat(clause.body)
        	clause.guard.foreach(walkExpr(_))
        	walkExpr(clause.pattern)
        	clearVal(clause.body)
        	BranchClearsMap(this.clearsMap, ClearSite(clause.body, true))
        }
      } :+ withClearsMap(undecided) {
        walkStat(elseStatement)
        clearVal(elseStatement)
        BranchClearsMap(this.clearsMap, ClearSite(elseStatement, true))
      }): _*)
      
    case _ => super.walkStat(statement)
  }
  
  override def walkExpr(expression: Expression) = expression match {
    case v @ Variable(sym) =>
      if (clearsMap.get(sym) == Some(null)) {
        clearsMap.put(sym, Seq(ClearSite(v, before=false)))
      }
      
    case LocalExpression(decls, expr) =>
      decls.foreach { decl =>
        clearsMap.put(decl.symbol, null)
      }
      walkExpr(expr)
      
    case ProcExpression(name, args, body, flags) =>
      val walker = new CaptureWalker(this.clearsMap)
      walker.walkStat(body)
      val captured = walker.captured
      for (sym <- walker.captured) {
        clearsMap.put(sym, Seq(ClearSite(expression, before=false)))
      }
      
    case IfExpression(condition, trueExpr, falseExpr) =>
      merge(withClearsMap(undecided) {
        walkExpr(trueExpr)
        BranchClearsMap(this.clearsMap, ClearSite(trueExpr, true))
      }, withClearsMap(undecided) {
    	  walkExpr(falseExpr)
        BranchClearsMap(this.clearsMap, ClearSite(falseExpr, true))
      })
      walkExpr(condition)
      
    case ShortCircuitBinaryOp(left, "andthen", right) => // We use the substitution mechanism, only if necessary.
      val subst = treeCopy.IfExpression(expression, left, right, Constant(False())(right.pos))
      walkExpr(subst)
      nodeSubst.put(expression, subst)
      
    case ShortCircuitBinaryOp(left, "orelse", right) =>
      val subst = treeCopy.IfExpression(expression, left, Constant(True())(right.pos), right)
      walkExpr(subst)
      nodeSubst.put(expression, subst)
      
    case MatchExpression(value, clauses, elseExpression) =>
      def clearVal(expr: Expression) = {
        val sym = value.asInstanceOf[Variable].symbol
        if (this.clearsMap.get(sym) == Some(null))
          this.clearsMap.put(sym, Seq(ClearSite(expr, true)))
      }
      
      merge((clauses.map { clause =>
        withClearsMap(undecided) {
        	walkExpr(clause.body)
        	clause.guard.foreach(walkExpr(_))
        	walkExpr(clause.pattern)
        	clearVal(clause.body)
        	BranchClearsMap(this.clearsMap, ClearSite(clause.body, true))
        }
      } :+ withClearsMap(undecided) {
        walkExpr(elseExpression)
        clearVal(elseExpression)
        BranchClearsMap(this.clearsMap, ClearSite(elseExpression, true))
      }): _*)
      
    case _ => super.walkExpr(expression)
  }
  
  var nodeSubst: IdentityHashMap[StatOrExpr, StatOrExpr] = new IdentityHashMap()
  var nodeClears: IdentityHashMap[StatOrExpr, Buffer[Clear]] = new IdentityHashMap()
  def withNodeClears[A](nodeReplacements: IdentityHashMap[StatOrExpr, Buffer[Clear]])(f: => A): A = {
    val oldNodeClears = this.nodeClears
    this.nodeClears = nodeReplacements
    try {
      return f
    } finally {
      this.nodeClears = oldNodeClears
    }
  }
  
  def clearsMap2nodeClears(clearsMap: ClearsMap): IdentityHashMap[StatOrExpr, Buffer[Clear]] = {
    val nodeClears = new IdentityHashMap[StatOrExpr, Buffer[Clear]]()
    for ((sym, clears) <- clearsMap if clears != null) { // TODO WHAT IF UNENCOUNTERED?
      for (ClearSite(node, before) <- clears) {
        var buffer = nodeClears.get(node)
        if (buffer == null) {
          buffer = ArrayBuffer[Clear]()
          nodeClears.put(node, buffer)
        }
        buffer += Clear(sym, before)
      }
    }
    
    nodeClears
  }
  
  def replaceStat(statement: Statement): Statement = {
    val replacements = nodeClears.get(statement)
    if (replacements != null) {
      treeCopy.ClearVarsStatement(statement, super.transformStat(statement),
          replacements.filter(_.before).map(_.sym),
          replacements.filter(!_.before).map(_.sym))
    } else {
  	  super.transformStat(statement)
    }
  }
  
  def replaceExpr(expression: Expression): Expression = {
    val replacements = nodeClears.get(expression)
      if (replacements != null) {
        treeCopy.ClearVarsExpression(expression, super.transformExpr(expression),
          replacements.filter(_.before).map(_.sym),
          replacements.filter(!_.before).map(_.sym))
      } else {
    	  super.transformExpr(expression)
      }
  }
  
  override def transformExpr(expression: Expression) = expression match {
    case proc @ ProcExpression(name, args, body, flags) =>
      val clearsMap = withClearsMap(new HashMap()) {
        args.foreach (_ match { case Variable(sym) => this.clearsMap.put(sym, null) })
    	  walkStat(body)
    	  this.clearsMap
      }
      withNodeClears(clearsMap2nodeClears(clearsMap)) {
    	  treeCopy.ProcExpression(expression, name, args, replaceStat(body), flags)
      }
      
    case v @ Variable(sym) => // Some nodes expect a variable as child. Do not create unnecessary resetters
      val repl = nodeClears.get(v)
      if (repl != null) {
        val idx = repl.indexWhere { clear => clear.sym == sym }
        if (idx >= 0) {
          v.clear = true
          if (repl.length == 1) {
            nodeClears.put(v, null)
          } else {
            repl.remove(idx)
          }
        }
      }
      replaceExpr(v)
      
    case ShortCircuitBinaryOp(left, "andthen", right) =>
      nodeSubst.get(expression) match {
        case ifExpr @ IfExpression(left, longCircuit, shortCircuit) =>
          if (nodeClears.get(shortCircuit) != null) {
          	replaceExpr(ifExpr)
          } else {
            treeCopy.ShortCircuitBinaryOp(expression, replaceExpr(left), "andthen", right)
          }
      }
      
    case ShortCircuitBinaryOp(left, "orelse", right) =>
      nodeSubst.get(expression) match {
        case ifExpr @ IfExpression(left, shortCircuit, longCircuit) =>
          if (nodeClears.get(shortCircuit) != null) {
          	replaceExpr(ifExpr)
          } else {
            treeCopy.ShortCircuitBinaryOp(expression, replaceExpr(left), "orelse", right)
          }
      }
      
    case NoElseExpression() => expression
    case _ => replaceExpr(expression)
  }
  
  override def transformStat(statement: Statement) = statement match {
    case NoElseStatement() => statement
    case _ => replaceStat(statement)
  }
}

class CaptureWalker(val map: HashMap[Symbol, Seq[ClearSite]]) extends Walker {
  val captured = new HashSet[Symbol]()
  
  override def walkExpr(expression: Expression) = expression match {
    case Variable(sym) =>
      if (map.get(sym) == Some(null)) {
    	  captured.add(sym)
      }
      
    case _ => super.walkExpr(expression)
  }
}