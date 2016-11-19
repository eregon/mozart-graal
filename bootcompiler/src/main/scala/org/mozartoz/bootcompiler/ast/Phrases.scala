package org.mozartoz.bootcompiler
package ast

import org.mozartoz.bootcompiler.fastparse.TypeAST
import Node.Pos

trait Phrase extends StatOrExpr {
  val pos: Pos
}

abstract class PhraseNode extends Phrase {
  def syntax(indent: String) = "<phrase>"
  def expr(phrase: Phrase) = TypeAST.expr(phrase)
  def stat(phrase: Phrase) = TypeAST.stat(phrase)
}

case class CompoundPhrase(parts: Seq[Phrase])(val pos: Pos) extends PhraseNode

case class RawLocalPhrase(declarations: Phrase, body: Phrase)(val pos: Pos) extends PhraseNode

case class ProcPhrase(name: Option[VariableOrRaw], args: Seq[VariableOrRaw], body: Phrase, flags: Seq[String])(val pos: Pos) extends PhraseNode

case class FunPhrase(name: Option[VariableOrRaw], args: Seq[VariableOrRaw], body: Phrase, flags: Seq[String])(val pos: Pos) extends PhraseNode

case class CallPhrase(callable: Phrase, args: Seq[Phrase])(val pos: Pos) extends PhraseNode

case class IfPhrase(condition: Phrase, truePhrase: Phrase, falsePhrase: Phrase)(val pos: Pos) extends PhraseNode

case class MatchPhrase(value: Phrase, clauses: Seq[MatchPhraseClause], elsePhrase: Phrase)(val pos: Pos) extends PhraseNode

case class MatchPhraseClause(pattern: Phrase, guard: Option[Phrase], body: Phrase)(val pos: Pos) extends PhraseNode {
  def toExpr = MatchExpressionClause(expr(pattern), guard.map(expr), expr(body))(pos)
  def toStat = MatchStatementClause(expr(pattern), guard.map(expr), stat(body))(pos)
}

case class NoElsePhrase()(val pos: Pos) extends PhraseNode

case class ForPhrase(from: Phrase, to: Phrase, proc: ProcPhrase)(val pos: Pos) extends PhraseNode

case class ThreadPhrase(body: Phrase)(val pos: Pos) extends PhraseNode

case class LockPhrase(lock: Phrase, body: Phrase)(val pos: Pos) extends PhraseNode

case class LockObjectPhrase(body: Phrase)(val pos: Pos) extends PhraseNode

case class TryPhrase(body: Phrase, exceptionVar: VariableOrRaw, catchBody: Phrase)(val pos: Pos) extends PhraseNode

case class TryFinallyPhrase(body: Phrase, finallyBody: Phrase)(val pos: Pos) extends PhraseNode

case class RaisePhrase(exception: Phrase)(val pos: Pos) extends PhraseNode

case class BindPhrase(left: Phrase, right: Phrase)(val pos: Pos) extends PhraseNode

case class DotAssignPhrase(left: Phrase, center: Phrase, right: Phrase)(val pos: Pos) extends PhraseNode

// Functors

case class AliasedFeaturePhrase(feature: Constant, alias: Option[VariableOrRaw])(val pos: Pos) extends PhraseNode

case class FunctorImportPhrase(module: VariableOrRaw, aliases: Seq[AliasedFeature], location: Option[String])(val pos: Pos) extends PhraseNode

case class FunctorExportPhrase(feature: Phrase, value: Phrase)(val pos: Pos) extends PhraseNode

case class FunctorPhrase(name: String,
                         require: Seq[FunctorImport], prepare: Option[Phrase],
                         imports: Seq[FunctorImport], define: Option[Phrase],
                         exports: Seq[FunctorExport])(val pos: Pos) extends PhraseNode

// Operations

/** Unary operation */
case class UnaryOpPhrase(operator: String, operand: Phrase)(val pos: Pos) extends PhraseNode

/** Binary operation */
case class BinaryOpPhrase(left: Phrase, operator: String, right: Phrase)(val pos: Pos) extends PhraseNode

/** Boolean binary operation with short-circuit semantics */
case class ShortCircuitBinaryOpPhrase(left: Phrase, operator: String, right: Phrase)(val pos: Pos) extends PhraseNode

// Trivial expressions

// Records

case class ListPhrase(elements: Seq[Phrase])(val pos: Pos) extends PhraseNode

case class RecordFieldPhrase(feature: Phrase, value: Phrase)(val pos: Pos) extends PhraseNode {
  def toExpr = RecordField(expr(feature), expr(value))(pos)
}

case class RecordPhrase(label: Phrase, fields: Seq[RecordFieldPhrase])(val pos: Pos) extends PhraseNode

case class OpenRecordPatternPhrase(label: Phrase, fields: Seq[RecordFieldPhrase])(val pos: Pos) extends PhraseNode

case class PatternConjunctionPhrase(parts: Seq[Phrase])(val pos: Pos) extends PhraseNode

// Classes

case class FeatOrAttrPhrase(name: Phrase, value: Option[Phrase])(val pos: Pos) extends PhraseNode {
  def toExpr = FeatOrAttr(expr(name), value.map(expr))(pos)
}

case class MethodParamPhrase(feature: Phrase, name: Phrase, default: Option[Phrase])(val pos: Pos) extends PhraseNode {
  def toExpr = MethodParam(expr(feature), expr(name), default.map(expr))(pos)
}

case class MethodHeaderPhrase(name: Phrase, params: Seq[MethodParamPhrase], open: Boolean)(val pos: Pos) extends PhraseNode {
  def toExpr = MethodHeader(expr(name), params.map(_.toExpr), open)(pos)
}

case class MethodDefPhrase(header: MethodHeaderPhrase, messageVar: Option[VariableOrRaw], body: Phrase)(val pos: Pos) extends PhraseNode {
  def resolve = if (header.params exists { param => param.name.isInstanceOf[NestingMarker] }) {
    MethodDef(header.toExpr, messageVar, expr(body))(pos)
  } else {
    MethodDef(header.toExpr, messageVar, stat(body))(pos)
  }
}

case class ClassPhrase(name: String, parents: Seq[Phrase],
                       features: Seq[FeatOrAttrPhrase], attributes: Seq[FeatOrAttrPhrase],
                       properties: Seq[Phrase],
                       methods: Seq[MethodDefPhrase])(val pos: Pos) extends PhraseNode
