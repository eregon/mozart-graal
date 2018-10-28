package org.mozartoz.bootcompiler

trait SourceInterface {
  def getName(): String
  def getPath(): String
  def getCode(): String
}
