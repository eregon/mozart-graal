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

  def create(moduleName: String, name: String, arity: Int) = new Builtin(moduleName, name, arity)
}

/** Builtin procedure of the host VM */
class Builtin(val moduleName: String, val name: String, val arity: Int) {
  override def toString() =
    moduleName + "." + (if (name.charAt(0).isLetter) name else "'" + name + "'")
}
