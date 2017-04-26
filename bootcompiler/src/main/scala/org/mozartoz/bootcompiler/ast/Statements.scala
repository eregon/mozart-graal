package org.mozartoz.bootcompiler
package ast

import Node.Pos
import org.mozartoz.bootcompiler.symtab.Symbol

/** Base class for ASTs that represent statements */
sealed abstract class Statement extends StatOrExpr with RawDeclaration

/** Sequential composition of several statements */
case class CompoundStatement(statements: Seq[Statement])(val pos: Pos) extends Statement {
  def syntax(indent: String) = {
    if (statements.isEmpty) "skip"
    else {
      statements.tail.foldLeft(statements.head.syntax(indent)) {
        _ + "\n" + indent + _.syntax(indent)
      }
    }
  }
}

trait LocalStatementOrRaw extends Statement

/** Raw local declaration statement (before naming)
 *
 *  {{{
 *  local
 *     <declarations>
 *  in
 *     <statement>
 *  end
 *  }}}
 */
case class RawLocalStatement(declarations: Seq[RawDeclaration],
    statement: Statement)(val pos: Pos) extends LocalStatementOrRaw with LocalCommon {
  protected val body = statement
}

/** Local declaration statement
 *
 *  {{{
 *  local
 *     <declarations>
 *  in
 *     <statement>
 *  end
 *  }}}
 */
case class LocalStatement(declarations: Seq[Variable],
    statement: Statement)(val pos: Pos) extends LocalStatementOrRaw with LocalCommon {
  protected val body = statement
}

/** Call statement
 *
 *  {{{
 *  {<callable> <args>...}
 *  }}}
 */
case class CallStatement(callable: Expression,
    args: Seq[Expression])(val pos: Pos) extends Statement with CallCommon
    
/** If statement
 *
 *  {{{
 *  if <condition> then
 *     <trueStatement>
 *  else
 *     <falseStatement>
 *  end
 *  }}}
 */
case class IfStatement(condition: Expression,
    trueStatement: Statement,
    falseStatement: Statement)(val pos: Pos) extends Statement with IfCommon {
  protected val truePart = trueStatement
  protected val falsePart = falseStatement
}

/** Pattern matching statement
 *
 *  {{{
 *  case <value>
 *  of <clauses>...
 *  else
 *     <elseStatement>
 *  end
 *  }}}
 */
case class MatchStatement(value: Expression,
    clauses: Seq[MatchStatementClause],
    elseStatement: Statement)(val pos: Pos) extends Statement with MatchCommon {
  protected val elsePart = elseStatement
}

/** Clause of a pattern matching statement
 *
 *  {{{
 *  [] <pattern> andthen <guard> then
 *     <body>
 *  }}}
 */
case class MatchStatementClause(pattern: Expression, guard: Option[Expression],
    body: Statement)(val pos: Pos) extends MatchClauseCommon {
}

/** Special node to mark that there is no else statement */
case class NoElseStatement()(val pos: Pos) extends Statement with NoElseCommon {
}

/** For statement
 *
 *  {{{
 *  for <variable> in <from>..<to> do
 *     <body>
 *  end
 *  }}}
 */
case class ForStatement(from: Expression, to: Expression, proc: ProcExpression)(val pos: Pos) extends Statement {
  def syntax(indent: String) = {
    val variable = proc.args(0)
    val decl = "for " + variable.syntax(indent) + " in " + from.syntax(indent) + ".." + to.syntax(indent) + " do"
    decl + "\n" + proc.body.syntax(indent + "   ") + "\n" + indent + "end"
  }
}

/** Thread statement
 *
 *  {{{
 *  thread
 *     <statement>
 *  end
 *  }}}
 */
case class ThreadStatement(
    statement: Statement)(val pos: Pos) extends Statement with ThreadCommon {
  protected val body = statement
}

/** Fail statement
 *
 *  {{{
 *  fail
 *  }}}
 */
case class FailStatement()(val pos: Pos) extends Statement with Phrase {
  def syntax(indent: String) = "fail"
}

/** Lock statement
 *
 *  {{{
 *  lock <lock> in
 *     <statement>
 *  end
 *  }}}
 */
case class LockStatement(lock: Expression,
    statement: Statement)(val pos: Pos) extends Statement with LockCommon {
  protected val body = statement
}

/** Lock object statement
 *
 *  {{{
 *  lock
 *     <statement>
 *  end
 *  }}}
 */
case class LockObjectStatement(
    statement: Statement)(val pos: Pos) extends Statement with LockObjectCommon {
  protected val body = statement
}

/** Try-catch statement
 *
 *  {{{
 *  try
 *     <body>
 *  catch <exceptionVar> then
 *     <catchBody>
 *  end
 *  }}}
 */
case class TryStatement(body: Statement, exceptionVar: VariableOrRaw,
    catchBody: Statement)(val pos: Pos) extends Statement with TryCommon {
}

/** Try-finally statement
 *
 *  {{{
 *  try
 *     <body>
 *  finally
 *     <finallyBody>
 *  end
 *  }}}
 */
case class TryFinallyStatement(body: Statement,
    finallyBody: Statement)(val pos: Pos) extends Statement with TryFinallyCommon {
}

/** Raise statement
 *
 *  {{{
 *  raise <exception> end
 *  }}}
 */
case class RaiseStatement(
    exception: Expression)(val pos: Pos) extends Statement with RaiseCommon {
}

/** Bind statement
 *
 *  {{{
 *  <left> = <right>
 *  }}}
 */
case class BindStatement(left: Expression,
    right: Expression)(val pos: Pos) extends Statement with BindCommon {
}

/** Binary operator statement
 *
 *  {{{
 *  <left> <operator> <right>
 *  }}}
 */
case class BinaryOpStatement(left: Expression, operator: String,
    right: Expression)(val pos: Pos) extends Statement with InfixSyntax {
  protected val opSyntax = " " + operator + " "
}

/** Dot-assign statement
 *
 *  {{{
 *  <left> . <center> := <right>
 *  }}}
 */
case class DotAssignStatement(left: Expression, center: Expression,
    right: Expression)(val pos: Pos) extends Statement with MultiInfixSyntax {
  protected val operands = Seq(left, center, right)
  protected val operators = Seq(".", " := ")
}

/** Skip statement
 *
 *  {{{
 *  skip
 *  }}}
 */
case class SkipStatement()(val pos: Pos) extends Statement with Phrase {
  def syntax(indent: String) = "skip"
}

case class ClearVarsStatement(node: Statement, before: Seq[Symbol],
    after: Seq[Symbol])(val pos: Pos) extends Statement with ClearVarsCommon
