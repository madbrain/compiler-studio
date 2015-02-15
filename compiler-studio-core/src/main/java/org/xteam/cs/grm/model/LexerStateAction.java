package org.xteam.cs.grm.model;

public class LexerStateAction extends Action {

	private int state;
	private int condition;

	public LexerStateAction(int state, int condition) {
		this.state = state;
		this.condition = condition;
	}

	public int getState() {
		return state;
	}

	public int getCondition() {
		return condition;
	}

}
