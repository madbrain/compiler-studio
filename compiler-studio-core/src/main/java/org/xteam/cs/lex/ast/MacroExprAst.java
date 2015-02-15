package org.xteam.cs.lex.ast;

import org.xteam.cs.runtime.Span;

public class MacroExprAst extends ExprAst {

	private String name;

	public MacroExprAst(Span span, String name) {
		super(span);
		this.name = name;
	}
	
	@Override
	public void visit(ILexerAstVisitor visitor) {
		visitor.visitMacroExpr(this);
	}

	public String getName() {
		return name;
	}

}
