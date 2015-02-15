package org.xteam.cs.lex.ast;

import org.xteam.cs.runtime.Span;

public class AnyCharExprAst extends ExprAst {

	public AnyCharExprAst(Span span) {
		super(span);
	}

	@Override
	public void visit(ILexerAstVisitor visitor) {
		visitor.visitAnyChar(this);
	}

}
