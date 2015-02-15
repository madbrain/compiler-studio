package org.xteam.cs.lex.ast;

import org.xteam.cs.runtime.AstList;
import org.xteam.cs.runtime.AstNode;
import org.xteam.cs.runtime.Span;

public class LexerStateDefinitionAst extends AstNode {

	private AstList<LexerIdentAst> names;
	private AstList<LexerRuleAst> rules;

	public LexerStateDefinitionAst(Span span,
			AstList<LexerIdentAst> names,
			AstList<LexerRuleAst> rules) {
		super(span);
		this.names = names;
		this.rules = rules;
	}
	
	public AstList<LexerIdentAst> getNames() {
		return names;
	}

	public AstList<LexerRuleAst> getRules() {
		return rules;
	}

}
