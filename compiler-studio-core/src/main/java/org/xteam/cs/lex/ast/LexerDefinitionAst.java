package org.xteam.cs.lex.ast;

import org.xteam.cs.runtime.AstNode;
import org.xteam.cs.runtime.Span;

public class LexerDefinitionAst extends AstNode {

	private LexerIdentAst name;
	private ExprAst expr;

	public LexerDefinitionAst(Span span, LexerIdentAst name, ExprAst expr) {
		super(span);
		this.name = name;
		this.expr = expr;
	}

	public LexerIdentAst getName() {
		return name;
	}
	
	public ExprAst getExpr() {
		return expr;
	}
	
}
