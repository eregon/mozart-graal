package org.mozartoz.bootcompiler

import org.mozartoz.bootcompiler.ast.Node.Pos

trait SourceInterface {
  def getName(): String
  def getPath(): String
  def getCode(): String
  def createSection(charIndex: Int, length: Int): Pos
}
