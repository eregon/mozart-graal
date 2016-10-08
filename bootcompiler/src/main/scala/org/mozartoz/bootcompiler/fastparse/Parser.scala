package org.mozartoz.bootcompiler.fastparse

import com.oracle.truffle.api.source.Source
import fastparse.WhitespaceApi
import java.io.File
import org.mozartoz.bootcompiler.ast._
import org.mozartoz.bootcompiler.oz._
import org.mozartoz.bootcompiler.fastparse.Preprocessor.SourceMap
import org.mozartoz.bootcompiler.fastparse.Tokens._
import org.mozartoz.bootcompiler.BootCompiler
import scala.collection.mutable.ListBuffer

object Whitespace {
  import fastparse.all._

  val whitespace = (
    CharPred(ch => ch <= ' ')
    | "/*" ~/ ((!"*/" ~ AnyChar).rep ~ "*/").opaque("unclosed comment")
    | "%" ~/ CharsWhile(_ != '\n').?).rep.opaque("whitespace")

  val whitespaceWrapper = WhitespaceApi.Wrapper {
    NoTrace(whitespace)
  }
}

// Compatibility API with parser combinators
object CompatAPI {
  import fastparse.all.Parser
  import fastparse.all.P
  import fastparse.all.parserApi

  class ParserMapper[T](val p: Parser[T]) {
    def ^^[U](f: T => U): Parser[U] = p.map { x => f(x) }

    def ^^^[U](f: => U): Parser[U] = p.map { x => f }
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

  val floatLiteralBase: P[Double] = P(
    (digit.rep(1).! ~ `.` ~ digit.rep.! ~ (CharIn("eE") ~ floatExponentBase).?).map {
      case (int, frac, None)      => (int + "." + frac).toDouble
      case (int, frac, Some(exp)) => { (int + "." + frac + "e" + exp).toDouble }
    })

  val floatExponentBase: P[String] = P(
    digit.rep(1).!
      | "~" ~ digit.rep(1).! ^^ (x => "-" + x))

  val integerLiteral: P[Long] = P(
    integerLiteralBase
      | "~" ~ integerLiteralBase ^^ (x => -x)) ~ !("." ~ (!"." ~ AnyChar)) // Disallow int.X but allow int..int (for I in 1..10 do)

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
    source.createSection("", start, len)
  }

  def positioned[T <: Node](p: P[T]) = P(Index ~~ p ~~ Index).map {
    case (pB, node, pE) => node.setSourceSection(createSection(pB, pE))
  }

  def deepPositioned[T <: Node](p: P[T]) = P(Index ~~ p ~~ Index).map {
    case (pB, node, pE) =>
      val section = createSection(pB, pE)
      node.walkBreak { subNode =>
        if (subNode.section != null) {
          false
        } else {
          subNode.setSourceSection(section)
          true
        }
      }
      node
  }

  /** Extracts the name of a RawVariable, or "" if it is not */
  private def nameOf(expression: Phrase) = expression match {
    case RawVariable(name) => name
    case _                 => ""
  }
  
  /** Extracts the name of an optional raw variable  */
  private def nameOf(expression: Option[Phrase]): Option[VariableOrRaw] = expression match {
    case Some(v @ RawVariable(_)) => Some(v)
    case _ => None
  }
  
  private val generatedIdentCounter = new org.mozartoz.bootcompiler.util.Counter
  private def generateExcIdent() = "<exc$" + generatedIdentCounter.next() + ">"
  private def generateParamIdent() = "<arg$" + generatedIdentCounter.next() + ">"

  def leftAssoc(p: P[Phrase], op: P[String], binOp: (Phrase, String, Phrase) => Phrase) = positioned {
    P(p ~ (op ~ p).rep).map {
      case (lhs, ops) => ops.foldLeft(lhs) { case (lhs, (op, rhs)) => binOp(lhs, op, rhs) }
    }
  }

  def unaryOp(p: P[(String, Phrase)]) = p ^^ UnaryOpPhrase.tupled

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

  val featureNoVar = positioned(featureConst ^^ Constant)

  // Trivial expressions

  val variable = positioned("?".? ~ identifier.! ^^ RawVariable)

  val escapedVariable: P[EscapedVariable] =
    positioned("!" ~ variable ^^ EscapedVariable)

  val wildcardExpr: P[UnboundExpression] =
    positioned(("_").!.map(_ => UnboundExpression()))

  val nestingMarker: P[NestingMarker] =
    positioned(P("$") ^^^ NestingMarker())

  val integerConstExpr = positioned(integerConst ^^ Constant)
  val floatConstExpr = positioned(floatConst ^^ Constant)
  val atomConstExpr = positioned(atomConst ^^ Constant)
  val literalConstExpr = positioned(literalConst ^^ Constant)
  val stringConstExpr = positioned(stringConst ^^ Constant)
  val selfExpr = positioned(P(`self`) ^^^ Self())

  val attrOrFeat: P[Phrase] =
    P(variable | escapedVariable | integerConstExpr | literalConstExpr)

  val trivialExpression: P[Phrase] =
    P(floatConstExpr | attrOrFeat | wildcardExpr | nestingMarker | selfExpr)

  // Record expressions

  val recordExpression: P[Phrase] = positioned(
    P(recordLabel ~~ "(" ~/ recordField.rep ~ ")" ^^ RecordPhrase))

  val recordLabel: P[Phrase] = atomConstExpr | variable

  val recordField = P(positioned {
    optFeature ~ expression ^^ RecordFieldPhrase
  })

  val feature: P[Phrase] = (featureNoVar | variable)

  val optFeature: P[Phrase] = positioned {
    (feature ~ `:`).? ^^ (_.getOrElse(AutoFeature()))
  }

  // List expressions

  val listExpression = P("[" ~ expression.rep(1) ~ "]") ^^ ListPhrase

  // Skip

  val skipStatement: P[Phrase] = positioned(P(`skip`) ^^^ SkipStatement())

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
  val lvlD: P[Phrase] = P(("@" | "!!").! ~/ lvlD ^^ UnaryOpPhrase | atPhrase)
  val lvlC: P[Phrase] = P(leftAssoc(lvlD, `.`.!.~/, BinaryOpPhrase))
  val lvlB: P[Phrase] = P("~".! ~/ lvlB ^^ UnaryOpPhrase | lvlC)
  val lvlA: P[Phrase] = P(lvlB ~ ("," ~/ lvlA).?).map {
    case (lhs, Some(rhs)) => BinaryOpPhrase(lhs, ",", rhs)
    case (lhs, None)      => lhs
  }
  val lvl9: P[Phrase] = leftAssoc(lvlA, ("*" | "/" | `div` | `mod`).!.~/, BinaryOpPhrase)
  val lvl8: P[Phrase] = leftAssoc(lvl9, ("+" | "-").!.~/, BinaryOpPhrase)
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
    case (lhs, Some((op, rhs))) => BinaryOpPhrase(lhs, op, rhs)
    case (lhs, None)            => lhs
  }
  val lvl3: P[Phrase] = P(lvl4 ~ (`andthen` ~/ lvl3).?).map {
    case (lhs, Some(rhs)) => ShortCircuitBinaryOpPhrase(lhs, "andthen", rhs)
    case (lhs, None)      => lhs
  }
  val lvl2: P[Phrase] = P(lvl3 ~ (`orelse` ~/ lvl2).?).map {
    case (lhs, Some(rhs)) => ShortCircuitBinaryOpPhrase(lhs, "orelse", rhs)
    case (lhs, None)      => lhs
  }
  val lvl1: P[Phrase] = P(lvl2 ~ (("<-" | ":=").! ~/ lvl1).?).map {
    case (BinaryOpPhrase(l, ".", r), Some((op, rhs))) => DotAssignPhrase(l, r, rhs)
    case (lhs, Some((op, rhs)))                       => BinaryOpPhrase(lhs, op, rhs)
    case (lhs, None)                                  => lhs
  }
  val lvl0: P[Phrase] = P(lvl1 ~ ("=" ~/ lvl0).?).map {
    case (lhs, Some(rhs)) => BindPhrase(lhs, rhs)
    case (lhs, None)      => lhs
  }
  val expression: P[Phrase] = lvl0

  val phrase: P[Phrase] = lvl0.rep(1).map {
    case Seq(node) => node
    case many      => CompoundPhrase(many)
  }

  // Declarations

  val inPhrase = P(phrase ~ (`in` ~/ phrase).?).map {
    case (lhs, Some(rhs)) => RawLocalPhrase(lhs, rhs)
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

  val procExpression: P[Phrase] = deepPositioned {
    P(`proc` ~/ procFlags ~ "{" ~ dollarOrExpr ~/ formalArgs ~ "}" ~/ inPhrase ~ `end`).map {
      case (flags, name, args0, body0) =>
        val (args, body) = postProcessArgsAndBody(args0, body0)
        val proc = ProcPhrase(nameOf(name), args, body, flags)
        name match {
          case Some(expr) => BindPhrase(expr, proc)
          case None       => proc
        }
    }
  }

  val funExpression: P[Phrase] = deepPositioned {
    P(`fun` ~/ procFlags ~ "{" ~ dollarOrExpr ~/ formalArgs ~ "}" ~/ inPhrase ~ `end`).map {
      case (flags, name, args0, body0) =>
        val (args, body) = postProcessArgsAndBody(args0, body0)
        val fun = FunPhrase(nameOf(name), args, body, flags)
        name match {
          case Some(expr) => BindPhrase(expr, fun)
          case None       => fun
        }
    }
  }

  // Call

  val callExpression = positioned {
    P("{" ~ expression ~ actualArgs ~ "}") ^^ CallPhrase
  }

  val actualArgs = P(expression.rep)

  // If then else end

  val ifExpression: P[Phrase] = positioned {
    P(`if` ~/ innerIfExpression ~ `end`)
  }

  val innerIfExpression: P[Phrase] =
    P(expression ~ `then` ~/ inPhrase ~ elseExpression) ^^ IfPhrase

  val elseExpression: P[Phrase] = P(positioned(
    `else` ~/ inPhrase
      | `elseif` ~/ innerIfExpression
      | `elsecase` ~/ innerCaseExpression
      | Pass ^^^ NoElsePhrase()))

  // case of

  val caseExpression = positioned {
    P(`case` ~/ innerCaseExpression ~ `end`)
  }

  val innerCaseExpression: P[Phrase] = P(
    expression ~ `of` ~/ caseExpressionClauses ~ elseExpression ^^ MatchPhrase)

  val caseExpressionClauses: P[Seq[MatchPhraseClause]] =
    P(caseExpressionClause.rep(min = 1, sep = ("[]" | `elseof`).~/))

  val caseExpressionClause: P[MatchPhraseClause] = positioned(
    P(pattern) ~ (`andthen` ~/ expression).? ~ (`then` ~/ inPhrase) ^^ MatchPhraseClause)

  // Pattern

  val pattern: P[Phrase] = P(pattern1 ~ ("=" ~/ pattern).?).map {
    case (lhs, Some(rhs)) => PatternConjunctionPhrase(Seq(lhs, rhs))
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
    positioned((recordLabel ~ "(" ~/ patternRecordField.rep ~ `...`.!.? ~ ")") ^^ {
      case (label, fields, Some(openMarker)) => OpenRecordPatternPhrase(label, fields)
      case (label, fields, None)             => RecordPhrase(label, fields)
    })
      | positioned("[" ~/ pattern.rep(1) ~ "]" ^^ exprListToListExpr)
      | floatConstExpr | integerConstExpr | literalConstExpr | stringConstExpr
      | variable
      | escapedVariable
      | wildcardExpr
      | "(" ~/ pattern ~ ")")

  val optFeatureNoVar: P[Phrase] =
    positioned((featureNoVar ~ `:`).? ^^ (_.getOrElse(AutoFeature())))

  val patternRecordField: P[RecordFieldPhrase] =
    positioned(optFeatureNoVar ~ pattern ^^ RecordFieldPhrase)

  // Thread

  val threadExpression = positioned {
    `thread` ~/ inPhrase ~ `end` ^^ ThreadPhrase
  }

  // Lock

  val lockExpression = positioned {
    `lock` ~/ NoCut(expression ~ `then`).? ~ inPhrase ~ `end` ^^ {
      case (Some(lock), body) => LockPhrase(lock, body)
      case (None, body)       => LockObjectPhrase(body)
    }
  }

  // Try

  val tryExpression = deepPositioned {
    P(`try` ~/ inPhrase ~ (`catch` ~/ caseExpressionClauses).? ~ (`finally` ~/ inPhrase).? ~ `end`) ^^ {
      case (body, optCatchClauses, optFinallyBody) =>
        val tryCatch = optCatchClauses match {
          case None => body
          case Some(catchClauses) =>
            val excVar = RawVariable(generateExcIdent())
            TryPhrase(body, excVar,
              MatchPhrase(excVar, catchClauses, RaisePhrase(excVar)))
        }

        optFinallyBody match {
          case None              => tryCatch
          case Some(finallyBody) => TryFinallyPhrase(tryCatch, finallyBody)
        }
    }
  }

  // Raise

  val raiseExpression = positioned {
    `raise` ~/ inPhrase ~ `end` ^^ RaisePhrase |
      `fail` ^^^ FailStatement()
  }

  // For loops

  val forStatement = deepPositioned {
    P(`for` ~/ formalArg ~ `in` ~/ forListGenerator ~ `do` ~/ inPhrase ~ `end`) ^^ {
      case (arg0, listExpr, body0) =>
        val (args, body) = postProcessArgsAndBody(Seq(arg0), body0)
        val forProc = ProcPhrase(None, args, body, Nil)
        CallPhrase(RawVariable("ForAll"), Seq(listExpr, forProc))
    }
  }

  val forListGenerator = deepPositioned {
    expression ~ (`..` ~/ expression).? ^^ {
      case (start, Some(end)) =>
        val listNumber = BinaryOpPhrase(RawVariable("List"), ".", Constant(OzAtom("number")))
        CallPhrase(listNumber, Seq(start, end, Constant(OzInt(1))))
      case (expr, None) => expr
    }
  }

  // Functor

  val functorExpression = deepPositioned {
    P(`functor` ~/ exprOrImplDollar ~ innerFunctor ~ `end`).map {
      case (None, functor) => functor
      case (Some(lhs), functor) =>
        BindPhrase(lhs, functor.copy(name = nameOf(lhs)))
    }
  }

  val innerFunctor: P[FunctorPhrase] = P(positioned(
    functorClause.rep ^^ {
      _.foldLeft(partialFunctor())(mergePartialFunctors)
    }))

  val functorClause: P[FunctorPhrase] = P(positioned(
    `require` ~/ importElem.rep ^^ (r => partialFunctor(require = r))
      | `prepare` ~/ defineBody ^^ (p => partialFunctor(prepare = Some(p)))
      | `import` ~/ importElem.rep ^^ (i => partialFunctor(imports = i))
      | `define` ~/ defineBody ^^ (d => partialFunctor(define = Some(d)))
      | `export` ~/ exportElem.rep ^^ (e => partialFunctor(exports = e))))

  val importElem: P[FunctorImport] = positioned(
    P(variable ~ ("(" ~/ importAlias.rep ~ ")").? ~ importLocation.?).map {
      case (module, None, location)          => FunctorImport(module, Nil, location)
      case (module, Some(aliases), location) => FunctorImport(module, aliases, location)
    })

  val importAlias: P[AliasedFeature] = positioned {
    featureNoVar ~ (`:` ~/ variable).? ^^ AliasedFeature
  }

  val importLocation: P[String] =
    `at` ~/ atomConst ^^ (_.value)

  val exportElem: P[FunctorExport] = positioned {
    P(exportFeature ~ variable) ^^ FunctorExport
  }

  val exportFeature: P[Expression] = positioned {
    (featureNoVar ~ `:`).? ^^ (_ getOrElse AutoFeature())
  }

  val defineBody: P[Phrase] = inPhrase

  // Class

  val exprOrImplDollar: P[Option[Phrase]] = (
    P("$") ^^^ None |
    expression.map(Some(_)) |
    Pass ^^^ None)

  val classExpression: P[Phrase] = deepPositioned {
    P(`class` ~/ exprOrImplDollar ~ classContents ~ `end`).map {
      case (None, clazz) => clazz
      case (Some(lhs), clazz) =>
        BindPhrase(lhs, clazz.copy(name = nameOf(lhs)))
    }
  }

  val classContents = P(classContentsItem.rep ^^ {
    _.foldLeft(partialClass())(mergePartialClasses)
  })

  val classContentsItem: P[ClassPhrase] = P(
    `from` ~/ expression.rep ^^ (p => partialClass(parents = p))
      | `feat` ~/ classFeatOrAttr.rep ^^ (f => partialClass(features = f))
      | `attr` ~/ classFeatOrAttr.rep ^^ (a => partialClass(attributes = a))
      | `prop` ~/ expression.rep ^^ (p => partialClass(properties = p))
      | classMethod ^^ (m => partialClass(methods = Seq(m))))

  val classFeatOrAttr: P[FeatOrAttrPhrase] = positioned {
    P(attrOrFeat ~ (`:` ~/ expression).?) ^^ FeatOrAttrPhrase
  }

  val methodParamName: P[Phrase] =
    P(variable | wildcardExpr | nestingMarker)

  val methodParamFeat: P[Phrase] = P(positioned {
    (feature ~ `:`).? ^^ (x => x.getOrElse(AutoFeature()))
  })

  val methodParam: P[MethodParamPhrase] = positioned(P(
    methodParamFeat ~ methodParamName ~ ("<=" ~/ expression).? ^^ MethodParamPhrase.tupled))

  val methodHeaderLabel: P[Phrase] = P(recordLabel | escapedVariable)

  val methodHeader: P[MethodHeaderPhrase] = P(positioned(
    (methodHeaderLabel ~ ("(" ~/ methodParam.rep ~ `...`.!.? ~ ")").?).map {
      case (label, None)                 => MethodHeaderPhrase(label, Nil, false)
      case (label, Some((params, open))) => MethodHeaderPhrase(label, params, open.isDefined)
    }))

  val classMethod: P[MethodDefPhrase] =
    P(`meth` ~/ methodHeader ~ ("=" ~ variable).? ~ inPhrase ~ `end`).map {
      case (header, msgVar, body) => MethodDefPhrase(header, msgVar, body)
    }

  // Helpers

  def postProcessArgsAndBody(args: Seq[Phrase], body: Phrase) = {
    if (args.forall(_.isInstanceOf[RawVariable])) {
      (args.map(_.asInstanceOf[RawVariable]), body)
    } else {
      val (newArgs, patMatValue, pattern) = postProcessArgsInner(args)
      (newArgs, MatchPhrase(patMatValue,
        Seq(MatchPhraseClause(pattern, None, body).copyAttrs(patMatValue)),
        NoElsePhrase()))
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
          val newArg = RawVariable(generateParamIdent()).copyAttrs(arg)
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
                     exports: Seq[FunctorExport] = Nil) = {
    FunctorPhrase(name, require, prepare, imports, define, exports)
  }

  def mergePartialFunctors(lhs: FunctorPhrase, rhs: FunctorPhrase) = {
    val FunctorPhrase(lhsName, lhsRequire, lhsPrepare,
      lhsImports, lhsDefine, lhsExports) = lhs

    val FunctorPhrase(rhsName, rhsRequire, rhsPrepare,
      rhsImports, rhsDefine, rhsExports) = rhs

    FunctorPhrase(if (lhsName.isEmpty) rhsName else lhsName,
      lhsRequire ++ rhsRequire, lhsPrepare orElse rhsPrepare,
      lhsImports ++ rhsImports, lhsDefine orElse rhsDefine,
      lhsExports ++ rhsExports)
  }

  def partialClass(name: String = "", parents: Seq[Phrase] = Nil,
                   features: Seq[FeatOrAttrPhrase] = Nil, attributes: Seq[FeatOrAttrPhrase] = Nil,
                   properties: Seq[Phrase] = Nil, methods: Seq[MethodDefPhrase] = Nil) =
    ClassPhrase(name, parents, features, attributes, properties, methods)

  def mergePartialClasses(lhs: ClassPhrase, rhs: ClassPhrase) = {
    val ClassPhrase(lhsName, lhsParents, lhsFeatures, lhsAttributes,
      lhsProperties, lhsMethods) = lhs

    val ClassPhrase(rhsName, rhsParents, rhsFeatures, rhsAttributes,
      rhsProperties, rhsMethods) = rhs

    ClassPhrase(if (lhsName.isEmpty) rhsName else lhsName,
      lhsParents ++ rhsParents, lhsFeatures ++ rhsFeatures,
      lhsAttributes ++ rhsAttributes, lhsProperties ++ rhsProperties,
      lhsMethods ++ rhsMethods)
  }

  def exprListToListExpr(elems: Seq[Phrase]): Phrase = {
    val nil: Phrase = Constant(OzAtom("nil")).copyAttrs(elems(0))
    elems.foldRight(nil)((e, tail) => cons(e, tail))
  }

  def cons(head: Phrase, tail: Phrase) = atPos(head) {
    RecordPhrase(Constant(OzAtom("|")),
      Seq(withAutoFeature(head), withAutoFeature(tail)))
  }

  def sharp(fields: Seq[Phrase]) = {
    if (fields.isEmpty) Constant(OzAtom("#"))
    else {
      atPos(fields.head) {
        RecordPhrase(Constant(OzAtom("#")), fields map withAutoFeature)
      }
    }
  }

  def withAutoFeature(expr: Phrase): RecordFieldPhrase = atPos(expr) {
    RecordFieldPhrase(AutoFeature(), expr)
  }

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
