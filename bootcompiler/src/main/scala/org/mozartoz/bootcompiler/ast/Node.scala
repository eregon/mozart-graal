package org.mozartoz.bootcompiler
package ast

import com.oracle.truffle.api.source.SourceSection

/**
 * Node of an Oz AST
 *
 *  There are two important subclasses of `Node`:
 *  [[org.mozartz.bootcompiler.ast.Statement]] and
 *  [[org.mozartz.bootcompiler.ast.Expression]], with obvious meanings.
 */
abstract class Node extends Product {

  var section: SourceSection = null

  def setSourceSection(sourceSection: SourceSection): this.type = {
    this.section = sourceSection
    this
  }

  def posFrom(left: Node, right: Node): this.type = {
    val leftSection = left.section
    val rightSection = right.section
    if (leftSection != null && rightSection != null && leftSection.getSource == rightSection.getSource) {
      val len = rightSection.getCharEndIndex - leftSection.getCharIndex
      section = leftSection.getSource.createSection(leftSection.getCharIndex, len)
    }
    this
  }

  /** Copy the attributes of a node into this `Node`. */
  private[bootcompiler] def copyAttrs(tree: Node): this.type = {
    section = tree.section
    this
  }

  /**
   * Returns a pretty-printed representation of this `Node`
   *
   *  @param indent indentation to use when writing a line feed
   */
  def syntax(indent: String = ""): String

  override def toString = syntax()

  /**
   * Pre-order walk of the subtree rooted at this `Node`
   *
   *  At each node, the `handler` is called. If it returns `true`, then the
   *  walk dives into the children of this `Node`. Otherwise, it does not.
   *
   *  @param handler handler callback
   */
  def walkBreak(handler: Node => Boolean) {
    walkBreakInner(this, handler)
  }

  private def walkBreakInner(element: Any, handler: Node => Boolean) {
    element match {
      case node: Node => {
        if (handler(node)) {
          node.productIterator foreach { walkBreakInner(_, handler) }
        }
      }
      case seq: Seq[_] => seq foreach { walkBreakInner(_, handler) }
      case _           => ()
    }
  }

  /**
   * Pre-order walk of the subtree rooted at this `Node`
   *
   *  At each node, the `handler` is called.
   *
   *  @param handler handler callback
   */
  def walk[U](handler: Node => U) {
    walkInner(this, handler)
  }

  private def walkInner[U](element: Any, handler: Node => U) {
    element match {
      case node: Node => {
        handler(node)
        node.productIterator foreach { walkInner(_, handler) }
      }
      case seq: Seq[_] => seq foreach { walkInner(_, handler) }
      case _           => ()
    }
  }
}
