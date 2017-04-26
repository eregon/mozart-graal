package org.mozartoz.bootcompiler
package transform

import ast._

trait ReverseWalker {
  /** Transforms a Statement */
  def walkStat(statement: Statement): Unit = (statement: @unchecked) match {
    case CompoundStatement(stats) =>
      stats.reverse foreach walkStat

    case LocalStatement(declarations, body) =>
      walkStat(body)

    case CallStatement(callable, args) =>
      args.reverse foreach walkExpr
      walkExpr(callable)

    case IfStatement(condition, trueStatement, falseStatement) =>
      walkStat(falseStatement)
      walkStat(trueStatement)
      walkExpr(condition)

    case MatchStatement(value, clauses, elseStatement) =>
      walkStat(elseStatement)
      clauses.reverse foreach walkClauseStat
      walkExpr(value)

    case NoElseStatement() =>

    case ForStatement(from, to, proc) =>
      walkExpr(proc)
      walkExpr(to)
      walkExpr(from)

    case ThreadStatement(body) =>
      walkStat(body)

    case LockStatement(lock, body) =>
      walkStat(body)
      walkExpr(lock)

    case LockObjectStatement(body) =>
      walkStat(body)

    case TryStatement(body, exceptionVar, catchBody) =>
      walkStat(catchBody)
      walkStat(body)

    case TryFinallyStatement(body, finallyBody) =>
      walkStat(finallyBody)
      walkStat(body)

    case RaiseStatement(body) =>
      walkExpr(body)

    case FailStatement() =>

    case BindStatement(left, right) =>
      walkExpr(right)
      walkExpr(left)

    case BinaryOpStatement(left, operator, right) =>
      walkExpr(right)
      walkExpr(left)

    case DotAssignStatement(left, center, right) =>
      walkExpr(right)
      walkExpr(center)
      walkExpr(left)

    case SkipStatement() =>
      
    case ClearVarsStatement(stat, before, after) =>
      walkStat(stat)
  }

  def walkExpr(expression: Expression): Unit = (expression: @unchecked) match {
    case StatAndExpression(statement, expr) =>
      walkExpr(expr)
      walkStat(statement)

    case LocalExpression(declarations, expr) =>
      walkExpr(expr)

    // Complex expressions

    case ProcExpression(name, args, body, flags) =>
      walkStat(body)

    case FunExpression(name, args, body, flags) =>
      walkExpr(body)

    case CallExpression(callable, args) =>
      args.reverse foreach walkExpr
      walkExpr(callable)

    case IfExpression(condition, trueExpression, falseExpression) =>
      walkExpr(falseExpression)
      walkExpr(trueExpression)
      walkExpr(condition)

    case MatchExpression(value, clauses, elseExpression) =>
      walkExpr(elseExpression)
      clauses.reverse foreach walkClauseExpr
      walkExpr(value)

    case NoElseExpression() =>

    case ThreadExpression(body) =>
      walkExpr(body)

    case LockExpression(lock, body) =>
      walkExpr(body)
      walkExpr(lock)

    case LockObjectExpression(body) =>
      walkExpr(body)

    case TryExpression(body, exceptionVar, catchBody) =>
      walkExpr(catchBody)
      walkExpr(body)

    case TryFinallyExpression(body, finallyBody) =>
      walkStat(finallyBody)
      walkExpr(body)

    case RaiseExpression(body) =>
      walkExpr(body)

    case BindExpression(left, right) =>
      walkExpr(right)
      walkExpr(left)

    case DotAssignExpression(left, center, right) =>
      walkExpr(right)
      walkExpr(center)
      walkExpr(left)

    case FunctorExpression(name, require, prepare, imports, define, exports) =>
      define foreach walkStat
      prepare foreach walkStat
      
    case ClearVarsExpression(expr, before, after) =>
      walkExpr(expr)

    // Operations

    case UnaryOp(operator, operand) =>
      walkExpr(operand)

    case BinaryOp(left, operator, right) =>
      walkExpr(right)
      walkExpr(left)

    case ShortCircuitBinaryOp(left, operator, right) =>
      walkExpr(right)
      walkExpr(left)

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
      fields.reverse foreach walkRecordField
      walkExpr(label)

    case OpenRecordPattern(label, fields) =>
      fields.reverse foreach walkRecordField
      walkExpr(label)

    case ListExpression(elements) =>
      elements.reverse foreach walkExpr

    case PatternConjunction(parts) =>
      parts.reverse foreach walkExpr

    // Classes

    case ClassExpression(name, parents, features, attributes, properties, methods) =>
      methods.reverse foreach walkMethodDef
      properties.reverse foreach walkExpr
      attributes.reverse foreach walkFeatOrAttr
      features.reverse foreach walkFeatOrAttr
      parents.reverse foreach walkExpr
  }

  /** Transforms a declaration */
  def walkDecl(declaration: RawDeclaration): Unit = declaration match {
    case stat: Statement => walkStat(stat)
    case _ =>
  }

  /** Transforms a record field */
  private def walkRecordField(field: RecordField): Unit = {
    walkExpr(field.value)
    walkExpr(field.feature)
  }

  /** Transforms a clause of a match statement */
  def walkClauseStat(clause: MatchStatementClause) = {
    walkStat(clause.body)
    clause.guard foreach walkExpr
    walkExpr(clause.pattern)
  }

  /** Transforms a clause of a match expression */
  def walkClauseExpr(clause: MatchExpressionClause) = {
	  walkExpr(clause.body)
    clause.guard foreach walkExpr
    walkExpr(clause.pattern)
  }

  /** Transforms a feature or an attribute of a class */
  def walkFeatOrAttr(featOrAttr: FeatOrAttr) = {
    featOrAttr.value foreach walkExpr
    walkExpr(featOrAttr.name)
  }

  /** Transforms a method definition */
  def walkMethodDef(method: MethodDef): Unit = {
    method.body match {
      case stat: Statement => walkStat(stat)
      case expr: Expression => walkExpr(expr)
    }
  }
}