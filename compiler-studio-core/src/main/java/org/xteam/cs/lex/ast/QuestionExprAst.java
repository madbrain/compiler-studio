package org.xteam.cs.lex.ast;

import org.xteam.cs.runtime.Span;

public class QuestionExprAst extends UnaryExprAst {

	public QuestionExprAst(Span span, ExprAst expr) {
		super(span, expr);
	}
	
	@Override
	public void visit(ILexerAstVisitor visitor) {
		visitor.visitQuestion(this);
	}

}
