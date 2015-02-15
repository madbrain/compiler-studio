package org.xteam.cs.lex.ast;

import org.xteam.cs.runtime.Span;

public class StarExprAst extends UnaryExprAst {

	public StarExprAst(Span span, ExprAst expr) {
		super(span, expr);
	}
	
	@Override
	public void visit(ILexerAstVisitor visitor) {
		visitor.visitStarExpr(this);
	}
	
}
