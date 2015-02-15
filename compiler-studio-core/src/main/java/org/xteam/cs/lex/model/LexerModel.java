package org.xteam.cs.lex.model;

import java.util.ArrayList;
import java.util.List;

public class LexerModel {
	
	private String name;
	private LexicalState initialState;
	private List<LexicalState> states = new ArrayList<LexicalState>();
	private List<LexicalState> inclusiveStates;
	private Action defaultAction;
	private List<RegularExpression> expressions = new ArrayList<RegularExpression>();
	
	public LexerModel(String name) {
		this.name = name;
	}

	public List<LexicalState> getStates() {
		return states;
	}

	public List<LexicalState> getInclusiveStates() {
		return inclusiveStates;
	}

	public void addState(LexicalState state) {
		this.states.add(state);
	}

	public void addExpression(RegularExpression expr) {
		this.expressions.add(expr);
	}
	
	public void removeExpression(RegularExpression expr) {
		this.expressions.remove(expr);
	}

	public List<RegularExpression> getExpressions() {
		return expressions;
	}

	public Action getDefaultAction() {
		return defaultAction;
	}

	public void setDefaultAction(Action action) {
		this.defaultAction = action;
	}

	public void setInitialState(LexicalState initialState) {
		this.initialState = initialState;
	}

	public LexicalState getInitialState() {
		return initialState;
	}

	public String getName() {
		return name;
	}
	
}
