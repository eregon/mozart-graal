package org.mozartoz.bootcompiler.fastparse

import java.io.File
import org.mozartoz.bootcompiler.fastparse.Tokens.PreprocessorDirective
import org.mozartoz.bootcompiler.fastparse.Tokens.PreprocessorDirectiveWithArg
import org.mozartoz.bootcompiler.fastparse.Tokens.Token
import com.oracle.truffle.api.source.Source
import fastparse.core.Parsed
import scala.collection.mutable.ArrayBuffer

object Preprocessor {
  case class SourceMap(fromOffset: Int, sourceOffset: Int, source: Source, toOffset: Int = 0) {
    override def toString = fromOffset + "-" + toOffset + " " + sourceOffset + " @ " + source.getName

    def in(pos: Int) = {
      fromOffset <= pos && pos < toOffset
    }

    def length = toOffset - fromOffset
  }

  def preprocess(source: Source): (String, Seq[SourceMap]) = {
    val input = source.getCharacters.toString
    val tokens = Parser.t(Parser.tokens(input), source.getPath)

    var defines: Set[String] = Set()
    var skipDepth = 0
    var offset = 0
    val buffer = new StringBuilder
    val sourceMap: ArrayBuffer[SourceMap] = new ArrayBuffer[SourceMap]

    def capture(until: Int) {
      buffer ++= input.substring(offset, until)
    }

    def restartAt(pos: Int) {
      offset = pos
      recordPosition
    }

    def ignore(token: Token) {
      capture(token.pB)
      restartAt(token.pE)
    }

    def recordPosition {
      sourceMap += SourceMap(buffer.length, offset, source)
    }

    recordPosition
    for (elem <- tokens) {
      if (elem.isInstanceOf[Token]) {
        val token = elem.asInstanceOf[Token]

        if (skipDepth > 0) {
          token match {
            case PreprocessorDirectiveWithArg("ifdef" | "ifndef", _) =>
              skipDepth += 1

            case PreprocessorDirective("else" | "endif") if skipDepth == 1 =>
              skipDepth = 0
              restartAt(token.pE)

            case PreprocessorDirective("endif") =>
              skipDepth -= 1
          }
        } else {
          token match {
            case PreprocessorDirectiveWithArg("define", name) =>
              defines += name
              ignore(token)

            case PreprocessorDirectiveWithArg("undef", name) =>
              defines -= name
              ignore(token)

            case PreprocessorDirectiveWithArg("ifdef", name) =>
              if (defines contains name) {
                // next
              } else {
                skipDepth = 1
              }
              ignore(token)

            case PreprocessorDirectiveWithArg("ifndef", name) =>
              if (!(defines contains name)) {
                // next
              } else {
                skipDepth = 1
              }
              ignore(token)

            case PreprocessorDirective("else") =>
              skipDepth = 1
              ignore(token)

            case PreprocessorDirective("endif") =>
              ignore(token)

            case PreprocessorDirectiveWithArg("insert", fileName) =>
              val file = resolve(new File(source.getPath), fileName)
              capture(token.pB)

              val subSource = Source.newBuilder(file).name(file.getName).mimeType("application/x-oz").build
              val (out, map) = preprocess(subSource)
              sourceMap ++= map.map {
                case SourceMap(fromOffset, sourceOffset, source, toOffset) =>
                  SourceMap(buffer.length + fromOffset, sourceOffset, source)
              }
              buffer ++= out
              restartAt(token.pE)
          }
        }
      }
    }
    capture(input.length)

    for (i <- 0 until sourceMap.size - 1) {
      sourceMap(i) = sourceMap(i).copy(toOffset = sourceMap(i + 1).fromOffset)
    }
    sourceMap(sourceMap.size - 1) = sourceMap.last.copy(toOffset = Int.MaxValue)
    (buffer.toString, sourceMap)
  }

  def resolve(currentFile: File, fileName: String) = {
    val file0 = if (new File(fileName).isAbsolute()) {
      new File(fileName)
    } else {
      new File(currentFile.getParentFile, fileName)
    }

    val file = {
      if (file0.exists()) file0
      else {
        val altFile = new File(currentFile.getParentFile, fileName + ".oz")
        if (altFile.exists()) altFile
        else file0
      }
    }
    file
  }
}
