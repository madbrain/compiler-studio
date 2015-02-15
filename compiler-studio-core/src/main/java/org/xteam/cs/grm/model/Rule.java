package org.xteam.cs.grm.model;

import java.util.ArrayList;
import java.util.List;


public class Rule {
	
	private NonTerminal lhs;
	private List<Symbol> rhs = new ArrayList<Symbol>();
	private Action action;
	
	public Rule(NonTerminal lhs, List<Symbol> rhs, Action action) {
		this.lhs = lhs;
		this.rhs = rhs;
		this.action = action;
		lhs.getRules().add(this);
	}

	public NonTerminal getLhs() {
		return lhs;
	}

	public List<Symbol> getRhs() {
		return rhs;
	}

	public Action getAction() {
		return action;
	}
	
	@Override
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		buffer.append(lhs.getName());
		buffer.append(" ::=");
		for (Symbol sym : rhs) {
			buffer.append(" ");
			buffer.append(sym.getName());
		}
		return buffer.toString();
	}

}
