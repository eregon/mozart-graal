package org.mozartoz.bootcompiler
package ast

import org.mozartoz.bootcompiler.symtab.Symbol

trait StatOrExpr extends Node

trait LocalCommon extends StatOrExpr {
  protected val declarations: Seq[RawDeclarationOrVar]
  protected val body: StatOrExpr

  def syntax(indent: String) = {
    val subIndent = indent + "   "

    val untilDecls = declarations.foldLeft("local") {
      _ + "\n" + subIndent + _.syntax(subIndent)
    }

    val untilIn = untilDecls + "\n" + indent + "in"

    untilIn + "\n" + subIndent + body.syntax(subIndent) + "\n" + indent + "end"
  }
}

trait ProcFunExpression extends StatOrExpr {
  protected val keyword: String

  protected val name: Option[VariableOrRaw]
  protected val args: Seq[VariableOrRaw]
  protected val body: StatOrExpr
  protected val flags: Seq[String]

  def syntax(indent: String) = {
    val flagsSyntax = flags.foldLeft("") { _ + " " + _ }
    val argsSyntax = args.foldLeft("") { _ + " " + _.syntax(indent) }

    val header0 = keyword + flagsSyntax + " {" + name.getOrElse("$")
    val header = header0 + argsSyntax + "}"

    val bodyIndent = indent + "   "
    val bodySyntax = bodyIndent + body.syntax(bodyIndent)

    header + "\n" + bodySyntax + "\n" + indent + "end"
  }
}

object CallKind extends Enumeration {
  case class CallKind(desc: String)
  
  val Normal = CallKind("")
  val Tail = CallKind("(tail)")
  val SelfTail = CallKind("(self)")
}

trait CallCommon extends StatOrExpr {
  var kind = CallKind.Normal
  
  def isSelfTail() = kind == CallKind.SelfTail
  def isTail() = kind == CallKind.Tail
  
  protected val callable: Expression
  protected val args: Seq[Expression]
  
  def syntax(indent: String) = args.toList match {
    case Nil => "{" + callable.syntax() + "}"

    case firstArg :: otherArgs => {
      val prefix = "{" + callable.syntax() + " "
      val subIndent = indent + " " * prefix.length

      val firstLine = prefix + firstArg.syntax(subIndent)

      otherArgs.foldLeft(firstLine) {
        _ + "\n" + subIndent + _.syntax(subIndent)
      } + "}" + kind.desc
    }
  }
}

trait IfCommon extends StatOrExpr {
  protected val condition: Expression
  protected val truePart: StatOrExpr
  protected val falsePart: StatOrExpr

  def syntax(indent: String) = {
    val subIndent = indent + "   "

    val condSyntax = "if " + condition.syntax(subIndent) + " then"
    val thenSyntax = "\n" + subIndent + truePart.syntax(subIndent)
    val elseSyntax = ("\n" + indent + "else\n" + subIndent +
        falsePart.syntax(subIndent))

    condSyntax + thenSyntax + elseSyntax + "\n" + indent + "end"
  }
}

trait MatchCommon extends StatOrExpr {
  protected val value: Expression
  protected val clauses: Seq[MatchClauseCommon]
  protected val elsePart: StatOrExpr

  def syntax(indent: String) = {
    val header = "case " + value.syntax(indent + "     ")

    val untilClauses = clauses.foldLeft(header) { (prev, clause) =>
      prev + "\n" + indent + clause.syntax(indent)
    }

    val untilElse = untilClauses + "\n" + indent + "else"
    val untilElseBody =
      untilElse + "\n" + indent + "   " + elsePart.syntax(indent + "   ")

    untilElseBody + "\n" + indent + "end"
  }
}

trait MatchClauseCommon extends Node {
  val pattern: Expression
  protected val guard: Option[Expression]
  protected val body: StatOrExpr

  def hasGuard = guard.isDefined

  def syntax(indent: String) = {
    val subIndent = indent + "   "

    val untilPattern = "[] " + pattern.syntax(subIndent)
    val untilGuard =
      if (guard.isEmpty) untilPattern
      else untilPattern + " andthen " + guard.get.syntax(subIndent)
    val allButBody = untilGuard + " then\n" + subIndent

    allButBody + body.syntax(subIndent)
  }
}

trait NoElseCommon extends StatOrExpr {
  def syntax(indent: String) = "<noelse>"
}

trait ThreadCommon extends StatOrExpr {
  protected val body: StatOrExpr

  def syntax(indent: String) = {
    val subIndent = indent + "   "

    "thread\n" + subIndent + body.syntax(subIndent) + "\n" + indent + "end"
  }
}

trait LockCommon extends StatOrExpr {
  protected val lock: Expression
  protected val body: StatOrExpr

  def syntax(indent: String) = {
    val subIndent = indent + "   "

    ("lock " + lock.syntax(indent+"     ") + " then\n" +
        subIndent + body.syntax(subIndent) + "\n" + indent + "end")
  }
}

trait LockObjectCommon extends StatOrExpr {
  protected val body: StatOrExpr

  def syntax(indent: String) = {
    val subIndent = indent + "   "

    "lock\n" + subIndent + body.syntax(subIndent) + "\n" + indent + "end"
  }
}

trait TryCommon extends StatOrExpr {
  protected val body: StatOrExpr
  protected val exceptionVar: VariableOrRaw
  protected val catchBody: StatOrExpr

  def syntax(indent: String) = {
    val subIndent = indent + "   "

    ("try\n" + subIndent + body.syntax(subIndent) + "\n" + indent +
        "catch " + exceptionVar.syntax(indent + "      ") + " then\n" +
        subIndent + catchBody.syntax(subIndent) + "\n" + indent + "end")
  }
}

trait TryFinallyCommon extends StatOrExpr {
  protected val body: StatOrExpr
  protected val finallyBody: Statement

  def syntax(indent: String) = {
    val subIndent = indent + "   "

    ("try\n" + subIndent + body.syntax(subIndent) + "\n" + indent +
        "finally\n" + subIndent + finallyBody.syntax(subIndent) + "\n" +
        indent + "end")
  }
}

trait RaiseCommon extends StatOrExpr {
  protected val exception: Expression

  def syntax(indent: String) = {
    "raise " + exception.syntax(indent + "      ") + " end"
  }
}

trait BindCommon extends StatOrExpr with InfixSyntax {
  var onStack = false
  
  protected val left: Expression
  protected val right: Expression

  protected val opSyntax = " = "
  
  override def syntax(indent: String) = super.syntax(indent) + (if (onStack) "(stack)" else "")
}

trait ClearVarsCommon extends StatOrExpr {
	protected val node: StatOrExpr
  protected val before: Seq[Symbol]
  protected val after: Seq[Symbol]
  
  override def syntax(indent: String) = {
    val subIndent = indent + "   "
	  "/"+before.mkString(",")+"/" + "\n"+
	    subIndent+node.syntax(subIndent) + "\n" +
	  indent + "\\"+after.mkString(",")+"\\"
	}
}
