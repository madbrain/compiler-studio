package org.xteam.cs.lex.ast;

import org.xteam.cs.runtime.AstList;
import org.xteam.cs.runtime.AstNode;
import org.xteam.cs.runtime.Span;

public class LexerFileAst extends AstNode {

	private LexerHeaderAst header;
	private AstList<LexerDefinitionAst> definitions;
	private AstList<LexerStateDefinitionAst> stateDefinitions;

	public LexerFileAst(Span span, LexerHeaderAst header,
			AstList<LexerDefinitionAst> definitions,
			AstList<LexerStateDefinitionAst> stateDefinitions) {
		super(span);
		this.header = header;
		this.definitions = definitions;
		this.stateDefinitions = stateDefinitions;
	}

	public LexerHeaderAst getHeader() {
		return header;
	}

	public AstList<LexerDefinitionAst> getDefinitions() {
		return definitions;
	}

	public AstList<LexerStateDefinitionAst> getStateDefinitions() {
		return stateDefinitions;
	}
	
}
