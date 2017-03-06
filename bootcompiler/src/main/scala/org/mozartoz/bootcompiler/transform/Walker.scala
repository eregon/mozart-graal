package org.mozartoz.bootcompiler
package transform

import ast._

trait Walker {
  /** Transforms a Statement */
  def walkStat(statement: Statement): Unit = (statement: @unchecked) match {
    case CompoundStatement(stats) =>
      stats foreach walkStat

    case RawLocalStatement(declarations, body) =>
      declarations foreach walkDecl
      walkStat(body)

    case LocalStatement(declarations, body) =>
      walkStat(body)

    case CallStatement(callable, args) =>
      walkExpr(callable)
      args foreach walkExpr

    case IfStatement(condition, trueStatement, falseStatement) =>
      walkExpr(condition)
      walkStat(trueStatement)
      walkStat(falseStatement)

    case MatchStatement(value, clauses, elseStatement) =>
      walkExpr(value)
      clauses foreach walkClauseStat
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
      declarations foreach walkDecl
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
      args foreach walkExpr

    case IfExpression(condition, trueExpression, falseExpression) =>
      walkExpr(condition)
      walkExpr(trueExpression)
      walkExpr(falseExpression)

    case MatchExpression(value, clauses, elseExpression) =>
      walkExpr(value)
      clauses foreach walkClauseExpr
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
      prepare foreach walkStat
      define foreach walkStat

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
      fields foreach walkRecordField

    case OpenRecordPattern(label, fields) =>
      walkExpr(label)
      fields foreach walkRecordField

    case ListExpression(elements) =>
      elements foreach walkExpr

    case PatternConjunction(parts) =>
      parts foreach walkExpr

    // Classes

    case ClassExpression(name, parents, features, attributes, properties, methods) =>
      parents foreach walkExpr
      features foreach walkFeatOrAttr
      attributes foreach walkFeatOrAttr
      properties foreach walkExpr
      methods foreach walkMethodDef
  }

  /** Transforms a declaration */
  def walkDecl(declaration: RawDeclaration): Unit = declaration match {
    case stat: Statement => walkStat(stat)
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
    clause.guard foreach walkExpr
  }

  /** Transforms a clause of a match expression */
  def walkClauseExpr(clause: MatchExpressionClause) = {
    walkExpr(clause.pattern)
    clause.guard foreach walkExpr
    walkExpr(clause.body)
  }

  /** Transforms a feature or an attribute of a class */
  def walkFeatOrAttr(featOrAttr: FeatOrAttr) = {
    walkExpr(featOrAttr.name)
    featOrAttr.value foreach walkExpr
  }

  /** Transforms a method definition */
  def walkMethodDef(method: MethodDef): Unit = {
    method.body match {
      case stat: Statement => walkStat(stat)
      case expr: Expression => walkExpr(expr)
    }
  }
}