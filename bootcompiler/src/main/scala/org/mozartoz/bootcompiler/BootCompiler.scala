package org.mozartoz.bootcompiler

import java.io.BufferedReader
import java.io.File
import java.io.FileReader

import scala.collection.immutable.PagedSeq
import scala.collection.mutable.Buffer
import scala.util.parsing.input.PagedSeqReader

import org.mozartoz.bootcompiler.parser.OzParser
import org.mozartoz.bootcompiler.symtab.Builtins
import org.mozartoz.bootcompiler.symtab.Program

/**
 * Main interface, called from Java
 */
object BootCompiler {

  def buildBaseEnvProgram(fileName: String, builtins: Builtins, baseDecls: Buffer[String]) = {
    val program = new Program(true, builtins, baseDecls)
    val functor = parseExpression(readerForFile(fileName), new File(fileName), Set.empty)
    ProgramBuilder.buildModuleProgram(program, functor)
    program
  }

  def buildMainProgram(fileName: String, builtins: Builtins, baseDecls: Buffer[String]) = {
    val program = new Program(false, builtins, baseDecls)
    val statement = parseStatement(readerForFile(fileName), new File(fileName), Set.empty)
    program.rawCode = statement
    program
  }

  def buildModuleProgram(fileName: String, builtins: Builtins, baseDecls: Buffer[String]) = {
    val program = new Program(false, builtins, baseDecls)
    val functor = parseExpression(readerForFile(fileName), new File(fileName), Set.empty)
    ProgramBuilder.buildModuleProgram(program, functor)
    program
  }

  /**
   * Parses an Oz statement from a reader
   *
   *  Upon lexical or syntactical error, displays a user-friendly error
   *  message on stderr and halts the program.
   *
   *  @param reader input reader
   *  @return The statement AST
   */
  private def parseStatement(reader: PagedSeqReader, file: File,
                             defines: Set[String]) =
    new ParserWrapper().parseStatement(reader, file, defines)

  /**
   * Parses an Oz expression from a reader
   *
   *  Upon lexical or syntactical error, displays a user-friendly error
   *  message on stderr and halts the program.
   *
   *  @param reader input reader
   *  @return The expression AST
   */
  private def parseExpression(reader: PagedSeqReader, file: File,
                              defines: Set[String]) =
    new ParserWrapper().parseExpression(reader, file, defines)

  /**
   * Utility wrapper for an [[org.mozartoz.bootcompiler.parser.OzParser]]
   *
   *  This wrapper provides user-directed error messages.
   */
  private class ParserWrapper {
    /** Underlying parser */
    private val parser = new OzParser()

    def parseStatement(reader: PagedSeqReader, file: File,
                       defines: Set[String]) =
      processResult(parser.parseStatement(reader, file, defines))

    def parseExpression(reader: PagedSeqReader, file: File,
                        defines: Set[String]) =
      processResult(parser.parseExpression(reader, file, defines))

    /**
     * Processes a parse result
     *
     *  Upon success, returns the underlying AST. Upon failure, displays a
     *  user-friendly error message on stderr and halts the program.
     *
     *  @param A type of AST
     *  @param result parse result to be processed
     *  @return the underlying AST, upon success only
     */
    private def processResult[A](result: parser.ParseResult[A]): A = {
      result match {
        case parser.Success(rawCode, _) =>
          rawCode

        case parser.NoSuccess(msg, next) =>
          Console.err.println(
            "Parse error at %s\n".format(next.pos.toString) +
              msg + "\n" +
              next.pos.longString)
          sys.exit(2)
      }
    }
  }

  /**
   * Builds a [[scala.util.parsing.input.PagedSeqReader]] for a file
   *
   *  @param fileName name of the file to be read
   */
  private def readerForFile(fileName: String) = {
    new PagedSeqReader(PagedSeq.fromReader(
      new BufferedReader(new FileReader(fileName))))
  }

  def checkCompileErrors(prog: Program, fileName: String) {
    if (prog.hasErrors) {
      Console.err.println(
        "There were errors while compiling %s" format fileName)
      for ((message, pos) <- prog.errors) {
        Console.err.println(
          "Error at %s\n".format(pos.toString) +
            message + "\n" +
            pos.longString)
      }

      sys.exit(2)
    }
  }

}
