package org.xteam.cs.lex.model;

public class LexicalState {
	
	private String name;
	private Action eofAction;
	
	public LexicalState(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}

	public Action getEofAction() {
		return eofAction;
	}

	public void setEofAction(Action action) {
		this.eofAction = action;
	}
	
	@Override
	public String toString() {
		return name;
	}
	
}
