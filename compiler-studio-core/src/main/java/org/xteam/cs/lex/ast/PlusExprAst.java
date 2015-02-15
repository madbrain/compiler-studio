package org.xteam.cs.lex.ast;

import org.xteam.cs.runtime.Span;

public class PlusExprAst extends UnaryExprAst {

	public PlusExprAst(Span span, ExprAst expr) {
		super(span, expr);
	}
	
	@Override
	public void visit(ILexerAstVisitor visitor) {
		visitor.visitPlusExpr(this);
	}

}
