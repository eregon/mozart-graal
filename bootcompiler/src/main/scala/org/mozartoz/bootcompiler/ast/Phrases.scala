package org.mozartoz.bootcompiler
package ast

import org.mozartoz.bootcompiler.fastparse.TypeAST

trait Phrase extends StatOrExpr

abstract class PhraseNode extends Phrase {
  def syntax(indent: String) = "<phrase>"
  def expr(phrase: Phrase) = TypeAST.expr(phrase)
  def stat(phrase: Phrase) = TypeAST.stat(phrase)
}

case class CompoundPhrase(parts: List[Phrase]) extends PhraseNode {
  posFrom(parts(0), parts.last)
}

case class RawLocalPhrase(declarations: Phrase, body: Phrase) extends PhraseNode {
  posFrom(declarations, body)
}

case class ProcPhrase(name: String, args: List[VariableOrRaw], body: Phrase, flags: List[String]) extends PhraseNode

case class FunPhrase(name: String, args: List[VariableOrRaw], body: Phrase, flags: List[String]) extends PhraseNode

case class CallPhrase(callable: Phrase, args: List[Phrase]) extends PhraseNode

case class IfPhrase(condition: Phrase, truePhrase: Phrase, falsePhrase: Phrase) extends PhraseNode

case class MatchPhrase(value: Phrase, clauses: List[MatchPhraseClause], elsePhrase: Phrase) extends PhraseNode

case class MatchPhraseClause(pattern: Phrase, guard: Option[Phrase], body: Phrase) extends PhraseNode {
  def toExpr = MatchExpressionClause(expr(pattern), guard.map(expr), expr(body)).copyAttrs(this)
  def toStat = MatchStatementClause(expr(pattern), guard.map(expr), stat(body)).copyAttrs(this)
}

case class NoElsePhrase() extends PhraseNode

case class ThreadPhrase(body: Phrase) extends PhraseNode

case class LockPhrase(lock: Phrase, body: Phrase) extends PhraseNode

case class LockObjectPhrase(body: Phrase) extends PhraseNode

case class TryPhrase(body: Phrase, exceptionVar: VariableOrRaw, catchBody: Phrase) extends PhraseNode

case class TryFinallyPhrase(body: Phrase, finallyBody: Phrase) extends PhraseNode

case class RaisePhrase(exception: Phrase) extends PhraseNode

case class BindPhrase(left: Phrase, right: Phrase) extends PhraseNode {
  posFrom(left, right)
}

case class DotAssignPhrase(left: Phrase, center: Phrase, right: Phrase) extends PhraseNode

// Functors

case class AliasedFeaturePhrase(feature: Constant, alias: Option[VariableOrRaw]) extends PhraseNode

case class FunctorImportPhrase(module: VariableOrRaw, aliases: List[AliasedFeature], location: Option[String]) extends PhraseNode

case class FunctorExportPhrase(feature: Phrase, value: Phrase) extends PhraseNode

case class FunctorPhrase(name: String,
                         require: List[FunctorImport], prepare: Option[Phrase],
                         imports: List[FunctorImport], define: Option[Phrase],
                         exports: List[FunctorExport]) extends PhraseNode

// Operations

/** Unary operation */
case class UnaryOpPhrase(operator: String, operand: Phrase) extends PhraseNode

/** Binary operation */
case class BinaryOpPhrase(left: Phrase, operator: String, right: Phrase) extends PhraseNode {
  posFrom(left, right)
}

/** Boolean binary operation with short-circuit semantics */
case class ShortCircuitBinaryOpPhrase(left: Phrase, operator: String, right: Phrase) extends PhraseNode {
  posFrom(left, right)
}

// Trivial expressions

// Records

case class RecordFieldPhrase(feature: Phrase, value: Phrase) extends PhraseNode {
  def toExpr = RecordField(expr(feature), expr(value))
}

case class RecordPhrase(label: Phrase, fields: List[RecordFieldPhrase]) extends PhraseNode

case class OpenRecordPatternPhrase(label: Phrase, fields: List[RecordFieldPhrase]) extends PhraseNode

case class PatternConjunctionPhrase(parts: List[Phrase]) extends PhraseNode {
  posFrom(parts(0), parts.last)
}

// Classes

case class FeatOrAttrPhrase(name: Phrase, value: Option[Phrase]) extends PhraseNode {
  def toExpr = FeatOrAttr(expr(name), value.map(expr))
}

case class MethodParamPhrase(feature: Phrase, name: Phrase, default: Option[Phrase]) extends PhraseNode {
  def toExpr = MethodParam(expr(feature), expr(name), default.map(expr))
}

case class MethodHeaderPhrase(name: Phrase, params: List[MethodParamPhrase], open: Boolean) extends PhraseNode {
  def toExpr = MethodHeader(expr(name), params.map(_.toExpr), open)
}

case class MethodDefPhrase(header: MethodHeaderPhrase, messageVar: Option[VariableOrRaw], body: Phrase) extends PhraseNode {
  def resolve = if (header.params exists { param => param.name.isInstanceOf[NestingMarker] }) {
    MethodDef(header.toExpr, messageVar, expr(body))
  } else {
    MethodDef(header.toExpr, messageVar, stat(body))
  }
}

case class ClassPhrase(name: String, parents: List[Phrase],
                       features: List[FeatOrAttrPhrase], attributes: List[FeatOrAttrPhrase],
                       properties: List[Phrase],
                       methods: List[MethodDefPhrase]) extends PhraseNode
