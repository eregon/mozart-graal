package org.mozartoz.bootcompiler
package ast

import scala.reflect.ClassTag
import oz._
import symtab._
import transform._
import Node.Pos

/** Mixin trait that provides a DSL to synthesize Oz ASTs */
trait TreeDSL {
  /** User-provided tree copier */
  val treeCopy: TreeCopier

  /** Operations on Statements */
  implicit def statement2ops(self: Statement) = new {
    /** `self ~ right` sequentially composes `self` and `right` */
    def ~ (right: Statement) = self match {
      case CompoundStatement(selfStats) =>
        treeCopy.CompoundStatement(self, selfStats :+ right)

      case SkipStatement() =>
        right

      case _ =>
        treeCopy.CompoundStatement(self, Seq(self, right))
    }

    /** `self ~> right` sequentially composes `self` and `right` */
    def ~> (right: Expression) = self match {
      case SkipStatement() =>
        right

      case _ =>
        treeCopy.StatAndExpression(self, self, right)
    }
  }

  /** Operations on Expressions */
  implicit def expression2ops(self: Expression) = new {
    /** `self === rhs` binds `self` to `rhs` */
    def === (rhs: Expression) =
      treeCopy.BindStatement(self, self, rhs)

    /** Call `self` with arguments `args` as a statement */
    def call(args: Expression*) =
      new {
        def at(pos: Pos) = CallStatement(self, args)(pos)
      }

    /** Call `self` with arguments `args` as an expression */
    def callExpr(args: Expression*) =
      new {
        def at(pos: Pos) = CallExpression(self, args)(pos)
      }

    /** `==` operator */
    def =?= (rhs: Expression) =
      treeCopy.BinaryOp(self, self, "==", rhs)

    /** `.` operator */
    def dot(rhs: Expression) =
      treeCopy.BinaryOp(self, self, ".", rhs)
  }

  /** Wrap an Oz value inside a Constant */
  implicit def value2constant(value: OzValue): Constant =
    Constant(value)()

  /** Operations on Builtins */
  implicit def builtin2ops(builtin: Builtin) =
    expression2ops(OzBuiltin(builtin))

  /** Pattern matching for BindStatements
   *
   *  Usage:
   *  {{{
   *  someStatement match {
   *    case lhs === rhs =>
   *      ...
   *  }
   *  }}}
   */
  object === {
    def unapply(statement: BindStatement) =
      Some((statement.left, statement.right))
  }

  /** Construct IfStatements and IfExpressions
   *
   *  Usage:
   *  {{{
   *  IF (<condition>) THEN {
   *    <thenPart>
   *  } ELSE {
   *    <elsePart>
   *  }
   *  }}}
   */
  def IF(cond: Expression) = new {
    // Statement
    def THEN(trueStat: Statement) = new {
      def ELSE(falseStat: Statement) =
        treeCopy.IfStatement(cond, cond, trueStat, falseStat)
    }

    // Expression
    def THEN(trueExpr: Expression) = new {
      def ELSE(falseExpr: Expression) =
        treeCopy.IfExpression(cond, cond, trueExpr, falseExpr)
    }
  }

  /** Construct ProcExpressions
   *
   *  Usage:
   *  {{{
   *  PROC (<name>, Seq(<args>...) [, <flags>]) {
   *    <body>
   *  }
   *  }}}
   */
  def PROC(pos: Pos, name: Option[VariableOrRaw], args: Seq[VariableOrRaw],
      flags: Seq[String] = Nil)(body: Statement) =
    ProcExpression(name, args, body, flags)(pos)

  /** Construct FunExpressions
   *
   *  Usage:
   *  {{{
   *  FUN (<name>, Seq(<args>...) [, <flags>]) {
   *    <body>
   *  }
   *  }}}
   */
  def FUN(pos: Pos, name: Option[VariableOrRaw], args: Seq[VariableOrRaw],
      flags: Seq[String] = Nil)(body: Expression) = {
    FunExpression(name, args, body, flags)(pos)
  }

  /** Construct ThreadStatements
   *
   *  Usage:
   *  {{{
   *  THREAD {
   *    <body>
   *  }
   *  }}}
   */
  def THREAD(pos: Pos)(body: Statement) =
    ThreadStatement(body)(pos)

  /** Construct RawLocalStatements and RawLocalExpressions
   *
   *  Usage:
   *  {{{
   *  RAWLOCAL (<decls>...) IN {
   *    <body>
   *  }
   *  }}}
   */
  def RAWLOCAL(decls: RawDeclaration*) = new {
    def IN(body: Statement) =
      treeCopy.RawLocalStatement(body, decls, body)

    def IN(body: Expression) =
      treeCopy.RawLocalExpression(body, decls, body)
  }

  /** Construct LocalStatements and LocalExpressions
   *
   *  Usage:
   *  {{{
   *  LOCAL (<decls>...) IN {
   *    <body>
   *  }
   *  }}}
   */
  def LOCAL(decls: Variable*) = new {
    def IN(body: Statement) =
      if (decls.isEmpty) body else
        treeCopy.LocalStatement(body, decls, body)

    def IN(body: Expression) =
      if (decls.isEmpty) body else
        treeCopy.LocalExpression(body, decls, body)
  }

  /** Declare a synthetic temporary variable in a statement
   *
   *  Usage:
   *  {{{
   *  statementWithTemp { temp =>
   *    <body>
   *  }
   *  }}}
   *
   *  In `body` you can use `temp` as a temporary variable.
   */
  def statementWithTemp(pos: Pos)(statement: Variable => Statement) = {
    val temp = Variable.newSynthetic()(pos)
    LOCAL (temp) IN statement(temp)
  }

  /** Declare two synthetic temporary variables in a statement
   *
   *  Usage:
   *  {{{
   *  statementWithTemps { (temp1, temp2) =>
   *    <body>
   *  }
   *  }}}
   *
   *  In `body` you can use `temp1` and `temp2` as a temporary variables.
   */
  def statementWithTemps(pos: Pos)(statement: (Variable, Variable) => Statement) = {
    val temp1 = Variable.newSynthetic()(pos)
    val temp2 = Variable.newSynthetic()(pos)
    LOCAL (temp1, temp2) IN statement(temp1, temp2)
  }

  /** Declare a synthetic temporary variable in an expression
   *
   *  Usage:
   *  {{{
   *  expressionWithTemp { temp =>
   *    <body>
   *  }
   *  }}}
   *
   *  In `body` you can use `temp` as a temporary variable.
   */
  def expressionWithTemp(pos: Pos)(expression: Variable => Expression) = {
    val temp = Variable.newSynthetic()(pos)
    LOCAL (temp) IN expression(temp)
  }

  def statementWithValIn[A >: Variable <: Expression : ClassTag](value: Expression)(
      statement: A => Statement) = {
    value match {
      case value: A =>
        statement(value)
      case _ =>
        statementWithTemp(value) { temp =>
          (temp === value) ~ statement(temp)
        }
    }
  }

  def expressionWithValIn[A >: Variable <: Expression : ClassTag](value: Expression)(
      expression: A => Expression) = {
    value match {
      case value: A =>
        expression(value)
      case _ =>
        expressionWithTemp(value) { temp =>
          (temp === value) ~> expression(temp)
        }
    }
  }
}
