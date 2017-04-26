package org.mozartoz.bootcompiler
package transform

import oz._
import ast._
import symtab._

class TreeCopier {
  def copyAttrs[U <: StatOrExpr](orig: Node, copy: U): U = {
		  (orig, copy) match {
  		  case (orig: BindCommon, copy: BindCommon) =>
  		    copy.onStack = orig.onStack
  		    
  		  case (orig: CallCommon, copy: CallCommon) =>
  		    copy.kind = orig.kind
  		    
  		  case _ =>
		  }
		  copy
  }
    
  // Statements

  def CompoundStatement(tree: Node, statements: Seq[Statement]) =
    new CompoundStatement(statements)(tree)

  def RawLocalStatement(tree: Node, declarations: Seq[RawDeclaration],
      statement: Statement) =
    new RawLocalStatement(declarations, statement)(tree)

  def LocalStatement(tree: Node, declarations: Seq[Variable],
      statement: Statement) =
    new LocalStatement(declarations, statement)(tree)

  def CallStatement(tree: Node, callable: Expression, args: Seq[Expression]) =
    copyAttrs(tree, new CallStatement(callable, args)(tree))

  def IfStatement(tree: Node, condition: Expression,
      trueStatement: Statement, falseStatement: Statement) =
    new IfStatement(condition, trueStatement, falseStatement)(tree)

  def MatchStatement(tree: Node, value: Expression,
      clauses: Seq[MatchStatementClause], elseStatement: Statement) =
    new MatchStatement(value, clauses, elseStatement)(tree)

  def NoElseStatement(tree: Node) =
    new NoElseStatement()(tree)

  def ForStatement(tree: Node, from: Expression, to: Expression, proc: ProcExpression) =
    new ForStatement(from, to, proc)(tree)

  def ThreadStatement(tree: Node, statement: Statement) =
    new ThreadStatement(statement)(tree)

  def LockStatement(tree: Node, lock: Expression, statement: Statement) =
    new LockStatement(lock, statement)(tree)

  def LockObjectStatement(tree: Node, statement: Statement) =
    new LockObjectStatement(statement)(tree)

  def TryStatement(tree: Node, body: Statement, exceptionVar: VariableOrRaw,
      catchBody: Statement) =
    new TryStatement(body, exceptionVar, catchBody)(tree)

  def TryFinallyStatement(tree: Node, body: Statement,
      finallyBody: Statement) =
    new TryFinallyStatement(body, finallyBody)(tree)

  def RaiseStatement(tree: Node, exception: Expression) =
    new RaiseStatement(exception)(tree)

  def FailStatement(tree: Node) =
    new FailStatement()(tree)

  def BindStatement(tree: Node, left: Expression, right: Expression) =
    copyAttrs(tree, new BindStatement(left, right)(tree))

  def BinaryOpStatement(tree: Node, left: Expression, operator: String,
      right: Expression) =
    new BinaryOpStatement(left, operator, right)(tree)

  def DotAssignStatement(tree: Node, left: Expression, center: Expression,
      right: Expression) =
    new DotAssignStatement(left, center, right)(tree)

  def SkipStatement(tree: Node) =
    new SkipStatement()(tree)
    
  def ClearVarsStatement(tree: Node, node: Statement, before: Seq[Symbol], after: Seq[Symbol])=
    new ClearVarsStatement(node, before, after)(tree)

  // Expressions

  def StatAndExpression(tree: Node, statement: Statement,
      expression: Expression) =
    new StatAndExpression(statement, expression)(tree)

  def RawLocalExpression(tree: Node, declarations: Seq[RawDeclaration],
      expression: Expression) =
    new RawLocalExpression(declarations, expression)(tree)

  def LocalExpression(tree: Node, declarations: Seq[Variable],
      expression: Expression) =
    new LocalExpression(declarations, expression)(tree)

  // Complex expressions

  def ProcExpression(tree: Node, name: Option[VariableOrRaw], args: Seq[VariableOrRaw],
      body: Statement, flags: Seq[String]) =
    new ProcExpression(name, args, body, flags)(tree)

  def FunExpression(tree: Node, name: Option[VariableOrRaw], args: Seq[VariableOrRaw],
      body: Expression, flags: Seq[String]) =
    new FunExpression(name, args, body, flags)(tree)

  def CallExpression(tree: Node, callable: Expression, args: Seq[Expression]) =
    copyAttrs(tree, new CallExpression(callable, args)(tree))

  def IfExpression(tree: Node, condition: Expression,
      trueExpression: Expression, falseExpression: Expression) =
    new IfExpression(condition, trueExpression, falseExpression)(tree)

  def MatchExpression(tree: Node, value: Expression,
      clauses: Seq[MatchExpressionClause], elseExpression: Expression) =
    new MatchExpression(value, clauses, elseExpression)(tree)

  def NoElseExpression(tree: Node) =
    new NoElseExpression()(tree)

  def ThreadExpression(tree: Node, expression: Expression) =
    new ThreadExpression(expression)(tree)

  def LockExpression(tree: Node, lock: Expression, expression: Expression) =
    new LockExpression(lock, expression)(tree)

  def LockObjectExpression(tree: Node, expression: Expression) =
    new LockObjectExpression(expression)(tree)

  def TryExpression(tree: Node, body: Expression, exceptionVar: VariableOrRaw,
      catchBody: Expression) =
    new TryExpression(body, exceptionVar, catchBody)(tree)

  def TryFinallyExpression(tree: Node, body: Expression,
      finallyBody: Statement) =
    new TryFinallyExpression(body, finallyBody)(tree)

  def RaiseExpression(tree: Node, exception: Expression) =
    new RaiseExpression(exception)(tree)

  def BindExpression(tree: Node, left: Expression, right: Expression) =
    copyAttrs(tree, new BindExpression(left, right)(tree))

  def DotAssignExpression(tree: Node, left: Expression, center: Expression,
      right: Expression) =
    new DotAssignExpression(left, center, right)(tree)
    
  def ClearVarsExpression(tree: Node, node: Expression, before: Seq[Symbol], after: Seq[Symbol]) =
    new ClearVarsExpression(node, before, after)(tree)

  // Functors

  def AliasedFeature(tree: Node, feature: Constant,
      alias: Option[VariableOrRaw]) =
    new AliasedFeature(feature, alias)(tree)

  def FunctorImport(tree: Node, module: VariableOrRaw,
      aliases: Seq[AliasedFeature], location: Option[String]) =
    new FunctorImport(module, aliases, location)(tree)

  def FunctorExport(tree: Node, feature: Expression, value: Expression) =
    new FunctorExport(feature, value)(tree)

  def FunctorExpression(tree: Node, name: String,
      require: Seq[FunctorImport], prepare: Option[LocalStatementOrRaw],
      imports: Seq[FunctorImport], define: Option[LocalStatementOrRaw],
      exports: Seq[FunctorExport]) = {
    new FunctorExpression(name, require, prepare, imports,
        define, exports)(tree)
  }

  // Operations

  def UnaryOp(tree: Node, operator: String, operand: Expression) =
    new UnaryOp(operator, operand)(tree)

  def BinaryOp(tree: Node, left: Expression, operator: String,
      right: Expression) =
    new BinaryOp(left, operator, right)(tree)

  def ShortCircuitBinaryOp(tree: Node, left: Expression, operator: String,
      right: Expression) =
    new ShortCircuitBinaryOp(left, operator, right)(tree)

  // Trivial expressions

  def RawVariable(tree: Node, name: String) =
    new RawVariable(name)(tree)

  def Variable(tree: Node, symbol: Symbol) =
    new Variable(symbol)(tree)

  def EscapedVariable(tree: Node, variable: RawVariable) =
    new EscapedVariable(variable)(tree)

  def UnboundExpression(tree: Node) =
    new UnboundExpression()(tree)

  def Self(tree: Node) =
    new Self()(tree)

  def Constant(tree: Node, value: OzValue) =
    new Constant(value)(tree)

  // Records

  def RecordField(tree: Node, feature: Expression, value: Expression) =
    new RecordField(feature, value)(tree)

  def Record(tree: Node, label: Expression, fields: Seq[RecordField]) =
    new Record(label, fields)(tree)

  def OpenRecordPattern(tree: Node, label: Expression,
      fields: Seq[RecordField]) =
    new OpenRecordPattern(label, fields)(tree)

  def ListExpression(tree: Node, elements: Seq[Expression]) =
    new ListExpression(elements)(tree)

  def PatternConjunction(tree: Node, parts: Seq[Expression]) =
    new PatternConjunction(parts)(tree)

  // Match clauses

  def MatchStatementClause(tree: Node, pattern: Expression,
      guard: Option[Expression], body: Statement) =
    new MatchStatementClause(pattern, guard, body)(tree)

  def MatchExpressionClause(tree: Node, pattern: Expression,
      guard: Option[Expression], body: Expression) =
    new MatchExpressionClause(pattern, guard, body)(tree)

  // Classes

  def FeatOrAttr(tree: Node, name: Expression, value: Option[Expression]) =
    new FeatOrAttr(name, value)(tree)

  def MethodParam(tree: Node, feature: Expression, name: Expression,
      default: Option[Expression]) =
    new MethodParam(feature, name, default)(tree)

  def MethodHeader(tree: Node, name: Expression, params: Seq[MethodParam],
      open: Boolean) =
    new MethodHeader(name, params, open)(tree)

  def MethodDef(tree: Node, header: MethodHeader,
      messageVar: Option[VariableOrRaw], body: StatOrExpr) =
    new MethodDef(header, messageVar, body)(tree)

  def ClassExpression(tree: Node, name: String, parents: Seq[Expression],
      features: Seq[FeatOrAttr], attributes: Seq[FeatOrAttr],
      properties: Seq[Expression], methods: Seq[MethodDef]) =
    new ClassExpression(name, parents, features, attributes,
        properties, methods)(tree)
}
