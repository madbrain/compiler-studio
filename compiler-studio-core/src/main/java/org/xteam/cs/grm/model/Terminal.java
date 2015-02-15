package org.xteam.cs.grm.model;

import org.xteam.cs.types.Type;


public class Terminal extends Symbol {

	public Terminal(String name, Type type) {
		super(name);
		this.type = type;
	}

	@Override
	public boolean isTerminal() {
		return true;
	}
	
	@Override
	public boolean isConstant() {
		return type == null;
	}

}
