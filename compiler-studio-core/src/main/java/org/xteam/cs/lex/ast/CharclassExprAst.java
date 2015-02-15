package org.xteam.cs.lex.ast;

import org.xteam.cs.runtime.Span;

public class CharclassExprAst extends ExprAst {

	private String value;

	public CharclassExprAst(Span span, String value) {
		super(span);
		this.value = value;
	}
	
	public String getValue() {
		return value;
	}
	
	@Override
	public void visit(ILexerAstVisitor visitor) {
		visitor.visitCharclassExpr(this);
	}

}
