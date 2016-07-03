package org.mozartoz.bootcompiler.fastparse

import org.mozartoz.bootcompiler.ast._
import org.mozartoz.bootcompiler.transform.TreeCopier

object TypeAST {

  val treeCopy = new TreeCopier

  def expr(p: Phrase): Expression = p match {
    case _ if p.isInstanceOf[Expression] =>
      p.asInstanceOf[Expression]
    case CompoundPhrase(parts) =>
      parts match {
        case Seq(expression) =>
          expr(expression)
        case Seq(statement, expression) =>
          treeCopy.StatAndExpression(p, stat(statement), expr(expression))
        case _ =>
          treeCopy.StatAndExpression(p, stat(CompoundPhrase(parts.slice(0, parts.size - 1))), expr(parts.last))
      }
    case RecordPhrase(label, fields) =>
      treeCopy.Record(p, expr(label), fields.map(_.toExpr))
    case OpenRecordPatternPhrase(label, fields) =>
      treeCopy.OpenRecordPattern(p, expr(label), fields.map(_.toExpr))
    case PatternConjunctionPhrase(parts) =>
      treeCopy.PatternConjunction(p, parts.map(expr))
    case UnaryOpPhrase(operator, operand) =>
      treeCopy.UnaryOp(p, operator, expr(operand))
    case BinaryOpPhrase(left, operator, right) =>
      treeCopy.BinaryOp(p, expr(left), operator, expr(right))
    case ShortCircuitBinaryOpPhrase(left, operator, right) =>
      treeCopy.ShortCircuitBinaryOp(p, expr(left), operator, expr(right))
    case DotAssignPhrase(left, center, right) =>
      treeCopy.DotAssignExpression(p, expr(left), expr(center), expr(right))
    case CallPhrase(callable, args) =>
      treeCopy.CallExpression(p, expr(callable), args.map(expr))
    case RaisePhrase(exception) =>
      treeCopy.RaiseExpression(p, expr(exception))
    case BindPhrase(left, right) =>
      treeCopy.BindExpression(p, expr(left), expr(right))
    case RawLocalPhrase(decls, body) =>
      treeCopy.RawLocalExpression(p, localDecls(decls), expr(body))
    case IfPhrase(condition, truePhrase, falsePhrase) =>
      treeCopy.IfExpression(p, expr(condition), expr(truePhrase), expr(falsePhrase))
    case NoElsePhrase() =>
      treeCopy.NoElseExpression(p)
    case MatchPhrase(value, clauses, elsePhrase) =>
      treeCopy.MatchExpression(p, expr(value), clauses.map(_.toExpr), expr(elsePhrase))
    case TryPhrase(body, exceptionVar, catchBody) =>
      treeCopy.TryExpression(p, expr(body), exceptionVar, expr(catchBody))
    case ThreadPhrase(body) =>
      treeCopy.ThreadExpression(p, expr(body))
    case LockPhrase(lock, body) =>
      treeCopy.LockExpression(p, expr(lock), expr(body))
    case LockObjectPhrase(body) =>
      treeCopy.LockObjectExpression(p, expr(body))
    case ProcPhrase(name, args, body, flags) =>
      treeCopy.ProcExpression(p, name, args, stat(body), flags)
    case FunPhrase(name, args, body, flags) =>
      treeCopy.FunExpression(p, name, args, expr(body), flags)
    case FunctorPhrase(name, require, prepare, imports, define, exports) =>
      treeCopy.FunctorExpression(p, name, require, prepare.map(functorBody), imports, define.map(functorBody), exports)
    case ClassPhrase(name, parents, features, attributes, properties, methods) =>
      treeCopy.ClassExpression(p, name, parents.map(expr),
        features.map(_.toExpr), attributes.map(_.toExpr),
        properties.map(expr), methods.map(_.resolve))
  }

  def stat(p: Phrase): Statement = p match {
    case _ if p.isInstanceOf[Statement] =>
      p.asInstanceOf[Statement]
    case CompoundPhrase(parts) =>
      treeCopy.CompoundStatement(p, parts.map(stat))
    case BinaryOpPhrase(left, operator, right) =>
      treeCopy.BinaryOpStatement(p, expr(left), operator, expr(right))
    case DotAssignPhrase(left, center, right) =>
      treeCopy.DotAssignStatement(p, expr(left), expr(center), expr(right))
    case BindPhrase(left, right) =>
      treeCopy.BindStatement(p, expr(left), expr(right))
    case CallPhrase(callable, args) =>
      treeCopy.CallStatement(p, expr(callable), args.map(expr))
    case RaisePhrase(exception) =>
      treeCopy.RaiseStatement(p, expr(exception))
    case RawLocalPhrase(decls, body) =>
      treeCopy.RawLocalStatement(p, localDecls(decls), stat(body))
    case IfPhrase(condition, truePhrase, falsePhrase) =>
      treeCopy.IfStatement(p, expr(condition), stat(truePhrase), stat(falsePhrase))
    case NoElsePhrase() =>
      treeCopy.NoElseStatement(p)
    case MatchPhrase(value, clauses, elsePhrase) =>
      treeCopy.MatchStatement(p, expr(value), clauses.map(_.toStat), stat(elsePhrase))
    case TryPhrase(body, exceptionVar, catchBody) =>
      treeCopy.TryStatement(p, stat(body), exceptionVar, stat(catchBody))
    case TryFinallyPhrase(body, finallyBody) =>
      treeCopy.TryFinallyStatement(p, stat(body), stat(finallyBody))
    case ThreadPhrase(body) =>
      treeCopy.ThreadStatement(p, stat(body))
    case LockPhrase(lock, body) =>
      treeCopy.LockStatement(p, expr(lock), stat(body))
    case LockObjectPhrase(body) =>
      treeCopy.LockObjectStatement(p, stat(body))
  }

  def localDecls(p: Phrase): Seq[RawDeclaration] = p match {
    case CompoundPhrase(parts) => parts.flatMap(localDecls)
    case v @ RawVariable(name) => Seq(v)
    case _                     => Seq(stat(p).asInstanceOf[RawDeclaration])
  }

  def functorBody(p: Phrase): LocalStatementOrRaw = p match {
    case RawLocalPhrase(declarations, body) => stat(p).asInstanceOf[LocalStatementOrRaw]
    case declarations                       => treeCopy.RawLocalStatement(p, localDecls(declarations), treeCopy.SkipStatement(p))
  }

}