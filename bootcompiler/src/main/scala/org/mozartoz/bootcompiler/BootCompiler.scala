package org.mozartoz.bootcompiler

import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import scala.collection.immutable.PagedSeq
import scala.collection.mutable.Buffer
import org.mozartoz.bootcompiler.symtab.Builtins
import org.mozartoz.bootcompiler.symtab.Program
import org.mozartoz.bootcompiler.fastparse.Parser
import com.oracle.truffle.api.source.Source

/**
 * Main interface, called from Java
 */
object BootCompiler {
  def buildMainProgram(source: Source, builtins: Builtins) = {
    val program = new Program(false, false, builtins)
    val statement = parseStatement(source, Set.empty)
    program.rawCode = statement
    program
  }

  def buildBaseEnvProgram(source: Source, builtins: Builtins) = {
    buildProgram(source, true, true, builtins)
  }

  def buildProgram(source: Source, isBase: Boolean, eagerLoad: Boolean, builtins: Builtins) = {
    val program = new Program(isBase, eagerLoad, builtins)
    val functor = parseExpression(source, Set.empty)
    ProgramBuilder.buildModuleProgram(program, functor)
    program
  }

  /**
   * Parses an Oz statement from a Source
   */
  private def parseStatement(source: Source, defines: Set[String]) =
    Parser.parseStatement(source, defines)

  /**
   * Parses an Oz expression from a Source
   */
  private def parseExpression(source: Source, defines: Set[String]) =
    Parser.parseExpression(source, defines)

  def checkCompileErrors(prog: Program, fileName: String) {
    if (prog.hasErrors) {
      Console.err.println(
        "There were errors while compiling %s" format fileName)
      for ((message, pos) <- prog.errors) {
        Console.err.println(
          "Error at %s\n".format(pos.toString) +
            message + "\n" + pos.getSource().getPath() + ":" + pos.getStartLine())
      }

      sys.exit(2)
    }
  }

}
