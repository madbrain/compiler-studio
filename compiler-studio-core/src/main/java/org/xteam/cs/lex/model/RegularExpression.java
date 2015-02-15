package org.xteam.cs.lex.model;

import java.util.List;

public class RegularExpression {
	
	private List<LexicalState> states;
	private Expr definition;
	private Expr lookahead;
	private boolean isBol;
	private boolean isEof;
	private Action action;
	
	public RegularExpression(List<LexicalState> states, boolean isBol,
			boolean isEof, Expr definition, Expr lookahead, Action action) {
		this.states = states;
		this.isBol = isBol;
		this.isEof = isEof;
		this.definition = definition;
		this.lookahead = lookahead;
		this.action = action;
	}

	public Expr getDefinition() {
		return definition;
	}
	
	public Expr getLookahead() {
		return lookahead;
	}

	public boolean isBol() {
		return isBol;
	}

	public List<LexicalState> getStates() {
		return states;
	}

	public Action getAction() {
		return action;
	}

	public boolean isEof() {
		return isEof;
	}
	
}
