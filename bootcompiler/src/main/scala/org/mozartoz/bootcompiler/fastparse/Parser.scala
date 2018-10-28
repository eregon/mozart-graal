package org.mozartoz.bootcompiler.fastparse

import fastparse.WhitespaceApi
import java.io.File

import org.mozartoz.bootcompiler.ast._
import org.mozartoz.bootcompiler.ast.Node.Pos
import org.mozartoz.bootcompiler.oz._
import org.mozartoz.bootcompiler.fastparse.Preprocessor.SourceMap
import org.mozartoz.bootcompiler.fastparse.Tokens._
import org.mozartoz.bootcompiler.BootCompiler
import org.mozartoz.bootcompiler.BootCompiler.Source

import scala.collection.mutable.ListBuffer

object Whitespace {
  import fastparse.all._

  val w = P(CharPred(ch => ch <= ' ')
    | "/*" ~/ ((!"*/" ~ AnyChar).rep ~ "*/").opaque("unclosed comment")
    | "%" ~/ CharsWhile(_ != '\n').?)

  val whitespace = w.rep.opaque("whitespace")

  val whitespaceWrapper = WhitespaceApi.Wrapper {
    NoTrace(whitespace)
  }
}

// Compatibility API with parser combinators
object CompatAPI {
  import fastparse.all.Parser
  import fastparse.all.P
  import fastparse.all.parserApi
  import fastparse.all.Index

  class ParserMapper[T](val p: Parser[T]) {
    def ^^[U](f: T => U): Parser[U] = p.map { x => f(x) }

    def ^^^[U](f: => U): Parser[U] = p.map { x => f }

    def ^[N <: Node](map: T => Pos => N): P[N] = P(Index ~ p ~ Index).map {
      case (pB, t, pE) => map(t)(Parser.createSection(pB, pE))
    }
  }

  implicit def parser2mapper[T](p: Parser[T]) = new ParserMapper(p)

  implicit def transform2[T, U, R](f: (T, U) => R): ((T, U)) => R = {
    case (a, b) => f(a, b)
  }
  implicit def transform3[T, U, V, R](f: (T, U, V) => R): ((T, U, V)) => R = {
    case (a, b, c) => f(a, b, c)
  }
}

object Lexer {
  import CompatAPI._
  import fastparse.all._

  val reserved = Seq(
    "andthen", "at", "attr", "case", "catch", "choice",
    "class", "cond", "declare", "define", "dis", "div",
    "do", "else", "elsecase", "elseif", "elseof", "end",
    "export", "fail", "false", "feat", "finally", "for", "from",
    "fun", "functor", "if", "import", "in", "local",
    "lock", "meth", "mod", "not", "of", "or", "orelse",
    "prepare", "proc", "prop", "raise", "require",
    "self", "skip", "then", "thread", "true", "try",
    "unit")

  val delimiters = Seq(
    "(", ")", "[", "]", "{", "}",
    "|", "#", ":", "...", "=", ".", ":=", "^", "[]", "$",
    "!", "_", "~", "+", "-", "*", "/", "@", "<-",
    ",", "!!", "<=", "==", "\\=", "<", "=<", ">",
    ">=", "=:", "\\=:", "<:", "=<:", ">:", ">=:", "::", ":::",
    "..")

  val isOpChar = CharIn("|#:=.^!")
  val isCompOpChar = CharIn("=-:")

  val `<` = "<" ~ !isCompOpChar
  val `=<` = "=<" ~ !":"
  val `>` = ">" ~ !isCompOpChar
  val `>=` = ">=" ~ !":"
  val `=` = "=" ~ !CharIn("=:")

  val `:` = ":" ~ !":"

  val `.` = "." ~ !"."
  val `..` = ".." ~ !"."
  val `...` = "..." ~ !"."

  private def computeDelims: Parser[String] = {
    /* construct parser for delimiters by |'ing together the parsers for the
     * individual delimiters, starting with the longest one -- otherwise a
     * delimiter D will never be matched if there is another delimiter that is
     * a prefix of D
     */
    val d = new Array[String](delimiters.size)
    delimiters.copyToArray(d, 0)
    scala.util.Sorting.quickSort(d)
    d.map { s => s.! }.foldRight(
      Fail.opaque("no matching delimiter"): Parser[String])((x, y) => y | x)
  }

  val delim: Parser[String] = computeDelims

  val whitespace = Whitespace.whitespace

  val letter = CharPred(_.isLetter)
  val lowerCaseLetter = CharPred(_.isLower)
  val upperCaseLetter = CharPred(_.isUpper)
  val digit = CharIn('0' to '9')
  val hexDigit = CharIn(('0' to '9') ++ ('A' to 'F') ++ ('a' to 'f'))
  val octalDigit = CharIn('0' to '7').!
  val binDigit = CharIn("01").!
  def isIdentChar(ch: Char) = { ch.isLetter || ch.isDigit || ch == '_' }
  val identCharPred = CharPred(isIdentChar)

  val identChars = CharsWhile(isIdentChar(_)).?
  val keyword = P(StringIn(reserved: _*) ~ !identCharPred)
  def KW(s: String) = P(s ~ !identCharPred)(sourcecode.Name(s"`$s`"))

  val `andthen` = KW("andthen")
  val `at` = KW("at")
  val `attr` = KW("attr")
  val `case` = KW("case")
  val `catch` = KW("catch")
  val `choice` = KW("choice")
  val `class` = KW("class")
  val `cond` = KW("cond")
  val `declare` = KW("declare")
  val `define` = KW("define")
  val `dis` = KW("dis")
  val `div` = KW("div")
  val `do` = KW("do")
  val `else` = KW("else")
  val `elsecase` = KW("elsecase")
  val `elseif` = KW("elseif")
  val `elseof` = KW("elseof")
  val `end` = KW("end")
  val `export` = KW("export")
  val `fail` = KW("fail")
  val `false` = KW("false")
  val `feat` = KW("feat")
  val `finally` = KW("finally")
  val `for` = KW("for")
  val `from` = KW("from")
  val `fun` = KW("fun")
  val `functor` = KW("functor")
  val `if` = KW("if")
  val `import` = KW("import")
  val `in` = KW("in")
  val `local` = KW("local")
  val `lock` = KW("lock")
  val `meth` = KW("meth")
  val `mod` = KW("mod")
  val `not` = KW("not")
  val `of` = KW("of")
  val `or` = KW("or")
  val `orelse` = KW("orelse")
  val `prepare` = KW("prepare")
  val `proc` = KW("proc")
  val `prop` = KW("prop")
  val `raise` = KW("raise")
  val `require` = KW("require")
  val `self` = KW("self")
  val `skip` = KW("skip")
  val `then` = KW("then")
  val `thread` = KW("thread")
  val `true` = KW("true")
  val `try` = KW("try")
  val `unit` = KW("unit")

  val escapeChar: P[Char] = (
    CharIn("'\"`&\\").! ^^ { str => str.charAt(0) }
    | "a".! ^^^ '\u0007'
    | "b".! ^^^ '\u0008'
    | "t".! ^^^ '\u0009'
    | "n".! ^^^ '\u000A'
    | "v".! ^^^ '\u000B'
    | "f".! ^^^ '\u000C'
    | "r".! ^^^ '\u000D')

  def latin1char(codePoint: Int) = codePoint.toChar

  val pseudoChar: P[Char] = "\\" ~ (
    octalDigit.rep(min = 3, max = 3).! ^^ {
      digits => latin1char(java.lang.Integer.parseInt(digits, 8))
    }
    | CharIn("xX") ~ hexDigit.rep(min = 2, max = 2).! ^^ {
      digits => latin1char(java.lang.Integer.parseInt(digits, 8))
    }
    | escapeChar)

  def inQuoteChar(quoteChar: Char): P[Char] =
    CharPred(ch => ch != '\\' && ch != quoteChar).!.map { _.charAt(0) } | pseudoChar

  def quoted(quoteChar: String): P[String] =
    quoteChar ~ inQuoteChar(quoteChar.charAt(0)).rep.map { _ mkString "" } ~ quoteChar

  val identifier: P[String] = P(
    (upperCaseLetter ~ identChars ~ !identCharPred).!
      | quoted("`"))

  val atomLiteral: P[String] = P(
    (!keyword ~ (lowerCaseLetter ~ identChars).! ~ !identCharPred)
      | quoted("'"))

  val stringLiteral: P[String] = quoted("\"")

  val floatLiteral: P[Double] = P(
    floatLiteralBase
      | "~" ~ floatLiteralBase ^^ (x => -x))

  val afterFloatDot = P(CharIn("eE") ~ ("~" | digit) | digit)

  val floatLiteralBase: P[Double] =
    P((digit.rep(1).! ~ `.` ~ digit.rep.! ~ !(identCharPred)).map {
      case (int, frac) => (int + "." + frac).toDouble
    } |
      (digit.rep(1).! ~ `.` ~ digit.rep.! ~ CharIn("eE") ~ floatExponentBase).map {
        case (int, frac, exp) => { (int + "." + frac + "e" + exp).toDouble }
      })

  val floatExponentBase: P[String] = P(
    digit.rep(1).!
      | "~" ~ digit.rep(1).! ^^ (x => "-" + x))

  val integerLiteral: P[Long] = P(
    integerLiteralBase
      | "~" ~ integerLiteralBase ^^ (x => -x)) ~ !("." ~ afterFloatDot) // Disallow int.X but allow int..int (for I in 1..10 do)

  def integerLiteralBase: P[Long] = P(
    "0" ~ CharIn("xX") ~ hexDigit.rep(1).! ^^ {
      digits => java.lang.Long.parseLong(digits, 16)
    }
      | "0" ~ CharIn("bB") ~ binDigit.rep(1).! ^^ {
        digits => java.lang.Long.parseLong(digits, 2)
      }
      | "0" ~ octalDigit.rep(1).! ^^ {
        digits => java.lang.Long.parseLong(digits, 8)
      }
      | digit.rep(1).! ^^ (chars => chars.toLong)) ~ !digit

  val charLiteral: P[Char] = "&" ~ (
    (!"\\" ~ AnyChar).! ^^ { str => str.charAt(0) } |
    pseudoChar)

  val preprocessorDirective = P(Index ~ "\\" ~/ preprocessorDirectiveInner ~ Index).map {
    case (pB, directive, pE) => directive.setPos(pB, pE)
  }

  val preprocessorDirectiveInner: P[Token] = P(
    "switch" ~ whitespace ~ switchArgs
      | ("showSwitches" | "pushSwitches" | "popSwitches" | "localSwitches" | "else" | "endif").! ^^ PreprocessorDirective
      | (("define" | "undef" | "ifdef" | "ifndef").! ~ whitespace ~ preprocessorVar.!) ^^ PreprocessorDirectiveWithArg.tupled
      | ("insert".! ~ whitespace ~ preprocessorFileName) ^^ PreprocessorDirectiveWithArg.tupled)

  val switchArgs = P(
    (P("+") ^^^ true | P("-") ^^^ false) ~ whitespace.? ~
      (lowerCaseLetter ~ identChars).!) ^^ { case (value, switch) => PreprocessorSwitch(switch, value) }

  val preprocessorVar = upperCaseLetter ~ identChars

  val preprocessorFileName = (
    (letter | digit | CharIn("/_~.-")).rep(1).!
    | "'" ~ CharsWhile(_ != '\'').! ~ "'")

  private val unitTrueFalse = Set("unit", "true", "false")

  val token: Parser[Any] = P(
    keyword.! // ^^ Keyword
      | identifier
      | atomLiteral
      | floatLiteral
      | integerLiteral
      | charLiteral
      | stringLiteral
      | delim
      | "?" ~ token
      | preprocessorDirective
      | Fail.opaque("illegal character"))

  // Tokens

  val atomLit: P[String] = atomLiteral
  val stringLit: P[String] = stringLiteral
  val charLit: P[Long] = charLiteral.map { ch => ch.toLong }
  val intLit: P[Long] = integerLiteral
  val floatLit: P[Double] = floatLiteral
}

object Parser {
  import CompatAPI._
  import fastparse.noApi._
  import Whitespace.whitespaceWrapper._
  import Lexer._

  // PARSER

  // Helpers

  var map: Seq[SourceMap] = null
  var i = 0

  def getSourceMap(pos: Int) = {
    while (pos < map(i).fromOffset) {
      i -= 1
    }
    while (pos >= map(i).toOffset) {
      i += 1
    }
    map(i)
  }

  def createSection(pB: Int, pE: Int) = {
    val startMap = getSourceMap(pB)
    val start = (pB - startMap.fromOffset) + startMap.sourceOffset
    val source = startMap.source

    var len = 0
    if (pE <= map(i).toOffset) {
      len = pE - pB
    } else {
      len = map(i).toOffset - pB
      i += 1
      while (!map(i).in(pE)) {
        val m = map(i)
        if (m.source eq source) {
          len += m.length
        }
        i += 1
      }
      if (map(i).source eq source) {
        len += (pE - map(i).fromOffset)
      }
    }
    Node.Pos(source, start, len)
  }

  def pos[T, U <: Phrase](p: P[T])(map: Pos => U) = P(Index ~~ p ~~ Index).map {
    case (pB, node, pE) => map(createSection(pB, pE))
  }

  def pos1[T, U <: Node](p: P[T])(map: (T, Pos) => U) = P(Index ~~ p ~~ Index).map {
    case (pB, node, pE) => map(node, createSection(pB, pE))
  }

  def pos2[A, B, U <: Node](p: P[(A, B)])(map: (A, B, Pos) => U) = P(Index ~~ p ~~ Index).map {
    case (pB, (a, b), pE) => map(a, b, createSection(pB, pE))
  }

  def pos3[A, B, C, U <: Node](p: P[(A, B, C)])(map: (A, B, C, Pos) => U) = P(Index ~~ p ~~ Index).map {
    case (pB, (a, b, c), pE) => map(a, b, c, createSection(pB, pE))
  }

  def pos4[A, B, C, D, U <: Node](p: P[(A, B, C, D)])(map: (A, B, C, D, Pos) => U) = P(Index ~~ p ~~ Index).map {
    case (pB, (a, b, c, d), pE) => map(a, b, c, d, createSection(pB, pE))
  }

  /** Extracts the name of a RawVariable, or "" if it is not */
  private def nameOf(expression: Phrase) = expression match {
    case RawVariable(name) => name
    case _                 => ""
  }

  /** Extracts the name of an optional raw variable  */
  private def nameOf(expression: Option[Phrase]): Option[VariableOrRaw] = expression match {
    case Some(v @ RawVariable(_)) => Some(v)
    case _                        => None
  }

  private val generatedIdentCounter = new org.mozartoz.bootcompiler.util.Counter
  private def generateExcIdent() = "<exc$" + generatedIdentCounter.next() + ">"
  private def generateParamIdent() = "<arg$" + generatedIdentCounter.next() + ">"

  def leftAssoc(p: P[Phrase], op: P[String], binOp: (Phrase, String, Phrase) => Pos => Phrase) =
    pos2(p ~ (op ~ p).rep) {
      case (lhs, ops, pos) => ops.foldLeft(lhs) { case (lhs, (op, rhs)) => binOp(lhs, op, rhs)(pos) }
    }

  def unaryOp(p: P[(String, Phrase)]) = p ^ UnaryOpPhrase.apply

  // Constants

  val integerConst = (intLit | charLit) ^^ OzInt

  val floatConst: P[OzFloat] = floatLit ^^ (value => OzFloat(value))

  val atomConst: P[OzAtom] = atomLit ^^ OzAtom

  val literalConst: P[OzLiteral] = P(
    `true` ^^^ True() | `false` ^^^ False() | `unit` ^^^ UnitVal()
      | atomConst)

  val featureConst: P[OzFeature] = integerConst | literalConst

  val stringConst: P[OzValue] =
    stringLit ^^ (chars => OzList(chars map (c => OzInt(c.toInt))))

  val featureNoVar = featureConst ^ Constant.apply

  // Trivial expressions

  val variable = P("?".? ~ identifier.!) ^ RawVariable.apply

  val escapedVariable: P[EscapedVariable] =
    "!" ~ (variable ^ EscapedVariable.apply)

  val wildcardExpr: P[UnboundExpression] =
    pos(("_").!) { pos => UnboundExpression()(pos) }

  val nestingMarker: P[NestingMarker] =
    pos(P("$")) { pos => NestingMarker()(pos) }

  val integerConstExpr = integerConst ^ Constant.apply
  val floatConstExpr = floatConst ^ Constant.apply
  val atomConstExpr = atomConst ^ Constant.apply
  val literalConstExpr = literalConst ^ Constant.apply
  val stringConstExpr = stringConst ^ Constant.apply
  val selfExpr = pos(P(`self`)) { pos => Self()(pos) }

  val attrOrFeat: P[Phrase] =
    P(variable | escapedVariable | integerConstExpr | literalConstExpr)

  val trivialExpression: P[Phrase] =
    P(floatConstExpr | attrOrFeat | wildcardExpr | nestingMarker | selfExpr)

  // Record expressions

  val recordExpression: P[Phrase] =
    P(recordLabel ~~ "(" ~/ recordField.rep ~ ")") ^ RecordPhrase.apply

  val recordLabel: P[Phrase] = atomConstExpr | variable

  val recordField = P(optFeature ~ expression) ^ RecordFieldPhrase.apply

  val feature: P[Phrase] = (featureNoVar | variable)

  val optFeature: P[Phrase] =
    pos1((feature ~ `:`).?) { case (f, p) => f.getOrElse(AutoFeature()(p)) }

  // List expressions

  val listExpression = P("[" ~ expression.rep(1) ~ "]") ^ ListPhrase.apply

  // Skip

  val skipStatement: P[Phrase] = pos(P(`skip`)) { p => SkipStatement()(p) }

  // Expressions

  // Operations with precedence
  val atPhrase: P[Phrase] = P(
    `local` ~/ inPhrase ~ `end`
      | "(" ~/ inPhrase ~ ")"
      | procExpression
      | funExpression
      | callExpression
      | ifExpression
      | caseExpression
      | threadExpression
      | lockExpression
      | tryExpression
      | raiseExpression
      | classExpression
      | functorExpression
      | listExpression // [
      | recordExpression // label(
      | trivialExpression
      | stringConstExpr
      | forStatement
      | skipStatement)
  val lvlD: P[Phrase] = P((("@" | "!!").! ~/ lvlD) ^ UnaryOpPhrase.apply | atPhrase)
  val lvlC: P[Phrase] = P(leftAssoc(lvlD, `.`.!.~/, BinaryOpPhrase.apply))
  val lvlB: P[Phrase] = P(("~".! ~/ lvlB) ^ UnaryOpPhrase.apply | lvlC)
  val lvlA: P[Phrase] = P(lvlB ~ ("," ~/ lvlA).?).map {
    case (lhs, Some(rhs)) => BinaryOpPhrase(lhs, ",", rhs)(Node.extend(lhs, rhs))
    case (lhs, None)      => lhs
  }
  val lvl9: P[Phrase] = leftAssoc(lvlA, ("*" | "/" | `div` | `mod`).!.~/, BinaryOpPhrase.apply)
  val lvl8: P[Phrase] = leftAssoc(lvl9, ("+" | "-").!.~/, BinaryOpPhrase.apply)
  val lvl7: P[Phrase] = P(lvl8 ~ ("#" ~/ lvl8).rep).map {
    case (first, rest) if !rest.isEmpty => sharp(first +: rest)
    case (first, _)                     => first
  }
  val lvl6: P[Phrase] = P(lvl7 ~ ("|" ~/ lvl6).?).map {
    case (lhs, Some(rhs)) => cons(lhs, rhs)
    case (lhs, None)      => lhs
  }
  val lvl5: P[Phrase] = lvl6 // Unsupported X::Y, X:::Y
  val comparisonOperator = ("==" | "\\=" | `<` | `=<` | `>` | `>=`)
  val lvl4: P[Phrase] = (lvl5 ~ (comparisonOperator.! ~/ lvl5).?).map {
    case (lhs, Some((op, rhs))) => BinaryOpPhrase(lhs, op, rhs)(Node.extend(lhs, rhs))
    case (lhs, None)            => lhs
  }
  val lvl3: P[Phrase] = P(lvl4 ~ (`andthen` ~/ lvl3).?).map {
    case (lhs, Some(rhs)) => ShortCircuitBinaryOpPhrase(lhs, "andthen", rhs)(Node.extend(lhs, rhs))
    case (lhs, None)      => lhs
  }
  val lvl2: P[Phrase] = P(lvl3 ~ (`orelse` ~/ lvl2).?).map {
    case (lhs, Some(rhs)) => ShortCircuitBinaryOpPhrase(lhs, "orelse", rhs)(Node.extend(lhs, rhs))
    case (lhs, None)      => lhs
  }
  val lvl1: P[Phrase] = P(lvl2 ~ (("<-" | ":=").! ~/ lvl1).?).map {
    case (BinaryOpPhrase(l, ".", r), Some((op, rhs))) => DotAssignPhrase(l, r, rhs)(Node.extend(l, rhs))
    case (lhs, Some((op, rhs)))                       => BinaryOpPhrase(lhs, op, rhs)(Node.extend(lhs, rhs))
    case (lhs, None)                                  => lhs
  }
  val lvl0: P[Phrase] = P(lvl1 ~ ("=" ~/ lvl0).?).map {
    case (lhs, Some(rhs)) => nameBindRHS(BindPhrase(lhs, rhs)(Node.extend(lhs, rhs)))
    case (lhs, None)      => lhs
  }
  val expression: P[Phrase] = lvl0

  val phrase: P[Phrase] = lvl0.rep(1).map {
    case Seq(node) => node
    case many      => CompoundPhrase(many)(Node.extend(many))
  }

  // Declarations

  val inPhrase = P(phrase ~ (`in` ~/ phrase).?).map {
    case (lhs, Some(rhs)) => RawLocalPhrase(lhs, rhs)(Node.extend(lhs, rhs))
    case (lhs, None)      => lhs
  }

  // Procedure and function definition

  val formalArg = P(pattern)
  val formalArgs = P(formalArg.rep)
  val procFlags = atomLit.rep

  val dollarOrExpr: P[Option[Phrase]] = {
    P("$") ^^^ None |
      expression.map { expr => Some(expr) }
  }

  val procExpression: P[Phrase] =
    pos4(`proc` ~/ procFlags ~ "{" ~ dollarOrExpr ~/ formalArgs ~ "}" ~/ inPhrase ~ `end`) {
      case (flags, name, args0, body0, pos) =>
        val (args, body) = postProcessArgsAndBody(args0, body0)
        val proc = ProcPhrase(nameOf(name), args, body, flags)(pos)
        name match {
          case Some(expr) => BindPhrase(expr, proc)(expr)
          case None       => proc
        }
    }

  val funExpression: P[Phrase] =
    pos4(`fun` ~/ procFlags ~ "{" ~ dollarOrExpr ~/ formalArgs ~ "}" ~/ inPhrase ~ `end`) {
      case (flags, name, args0, body0, pos) =>
        val (args, body) = postProcessArgsAndBody(args0, body0)
        val fun = FunPhrase(nameOf(name), args, body, flags)(pos)
        name match {
          case Some(expr) => BindPhrase(expr, fun)(expr)
          case None       => fun
        }
    }

  // Call

  val callExpression = P("{" ~ expression ~ actualArgs ~ "}") ^ CallPhrase.apply

  val actualArgs = P(expression.rep)

  // If then else end

  val ifExpression: P[Phrase] = P(`if` ~/ innerIfExpression ~ `end`)

  val innerIfExpression: P[Phrase] =
    P(expression ~ `then` ~/ inPhrase ~ elseExpression) ^ IfPhrase.apply

  val elseExpression: P[Phrase] = P(
    `else` ~/ inPhrase
      | `elseif` ~/ innerIfExpression
      | `elsecase` ~/ innerCaseExpression
      | pos(Pass) { pos => NoElsePhrase()(pos) })

  // case of

  val caseExpression = P(`case` ~/ innerCaseExpression ~ `end`)

  val innerCaseExpression: P[Phrase] =
    P(expression ~ `of` ~/ caseExpressionClauses ~ elseExpression) ^ MatchPhrase.apply

  val caseExpressionClauses: P[Seq[MatchPhraseClause]] =
    P(caseExpressionClause.rep(min = 1, sep = ("[]" | `elseof`).~/))

  val caseExpressionClause: P[MatchPhraseClause] =
    (P(pattern) ~ (`andthen` ~/ expression).? ~ (`then` ~/ inPhrase)) ^ MatchPhraseClause.apply

  // Pattern

  val pattern: P[Phrase] = P(pattern1 ~ ("=" ~/ pattern).?).map {
    case (lhs, Some(rhs)) => PatternConjunctionPhrase(Seq(lhs, rhs))(Node.extend(lhs, rhs))
    case (lhs, None)      => lhs
  }

  val pattern1: P[Phrase] = P(pattern2 ~ ("|" ~/ pattern1).?).map {
    case (lhs, Some(rhs)) => cons(lhs, rhs)
    case (lhs, None)      => lhs
  }

  val pattern2: P[Phrase] = P(pattern3 ~ ("#" ~/ pattern3).rep).map {
    case (first, rest) if !rest.isEmpty => sharp(first +: rest)
    case (first, _)                     => first
  }

  val pattern3: P[Phrase] = P(
    pos3(recordLabel ~ "(" ~/ patternRecordField.rep ~ `...`.!.? ~ ")") {
      case (label, fields, Some(openMarker), pos) => OpenRecordPatternPhrase(label, fields)(pos)
      case (label, fields, None, pos)             => RecordPhrase(label, fields)(pos)
    }
      | "[" ~/ pattern.rep(1) ~ "]" ^^ exprListToListExpr
      | floatConstExpr | integerConstExpr | literalConstExpr | stringConstExpr
      | variable
      | escapedVariable
      | wildcardExpr
      | "(" ~/ pattern ~ ")")

  val optFeatureNoVar: P[Phrase] =
    pos1((featureNoVar ~ `:`).?) { case (f, p) => f.getOrElse(AutoFeature()(p)) }

  val patternRecordField: P[RecordFieldPhrase] =
    (optFeatureNoVar ~ pattern) ^ RecordFieldPhrase.apply

  // Thread

  val threadExpression =
    P(`thread` ~/ inPhrase ~ `end`) ^ ThreadPhrase.apply

  // Lock

  val lockExpression =
    pos2(`lock` ~/ NoCut(expression ~ `then`).? ~ inPhrase ~ `end`) {
      case (Some(lock), body, pos) => LockPhrase(lock, body)(pos)
      case (None, body, pos)       => LockObjectPhrase(body)(pos)
    }

  // Try

  val tryExpression =
    pos3(`try` ~/ inPhrase ~ (`catch` ~/ caseExpressionClauses).? ~ (`finally` ~/ inPhrase).? ~ `end`) {
      case (body, optCatchClauses, optFinallyBody, pos) =>
        val tryCatch = optCatchClauses match {
          case None => body
          case Some(catchClauses) =>
            val excVar = RawVariable(generateExcIdent())(body)
            TryPhrase(body, excVar,
              MatchPhrase(excVar, catchClauses, RaisePhrase(excVar)(excVar))(Node.extend(catchClauses)))(pos)
        }

        optFinallyBody match {
          case None              => tryCatch
          case Some(finallyBody) => TryFinallyPhrase(tryCatch, finallyBody)(pos)
        }
    }

  // Raise

  val raiseExpression =
    (`raise` ~/ inPhrase ~ `end`) ^ RaisePhrase.apply |
      pos(`fail`) { p => FailStatement()(p) }

  // For loops

  val forListGenerator =
    P(expression ~ (`..` ~/ expression).?) ^^ {
      case (start, Some(end)) =>
        ForPhrase(start, end, null)(Node.extend(start, end))
      case (expr, None) => expr
    }

  val forStatement =
    pos3(`for` ~/ formalArg ~ `in` ~/ forListGenerator ~ `do` ~/ inPhrase ~ `end`) {
      case (arg0, listExpr, body0, pos) =>
        val (args, body) = postProcessArgsAndBody(Seq(arg0), body0)
        val forProc = ProcPhrase(None, args, body, Nil)(pos)
        listExpr match {
          case ForPhrase(from, to, _) => ForPhrase(from, to, forProc)(pos)
          case _                      => CallPhrase(RawVariable("ForAll")(forProc), Seq(listExpr, forProc))(pos)
        }
    }

  // Functor

  val functorExpression =
    pos2(P(`functor` ~/ exprOrImplDollar ~ functorClause.rep ~ `end`)) {
      case (None, parts, pos) => buildFunctor(parts)(pos)
      case (Some(lhs), parts, pos) =>
        BindPhrase(lhs, buildFunctor(parts)(pos).copy(name = nameOf(lhs))(pos))(pos)
    }

  def buildFunctor(parts: Seq[FunctorPhrase])(pos: Pos) =
    parts.foldLeft(partialFunctor(pos = pos))(mergePartialFunctors)

  val functorClause: P[FunctorPhrase] = P(
    (`require` ~/ importElem.rep) ^^ (r => partialFunctor(require = r))
      | `prepare` ~/ defineBody ^^ (p => partialFunctor(prepare = Some(p)))
      | `import` ~/ importElem.rep ^^ (i => partialFunctor(imports = i))
      | `define` ~/ defineBody ^^ (d => partialFunctor(define = Some(d)))
      | `export` ~/ exportElem.rep ^^ (e => partialFunctor(exports = e)))

  val importAlias: P[AliasedFeature] =
    (featureNoVar ~ (`:` ~/ variable).?) ^ AliasedFeature.apply

  val importLocation: P[String] =
    `at` ~/ atomConst ^^ (_.value)

  val importElem: P[FunctorImport] =
    pos3(variable ~ ("(" ~/ importAlias.rep ~ ")").? ~ importLocation.?) {
      case (module, None, location, pos)          => FunctorImport(module, Nil, location)(pos)
      case (module, Some(aliases), location, pos) => FunctorImport(module, aliases, location)(pos)
    }

  val exportElem: P[FunctorExport] =
    P(exportFeature ~ variable) ^ FunctorExport.apply

  val exportFeature: P[Expression] =
    pos1((featureNoVar ~ `:`).?) { case (f, p) => f.getOrElse(AutoFeature()(p)) }

  val defineBody: P[Phrase] = inPhrase

  // Class

  val exprOrImplDollar: P[Option[Phrase]] = (
    P("$") ^^^ None |
    expression.map(Some(_)) |
    Pass ^^^ None)

  val classExpression: P[Phrase] =
    pos2(P(`class` ~/ exprOrImplDollar ~ classContentsItem.rep ~ `end`)) {
      case (None, parts, pos) => buildClass(parts)(pos)
      case (Some(lhs), parts, pos) =>
        BindPhrase(lhs, buildClass(parts)(pos).copy(name = nameOf(lhs))(pos))(pos)
    }

  def buildClass(parts: Seq[ClassPhrase])(pos: Pos) =
    parts.foldLeft(partialClass(pos = pos))(mergePartialClasses)

  val classContentsItem: P[ClassPhrase] = P(
    `from` ~/ expression.rep ^^ (p => partialClass(parents = p))
      | `feat` ~/ classFeatOrAttr.rep ^^ (f => partialClass(features = f))
      | `attr` ~/ classFeatOrAttr.rep ^^ (a => partialClass(attributes = a))
      | `prop` ~/ expression.rep ^^ (p => partialClass(properties = p))
      | classMethod ^^ (m => partialClass(methods = Seq(m))))

  val classFeatOrAttr: P[FeatOrAttrPhrase] =
    P(attrOrFeat ~ (`:` ~/ expression).?) ^ FeatOrAttrPhrase.apply

  val methodParamName: P[Phrase] =
    P(variable | wildcardExpr | nestingMarker)

  val methodParamFeat: P[Phrase] = optFeature

  val methodParam: P[MethodParamPhrase] =
    P(methodParamFeat ~ methodParamName ~ ("<=" ~/ expression).?) ^ MethodParamPhrase.apply

  val methodHeaderLabel: P[Phrase] = P(recordLabel | escapedVariable)

  val methodHeader: P[MethodHeaderPhrase] = P(
    pos2(methodHeaderLabel ~ ("(" ~/ methodParam.rep ~ `...`.!.? ~ ")").?) {
      case (label, None, pos)                 => MethodHeaderPhrase(label, Nil, false)(pos)
      case (label, Some((params, open)), pos) => MethodHeaderPhrase(label, params, open.isDefined)(pos)
    })

  val classMethod: P[MethodDefPhrase] =
    pos3(`meth` ~/ methodHeader ~ ("=" ~ variable).? ~ inPhrase ~ `end`) {
      case (header, msgVar, body, pos) => MethodDefPhrase(header, msgVar, body)(pos)
    }

  // Helpers

  def nameBindRHS(bind: BindPhrase) = bind match {
    case BindPhrase(lhs @ RawVariable(varName), proc @ ProcPhrase(None, args, body, flags)) =>
      BindPhrase(lhs, proc.copy(name = Some(lhs))(proc))(lhs)
    case BindPhrase(lhs @ RawVariable(varName), fun @ FunPhrase(None, args, body, flags)) =>
      BindPhrase(lhs, fun.copy(name = Some(lhs))(fun))(lhs)
    case _ =>
      bind
  }

  def postProcessArgsAndBody(args: Seq[Phrase], body: Phrase) = {
    if (args.forall(_.isInstanceOf[RawVariable])) {
      (args.map(_.asInstanceOf[RawVariable]), body)
    } else {
      val pos = Node.posFromSeq(args, body)
      val (newArgs, patMatValue, pattern) = postProcessArgsInner(args)
      (newArgs, MatchPhrase(patMatValue,
        Seq(MatchPhraseClause(pattern, None, body)(patMatValue)),
        NoElsePhrase()(pos))(pos))
    }
  }

  def postProcessArgsInner(args: Seq[Phrase]) = {
    val newArgs = new ListBuffer[RawVariable]
    val patMatValueItems = new ListBuffer[RawVariable]
    val patternItems = new ListBuffer[Phrase]

    for (arg <- args) {
      arg match {
        case v: RawVariable =>
          newArgs += v

        case _ =>
          val newArg = RawVariable(generateParamIdent())(arg)
          newArgs += newArg
          patMatValueItems += newArg
          patternItems += arg
      }
    }

    assert(!patMatValueItems.isEmpty)

    if (patMatValueItems.size == 1) {
      (newArgs, patMatValueItems.head, patternItems.head)
    } else {
      (newArgs, sharp(patMatValueItems), sharp(patternItems))
    }
  }

  def partialFunctor(name: String = "",
                     require: Seq[FunctorImport] = Nil,
                     prepare: Option[Phrase] = None,
                     imports: Seq[FunctorImport] = Nil,
                     define: Option[Phrase] = None,
                     exports: Seq[FunctorExport] = Nil,
                     pos: Pos = Node.noPos) = {
    FunctorPhrase(name, require, prepare, imports, define, exports)(pos)
  }

  def mergePartialFunctors(lhs: FunctorPhrase, rhs: FunctorPhrase) = {
    val FunctorPhrase(lhsName, lhsRequire, lhsPrepare,
      lhsImports, lhsDefine, lhsExports) = lhs

    val FunctorPhrase(rhsName, rhsRequire, rhsPrepare,
      rhsImports, rhsDefine, rhsExports) = rhs

    val pos = if (lhs.pos != Node.noPos) lhs else rhs

    FunctorPhrase(if (lhsName.isEmpty) rhsName else lhsName,
      lhsRequire ++ rhsRequire, lhsPrepare orElse rhsPrepare,
      lhsImports ++ rhsImports, lhsDefine orElse rhsDefine,
      lhsExports ++ rhsExports)(pos)
  }

  def partialClass(name: String = "", parents: Seq[Phrase] = Nil,
                   features: Seq[FeatOrAttrPhrase] = Nil, attributes: Seq[FeatOrAttrPhrase] = Nil,
                   properties: Seq[Phrase] = Nil, methods: Seq[MethodDefPhrase] = Nil,
                   pos: Pos = Node.noPos) =
    ClassPhrase(name, parents, features, attributes, properties, methods)(pos)

  def mergePartialClasses(lhs: ClassPhrase, rhs: ClassPhrase) = {
    val ClassPhrase(lhsName, lhsParents, lhsFeatures, lhsAttributes,
      lhsProperties, lhsMethods) = lhs

    val ClassPhrase(rhsName, rhsParents, rhsFeatures, rhsAttributes,
      rhsProperties, rhsMethods) = rhs

    val pos = if (lhs.pos != Node.noPos) lhs else rhs

    ClassPhrase(if (lhsName.isEmpty) rhsName else lhsName,
      lhsParents ++ rhsParents, lhsFeatures ++ rhsFeatures,
      lhsAttributes ++ rhsAttributes, lhsProperties ++ rhsProperties,
      lhsMethods ++ rhsMethods)(pos)
  }

  def exprListToListExpr(elems: Seq[Phrase]): Phrase = {
    val nil: Phrase = Constant(OzAtom("nil"))(elems(0))
    elems.foldRight(nil)((e, tail) => cons(e, tail))
  }

  def cons(head: Phrase, tail: Phrase) =
    RecordPhrase(Constant(OzAtom("|"))(head),
      Seq(withAutoFeature(head), withAutoFeature(tail)))(head)

  def sharp(fields: Seq[Phrase]) = {
    assert(!fields.isEmpty)
    RecordPhrase(Constant(OzAtom("#"))(fields(0)), fields map withAutoFeature)(fields.head)
  }

  def withAutoFeature(expr: Phrase): RecordFieldPhrase =
    RecordFieldPhrase(AutoFeature()(expr), expr)(expr)

  // MAIN

  val tokenizer = Start ~ token.rep.~/ ~ End
  val expressionParser = Start ~ expression ~ End

  private def setupParser(source: Source, defines: Set[String]) = {
    val (input, map) = Preprocessor.preprocess(source)
    Parser.map = map
    Parser.i = 0
    input
  }

  def parseStatement(source: Source, defines: Set[String]): Statement = {
    val input = setupParser(source, defines)
    val phrase = t(expressionParser.parse(input), source.getName)
    TypeAST.stat(phrase)
  }

  def parseExpression(source: Source, defines: Set[String]): Expression = {
    val input = setupParser(source, defines)
    val phrase = t(expressionParser.parse(input), source.getName)
    TypeAST.expr(phrase)
  }

  def tokens(input: String): Parsed[Seq[Any]] =
    tokenizer.parse(input)

  def t[T](result: Parsed[T], file: String): T = {
    result match {
      case Parsed.Success(value, idx) =>
        value
      case fail @ Parsed.Failure(lastParser, index, extra) =>
        println("FAILED parsing " + file + " at index " + index + " line " + extra.line + " col " + extra.col)
        println(extra.traced.trace)
        extra.traced.stack.foreach { f => println("  " + f) }
        println()
        extra.traced.fullStack.foreach { f => println("  " + f) }
        throw new Error("Parsing failed for " + file)
    }
  }
}
