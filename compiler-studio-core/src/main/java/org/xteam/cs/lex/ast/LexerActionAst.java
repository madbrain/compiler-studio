package org.xteam.cs.lex.ast;

import org.xteam.cs.runtime.AstList;
import org.xteam.cs.runtime.AstNode;
import org.xteam.cs.runtime.Span;

public class LexerActionAst extends AstNode {

	private LexerIdentAst name;
	private AstList<LexerIdentAst> arguments;

	public LexerActionAst(Span span, LexerIdentAst name, AstList<LexerIdentAst> arguments) {
		super(span);
		this.name = name;
		this.arguments = arguments;
	}

	public LexerIdentAst getName() {
		return name;
	}

	public AstList<LexerIdentAst> getArguments() {
		return arguments;
	}
	
}
