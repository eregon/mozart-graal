package org.mozartoz.bootcompiler
package symtab

import scala.collection.mutable.{ Buffer, ArrayBuffer, HashMap }
import ast._
import util._
import com.oracle.truffle.api.source.SourceSection

/** Program to be compiled */
class Program(
    val isBaseEnvironment: Boolean = false,

    val eagerLoad: Boolean = false,

    /** Builtin manager */
    val builtins: Builtins = new Builtins) {

  /** Before flattening, abstract syntax tree of the whole program */
  var rawCode: Statement = null

  /** Returns `true` if the program is currently represented as a full AST */
  def isRawCode = rawCode ne null

  /** Map of base symbols (only in base environment mode) */
  val baseSymbols = new HashMap[String, Symbol]

  /**
   * The <Base> parameter of the top-level abstraction (only in normal mode)
   *  It contains the base environment
   */
  val baseEnvSymbol =
    if (isBaseEnvironment) NoSymbol
    else new Symbol("<Base>", synthetic = true, formal = true)

  /** The <Result> parameter of the top-level abstraction */
  val topLevelResultSymbol =
    new Symbol("<Result>", synthetic = true, formal = true)

  /** Compile errors */
  val errors = new ArrayBuffer[(String, SourceSection)]

  /** Returns `true` if at least one compile error was reported */
  def hasErrors = !errors.isEmpty

  /**
   * Reports a compile error
   *  @param message error message
   *  @param pos position of the error
   */
  def reportError(message: String, section: SourceSection = Node.noPos) {
    errors += ((message, section))
  }

  /**
   * Reports a compile error
   *  @param message error message
   *  @param positional positional that holds the position of the error
   */
  def reportError(message: String, positional: Node) {
    reportError(message, positional.pos)
  }

  /** Dumps the program on standard error */
  def dump() {
    println(rawCode)
  }
}
