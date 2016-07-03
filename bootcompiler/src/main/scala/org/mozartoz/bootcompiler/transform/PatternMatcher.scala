package org.mozartoz.bootcompiler
package transform

import scala.collection.mutable.ListBuffer

import ast._
import oz._
import symtab._

object PatternMatcher extends Transformer with TreeDSL {
  override def transformStat(statement: Statement) = statement match {
    case matchStat @ MatchStatement(value, clauses, elseStat)
    if clauses exists (clause => containsVariable(clause.pattern)) =>
      val newCaptures = new ListBuffer[Variable]

      val newClauses = for {
        clause @ MatchStatementClause(pattern, guard, body) <- clauses
      } yield {
        val (newPattern, newGuard) = processVariablesInPattern(
            pattern, guard, newCaptures)
        treeCopy.MatchStatementClause(clause, newPattern, newGuard, body)
      }

      transformStat {
        LOCAL (newCaptures:_*) IN {
          treeCopy.MatchStatement(matchStat, value, newClauses, elseStat)
        }
      }

    case _ =>
      super.transformStat(statement)
  }

  override def transformExpr(expression: Expression) = expression match {
    case matchExpr @ MatchExpression(value, clauses, elseExpr)
    if clauses exists (clause => containsVariable(clause.pattern)) =>
      val newCaptures = new ListBuffer[Variable]

      val newClauses = for {
        clause @ MatchExpressionClause(pattern, guard, body) <- clauses
      } yield {
        val (newPattern, newGuard) = processVariablesInPattern(
            pattern, guard, newCaptures)
        treeCopy.MatchExpressionClause(clause, newPattern, newGuard, body)
      }

      transformExpr {
        LOCAL (newCaptures:_*) IN {
          treeCopy.MatchExpression(matchExpr, value, newClauses, elseExpr)
        }
      }

    case _ =>
      super.transformExpr(expression)
  }

  private def containsVariable(pattern: Expression): Boolean = {
    pattern walk {
      case Variable(_) => return true
      case _ => ()
    }
    return false
  }

  private def processVariablesInPattern(pattern: Expression,
      guard: Option[Expression],
      captures: ListBuffer[Variable]): (Expression, Option[Expression]) = {
    if (!containsVariable(pattern)) {
      (pattern, guard)
    } else {
      val guardsBuffer = new ListBuffer[Expression]
      val newPattern = processVariablesInPatternInner(pattern,
          captures, guardsBuffer)
      guardsBuffer ++= guard

      val guards = guardsBuffer
      assert(!guards.isEmpty)

      val newGuard = guards.tail.foldLeft(guards.head) {
        (lhs, rhs) => IF (lhs) THEN (rhs) ELSE (False())
      }

      (newPattern, Some(newGuard))
    }
  }

  /** Processes the variables in a pattern (inner) */
  private def processVariablesInPatternInner(pattern: Expression,
      captures: ListBuffer[Variable],
      guards: ListBuffer[Expression]): Expression = {

    def processRecordFields(fields: Seq[RecordField]) = {
      for (field @ RecordField(feature, value) <- fields) yield {
        val newValue = processVariablesInPatternInner(value, captures, guards)
        treeCopy.RecordField(field, feature, newValue)
      }
    }

    pattern match {
      /* Variable, what we're here for */
      case v @ Variable(symbol) =>
        val capture = new Symbol(symbol.name + "$", capture = true)
        captures += capture
        guards += builtins.binaryOpToBuiltin("==") callExpr (capture, v)
        treeCopy.Constant(pattern, OzPatMatCapture(capture))

      /* Dive into records */
      case record @ Record(label, fields) =>
        treeCopy.Record(record,
            processVariablesInPatternInner(label, captures, guards),
            processRecordFields(fields))

      /* Dive into open record patterns */
      case pattern @ OpenRecordPattern(label, fields) =>
        treeCopy.OpenRecordPattern(pattern,
            processVariablesInPatternInner(label, captures, guards),
            processRecordFields(fields))

      /* Dive into pattern conjunctions */
      case conj @ PatternConjunction(parts) =>
        val newParts = parts map {
          part => processVariablesInPatternInner(part, captures, guards)
        }
        treeCopy.PatternConjunction(conj, newParts)

      case _ =>
        pattern
    }
  }
}
