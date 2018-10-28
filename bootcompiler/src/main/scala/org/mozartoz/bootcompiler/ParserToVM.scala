package org.mozartoz.bootcompiler

import java.io.File

import org.mozartoz.bootcompiler.BootCompiler.Source
import org.mozartoz.bootcompiler.ast.Node.Pos

trait ParserToVM {
  def createSource(path: String): Source
  def sectionFileLine(section: Pos): String
  def extendSection(left: Pos, right: Pos): Pos
}
