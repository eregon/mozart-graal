package org.mozartoz.bootcompiler

import java.io.File
import com.oracle.truffle.api.source.Source

trait ParserToVM {
  def createSource(path: String): Source
}
