package org.mozartoz.bootcompiler.fastparse

object Tokens {
  abstract class Token {
    var pB: Int = -1
    var pE: Int = -1

    def setPos(pB: Int, pE: Int): this.type = {
      this.pB = pB
      this.pE = pE
      this
    }
  }

  case class PreprocessorSwitch(switch: String, value: Boolean) extends Token
  case class PreprocessorDirective(directive: String) extends Token
  case class PreprocessorDirectiveWithArg(directive: String, arg: String) extends Token
}
