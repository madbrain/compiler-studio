package org.xteam.cs.grm.model;

import org.xteam.cs.types.Type;

public abstract class Symbol {

	protected String name;
	protected Type type;
	
	public Symbol(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}
	
	public Type getType() {
		return type;
	}

	public void setType(Type type) {
		this.type = type;
	}

	public boolean isTerminal() {
		return false;
	}

	public boolean isConstant() {
		return false;
	}
	
	@Override
	public String toString() {
		return name;
	}

}
