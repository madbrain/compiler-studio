package org.xteam.cs.lex.ast;

import org.xteam.cs.runtime.Span;

public class EofExprAst extends ExprAst {

	public EofExprAst(Span span) {
		super(span);
	}
	
	@Override
	public void visit(ILexerAstVisitor visitor) {
		visitor.visitEof(this);
	}

}
