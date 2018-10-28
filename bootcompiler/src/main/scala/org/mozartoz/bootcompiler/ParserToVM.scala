package org.mozartoz.bootcompiler

import org.mozartoz.bootcompiler.BootCompiler.Source
import org.mozartoz.bootcompiler.ast.Node.Pos

trait ParserToVM {
  def createSource(path: String): Source
  def sectionFileLine(section: Pos): String
}
