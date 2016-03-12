package org.mozartoz.bootcompiler
package symtab

/** Companion object for Builtin */
object Builtin {
  /** Parameter kind */
  object ParamKind extends Enumeration {
    val In, Out = Value
  }

  /** Parameter kind */
  type ParamKind = ParamKind.Value

  def create(moduleName: String, name: String, arity: Int) = new Builtin(moduleName, name, Nil, arity, None)
}

/** Builtin procedure of the host VM */
class Builtin(val moduleName: String, val name: String,
    val paramKinds: List[Builtin.ParamKind],
    val arity: Int,
    val inlineAs: Option[Int]) {

  override def toString() =
    moduleName + "." + (if (name.charAt(0).isLetter) name else "'" + name + "'")

  val inlineable = inlineAs.isDefined
  def inlineOpCode = inlineAs.get
}
