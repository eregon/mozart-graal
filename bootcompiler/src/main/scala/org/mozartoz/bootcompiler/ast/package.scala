package org.mozartoz.bootcompiler

import oz._
import ast.Node.Pos

/** Classes representing the AST of Oz code
 *
 *  Provides general utilities for working with ASTs.
 */
package object ast {
  // Utils

  def escapePseudoChars(name: String, delim: Char) = {
    val result = new StringBuffer
    name foreach { c =>
      if (c == '\\' || c == delim)
        result append '\\'
      result append c
    }
    result.toString
  }

  /** Builds an Oz List expression from a list of expressions */
  def exprListToListExpr(elems: Seq[Expression])(pos: Pos): Expression = {
    elems.foldRight(Constant(OzAtom("nil"))(pos): Expression)((e, tail) => cons(e, tail))
  }

  /** Builds an Oz Cons pair */
  def cons(head: Expression, tail: Expression) =
    Record(Constant(OzAtom("|"))(head),
        Seq(withAutoFeature(head), withAutoFeature(tail)))(head)

  /** Builds an Oz #-tuple */
  def sharp(fields: Seq[Expression])(pos: Pos) = {
    if (fields.isEmpty) Constant(OzAtom("#"))(pos)
    else {
      Record(Constant(OzAtom("#"))(fields(0)), fields map withAutoFeature)(fields.head)
    }
  }

  /** Equips an expression with an AutoFeature */
  def withAutoFeature(expr: Expression): RecordField =
    RecordField(AutoFeature()(expr), expr)(expr)
}
