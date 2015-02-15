package org.xteam.cs.ast.model;

import org.xteam.cs.types.Type;

public class AstField {
	
	private String name;
	private Type type;
	
	public AstField(String name, Type type) {
		this.name = name;
		this.type = type;
	}

	public String getName() {
		return name;
	}

	public Type getType() {
		return type;
	}
}
