package org.mozartoz.bootcompiler.transform

import org.mozartoz.bootcompiler.ast.Expression
import org.mozartoz.bootcompiler.ast.Statement
import org.mozartoz.bootcompiler.ast.CompoundStatement
import org.mozartoz.bootcompiler.ast.SkipStatement
import org.mozartoz.bootcompiler.ast.DotAssignStatement
import org.mozartoz.bootcompiler.ast.RaiseStatement
import org.mozartoz.bootcompiler.ast.FailStatement
import org.mozartoz.bootcompiler.ast.CallStatement
import org.mozartoz.bootcompiler.ast.LocalStatement
import org.mozartoz.bootcompiler.ast.IfStatement
import org.mozartoz.bootcompiler.ast.LockStatement
import org.mozartoz.bootcompiler.ast.ProcExpression
import org.mozartoz.bootcompiler.ast.NoElseStatement
import org.mozartoz.bootcompiler.ast.TryFinallyStatement
import org.mozartoz.bootcompiler.ast.LockObjectStatement
import org.mozartoz.bootcompiler.ast.ThreadStatement
import org.mozartoz.bootcompiler.ast.RawLocalStatement
import org.mozartoz.bootcompiler.ast.MatchStatement
import org.mozartoz.bootcompiler.ast.TryStatement
import org.mozartoz.bootcompiler.ast.ForStatement
import org.mozartoz.bootcompiler.ast.BinaryOpStatement
import org.mozartoz.bootcompiler.ast.BindStatement
import org.mozartoz.bootcompiler.ast.MatchExpressionClause
import org.mozartoz.bootcompiler.ast.RecordField
import org.mozartoz.bootcompiler.ast.RawDeclaration
import org.mozartoz.bootcompiler.ast.MethodDef
import org.mozartoz.bootcompiler.ast.FeatOrAttr
import org.mozartoz.bootcompiler.ast.MatchStatementClause
import org.mozartoz.bootcompiler.ast.TryExpression
import org.mozartoz.bootcompiler.ast.CallExpression
import org.mozartoz.bootcompiler.ast.NestingMarker
import org.mozartoz.bootcompiler.ast.NoElseExpression
import org.mozartoz.bootcompiler.ast.DotAssignExpression
import org.mozartoz.bootcompiler.ast.LocalStatementOrRaw
import org.mozartoz.bootcompiler.ast.FunctorExpression
import org.mozartoz.bootcompiler.ast.StatAndExpression
import org.mozartoz.bootcompiler.ast.IfExpression
import org.mozartoz.bootcompiler.ast.RawLocalExpression
import org.mozartoz.bootcompiler.ast.Self
import org.mozartoz.bootcompiler.ast.EscapedVariable
import org.mozartoz.bootcompiler.ast.BinaryOp
import org.mozartoz.bootcompiler.ast.OpenRecordPattern
import org.mozartoz.bootcompiler.ast.BindExpression
import org.mozartoz.bootcompiler.ast.LocalExpression
import org.mozartoz.bootcompiler.ast.LockObjectExpression
import org.mozartoz.bootcompiler.ast.PatternConjunction
import org.mozartoz.bootcompiler.ast.UnaryOp
import org.mozartoz.bootcompiler.ast.RaiseExpression
import org.mozartoz.bootcompiler.ast.LockExpression
import org.mozartoz.bootcompiler.ast.TryFinallyExpression
import org.mozartoz.bootcompiler.ast.FunExpression
import org.mozartoz.bootcompiler.ast.ClassExpression
import org.mozartoz.bootcompiler.ast.AutoFeature
import org.mozartoz.bootcompiler.ast.MatchExpression
import org.mozartoz.bootcompiler.ast.ShortCircuitBinaryOp
import org.mozartoz.bootcompiler.ast.UnboundExpression
import org.mozartoz.bootcompiler.ast.RawVariable
import org.mozartoz.bootcompiler.ast.ThreadExpression
import org.mozartoz.bootcompiler.ast.Constant
import org.mozartoz.bootcompiler.ast.Variable
import org.mozartoz.bootcompiler.ast.Record
import org.mozartoz.bootcompiler.ast.ListExpression

trait Walker {
	/** Transforms a Statement */
  def walkStat(statement: Statement): Unit = (statement: @unchecked) match {
    case CompoundStatement(stats) =>
      stats map walkStat

    case RawLocalStatement(declarations, body) =>
    	declarations map walkDecl
    	walkStat(body)

    case LocalStatement(declarations, body) =>
      walkStat(body)

    case CallStatement(callable, args) =>
    	walkExpr(callable)
      args map walkExpr

    case IfStatement(condition, trueStatement, falseStatement) =>
    	walkExpr(condition)
    	walkStat(trueStatement)
    	walkStat(falseStatement)

    case MatchStatement(value, clauses, elseStatement) =>
    	walkExpr(value)
    	clauses map walkClauseStat
    	walkStat(elseStatement)

    case NoElseStatement() =>

    case ForStatement(from, to, proc) =>
    	walkExpr(from)
    	walkExpr(to)
    	walkExpr(proc)

    case ThreadStatement(body) =>
      walkStat(body)

    case LockStatement(lock, body) =>
    	walkExpr(lock)
    	walkStat(body)

    case LockObjectStatement(body) =>
      walkStat(body)

    case TryStatement(body, exceptionVar, catchBody) =>
    	walkStat(body)
    	walkStat(catchBody)

    case TryFinallyStatement(body, finallyBody) =>
    	walkStat(body)
    	walkStat(finallyBody)

    case RaiseStatement(body) =>
      walkExpr(body)

    case FailStatement() =>

    case BindStatement(left, right) =>
    	walkExpr(left)
    	walkExpr(right)

    case BinaryOpStatement(left, operator, right) =>
    	walkExpr(left)
    	walkExpr(right)

    case DotAssignStatement(left, center, right) =>
    	walkExpr(left)
    	walkExpr(center)
    	walkExpr(right)

    case SkipStatement() =>
  }
  
  def walkExpr(expression: Expression): Unit = (expression: @unchecked) match {
    case StatAndExpression(statement, expr) =>
    	walkStat(statement)
    	walkExpr(expr)

    case RawLocalExpression(declarations, expr) =>
    	declarations map walkDecl
    	walkExpr(expr)

    case LocalExpression(declarations, expr) =>
    	walkExpr(expr)

    // Complex expressions

    case ProcExpression(name, args, body, flags) =>
      walkStat(body)

    case FunExpression(name, args, body, flags) =>
      walkExpr(body)

    case CallExpression(callable, args) =>
    	walkExpr(callable)
    	args map walkExpr

    case IfExpression(condition, trueExpression, falseExpression) =>
    	walkExpr(condition)
    	walkExpr(trueExpression)
    	walkExpr(falseExpression)

    case MatchExpression(value, clauses, elseExpression) =>
    	walkExpr(value)
    	clauses map walkClauseExpr
    	walkExpr(elseExpression)

    case NoElseExpression() =>

    case ThreadExpression(body) =>
      walkExpr(body)

    case LockExpression(lock, body) =>
    	walkExpr(lock)
    	walkExpr(body)

    case LockObjectExpression(body) =>
      walkExpr(body)

    case TryExpression(body, exceptionVar, catchBody) =>
    	walkExpr(body)
    	walkExpr(catchBody)

    case TryFinallyExpression(body, finallyBody) =>
    	walkExpr(body)
    	walkStat(finallyBody)

    case RaiseExpression(body) =>
      walkExpr(body)

    case BindExpression(left, right) =>
    	walkExpr(left)
    	walkExpr(right)

    case DotAssignExpression(left, center, right) =>
    	walkExpr(left)
    	walkExpr(center)
    	walkExpr(right)

    case FunctorExpression(name, require, prepare, imports, define, exports) =>
      prepare map walkStat
      define map walkStat
    
    // Operations

    case UnaryOp(operator, operand) =>
      walkExpr(operand)

    case BinaryOp(left, operator, right) =>
    	walkExpr(left)
      walkExpr(right)

    case ShortCircuitBinaryOp(left, operator, right) =>
      walkExpr(left)
      walkExpr(right)

    // Trivial expressions

    case RawVariable(name) =>
    case Variable(symbol) =>
    case EscapedVariable(variable) =>
    case UnboundExpression() =>
    case NestingMarker() =>
    case Self() =>

    // Constants

    case Constant(value) =>

    // Records

    case AutoFeature() =>

    case Record(label, fields) =>
    	walkExpr(label)
    	fields map walkRecordField

    case OpenRecordPattern(label, fields) =>
    	walkExpr(label)
    	fields map walkRecordField

    case ListExpression(elements) =>
      elements map walkExpr

    case PatternConjunction(parts) =>
      parts map walkExpr

    // Classes

    case ClassExpression(name, parents, features, attributes, properties, methods) =>
        	parents map walkExpr
        	features map walkFeatOrAttr
        	attributes map walkFeatOrAttr
        	properties map walkExpr
        	methods map walkMethodDef
  }
  
  /** Transforms a declaration */
  def walkDecl(declaration: RawDeclaration): Unit = declaration match {
    case stat:Statement => walkStat(stat)
    case _ => 
  }

  /** Transforms a record field */
  private def walkRecordField(field: RecordField): Unit = {
		  walkExpr(field.feature)
		  walkExpr(field.value)
  }

  /** Transforms a clause of a match statement */
  def walkClauseStat(clause: MatchStatementClause) = {
    walkExpr(clause.pattern)
    walkStat(clause.body)
    clause.guard map walkExpr
  }

  /** Transforms a clause of a match expression */
  def walkClauseExpr(clause: MatchExpressionClause) = {
		  walkExpr(clause.pattern)
		  clause.guard map walkExpr
		  walkExpr(clause.body)
  }

  /** Transforms a feature or an attribute of a class */
  def walkFeatOrAttr(featOrAttr: FeatOrAttr) = {
		  walkExpr(featOrAttr.name)
		  featOrAttr.value map walkExpr
  }

  /** Transforms a method definition */
  def walkMethodDef(method: MethodDef): Unit = {
    method.body match {
      case stat:Statement => walkStat(stat)
      case expr:Expression => walkExpr(expr)
    }
  }
}