package org.xteam.cs.lex.ast;

import org.xteam.cs.runtime.AstList;
import org.xteam.cs.runtime.AstNode;
import org.xteam.cs.runtime.Span;

public class LexerRuleAst extends AstNode {

	private LexerIdentAst name;
	private LexerBolAst bol;
	private ExprAst expr;
	private ExprAst lookahead;
	private AstList<LexerActionAst> actions;

	public LexerRuleAst(Span span, LexerIdentAst name,
			LexerBolAst bol, ExprAst expr,
			ExprAst lookahead, AstList<LexerActionAst> actions) {
		super(span);
		this.name = name;
		this.bol = bol;
		this.expr = expr;
		this.lookahead = lookahead;
		this.actions = actions;
	}
	
	public LexerBolAst getBol() {
		return bol;
	}

	public AstList<LexerActionAst> getActions() {
		return actions;
	}

	public LexerIdentAst getName() {
		return name;
	}

	public ExprAst getExpression() {
		return expr;
	}

	public ExprAst getLookahead() {
		return lookahead;
	}

}
