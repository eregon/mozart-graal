package org.mozartoz.bootcompiler

import org.mozartoz.bootcompiler.symtab.Builtins
import org.mozartoz.bootcompiler.symtab.Program
import org.mozartoz.bootcompiler.fastparse.Parser

/**
 * Main interface, called from Java
 */
object BootCompiler {

  type Source = SourceInterface

  var parserToVM: ParserToVM = null

  def registerParserToVM(parserToVM: ParserToVM) = {
    BootCompiler.parserToVM = parserToVM
  }

  def buildMainProgram(source: Source, builtins: Builtins, options: BootCompilerOptions) = {
    val program = new Program(false, false, builtins, options)
    val statement = parseStatement(source, Set.empty)
    program.rawCode = statement
    program
  }

  def buildBaseEnvProgram(source: Source, builtins: Builtins, options: BootCompilerOptions) = {
    buildProgram(source, true, true, builtins, options)
  }

  def buildProgram(source: Source, isBase: Boolean, eagerLoad: Boolean, builtins: Builtins, options: BootCompilerOptions) = {
    val program = new Program(isBase, eagerLoad, builtins, options)
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
            message + "\n" + parserToVM.sectionFileLine(pos))
      }

      sys.exit(2)
    }
  }

}
