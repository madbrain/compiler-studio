package org.xteam.cs.lex.ast;

import org.xteam.cs.runtime.Span;

public abstract class UnaryExprAst extends ExprAst {

	protected ExprAst expr;

	public UnaryExprAst(Span span, ExprAst expr) {
		super(span);
		this.expr = expr;
	}
	
	public ExprAst getExpr() {
		return expr;
	}
	
}
