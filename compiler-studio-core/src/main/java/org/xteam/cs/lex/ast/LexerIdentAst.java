package org.xteam.cs.lex.ast;

import org.xteam.cs.runtime.AstNode;
import org.xteam.cs.runtime.Span;

public class LexerIdentAst extends AstNode {

	private String value;

	public LexerIdentAst(Span span, String value) {
		super(span);
		this.value = value;
	}

	public String getValue() {
		return value;
	}
	
}
