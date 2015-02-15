package org.xteam.cs.lex.ast;

import org.xteam.cs.runtime.AstNode;
import org.xteam.cs.runtime.Span;

public class LexerHeaderAst extends AstNode {

	private LexerIdentAst name;

	public LexerHeaderAst(Span span, LexerIdentAst name) {
		super(span);
		this.name = name;
	}

	public LexerIdentAst getName() {
		return name;
	}
	
}
