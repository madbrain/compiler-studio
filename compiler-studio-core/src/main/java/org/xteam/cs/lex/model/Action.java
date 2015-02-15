package org.xteam.cs.lex.model;

import java.util.ArrayList;
import java.util.List;

public class Action {
	private List<ActionCode> codes;
	private LexicalState next;
	private String token;
	private int priority;
	private int tokenType;
	private String convertCode;
	
	public Action(List<ActionCode> codes, int priority, String token,
			LexicalState next, int tokenType, String convertCode) {
		this.codes = codes;
		this.priority = priority;
		this.token = token;
		this.next = next;
		this.tokenType = tokenType;
		this.convertCode = convertCode;
	}

	public Action(int priority) {
		this.priority = priority;
		this.codes = new ArrayList<ActionCode>();
	}

	public Action getHighestPriority(Action other) {
		// the smaller the number the higher the priority
		if (other == null || other.priority > this.priority)
			return this;
		return other;
	}
	
	public boolean isEquivalent(Action other) {
		return this == other;
	}

	public List<ActionCode> getCodes() {
		return codes;
	}

	public String getToken() {
		return token;
	}

	public LexicalState getNext() {
		return next;
	}

	public int getTokenType() {
		return tokenType;
	}

	public int getPriority() {
		return priority;
	}

	public String getConvertCode() {
		return convertCode;
	}
}
