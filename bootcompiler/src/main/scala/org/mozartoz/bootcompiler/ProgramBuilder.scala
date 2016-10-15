package org.mozartoz.bootcompiler

import ast._
import symtab._
import oz._
import transform._

/** Provides a method for building a program out of its parts */
object ProgramBuilder extends TreeDSL with TransformUtils {
  val treeCopy = new TreeCopier

  /** Builds a program that defines a regular functor
   *
   *  Given a functor expression <functor>, the generated procedure is
   *  straightforward:
   *  {{{
   *  proc {$ <Base> ?<Result>}
   *     <Result> = <functor>
   *  end
   *  }}}
   */
  def buildModuleProgram(prog: Program, functor: Expression) {
    prog.rawCode = {
      prog.topLevelResultSymbol.copyAttrs(functor) === functor
    }
  }
}
