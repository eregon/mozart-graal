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

  def buildBaseEnvProgram(source: Source, builtins: Builtins, baseDecls: Buffer[String]) = {
    val program = new Program(true, builtins, baseDecls)
    val functor = parseExpression(source, Set.empty)
    ProgramBuilder.buildModuleProgram(program, functor)
    program
  }

  def buildMainProgram(source: Source, builtins: Builtins, baseDecls: Buffer[String]) = {
    val program = new Program(false, builtins, baseDecls)
    val statement = parseStatement(source, Set.empty)
    program.rawCode = statement
    program
  }

  def buildModuleProgram(source: Source, builtins: Builtins, baseDecls: Buffer[String]) = {
    val program = new Program(false, builtins, baseDecls)
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
            message + "\n" + pos.getShortDescription)
      }

      sys.exit(2)
    }
  }

}
