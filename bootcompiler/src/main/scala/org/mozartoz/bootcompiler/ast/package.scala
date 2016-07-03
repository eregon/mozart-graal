package org.mozartoz.bootcompiler

import oz._

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

  /** Gives a position to a subtree
   *
   *  The position of `tree` is given to `node` and all its subtrees that do not
   *  yet have a position. If a subtree has a position, its children are not
   *  explored.
   *
   *  This method is mostly useful for synthesized AST subtrees, as in
   *  {{{
   *  val synthesized = atPos(oldTree.pos) {
   *    buildTheTree()
   *  }
   *  }}}
   *
   *  @tparam A type of node
   *  @param tree node to get the position from
   *  @param node root of the subtree to give a position to
   *  @return the node `node`
   */
  def atPos[A <: Node](tree: Node)(node: A): A = {
    node walkBreak { subNode =>
      if (subNode.section != null) {
        false
      } else {
        subNode.copyAttrs(tree)
        true
      }
    }
    node
  }

  /** Builds an Oz List expression from a list of expressions */
  def exprListToListExpr(elems: Seq[Expression]): Expression = {
    elems.foldRight(Constant(OzAtom("nil")): Expression)((e, tail) => cons(e, tail))
  }

  /** Builds an Oz Cons pair */
  def cons(head: Expression, tail: Expression) = atPos(head) {
    Record(Constant(OzAtom("|")),
        Seq(withAutoFeature(head), withAutoFeature(tail)))
  }

  /** Builds an Oz #-tuple */
  def sharp(fields: Seq[Expression]) = {
    if (fields.isEmpty) Constant(OzAtom("#"))
    else {
      atPos(fields.head) {
        Record(Constant(OzAtom("#")), fields map withAutoFeature)
      }
    }
  }

  /** Equips an expression with an AutoFeature */
  def withAutoFeature(expr: Expression): RecordField = atPos(expr) {
    RecordField(AutoFeature(), expr)
  }
}
