package org.xteam.cs.gui.grm;

import org.xteam.cs.runtime.IToken;

public class ASTTokenNode extends GenericAST implements IToken {

	private int length;
	private int start;
	private int type;
	private Object value;
	private String name;

	public ASTTokenNode(int type, String name, int start, int length,
			Object content) {
		this.type = type;
		this.name = name;
		this.start = start;
		this.length = length;
		this.value = content;
	}

	@Override
	public int length() {
		return length;
	}

	@Override
	public int start() {
		return start;
	}
	
	@Override
	public int end() {
		return start + length;
	}

	@Override
	public int type() {
		return type;
	}

	@Override
	public Object value() {
		return value;
	}
	
	@Override
	public String toString() {
		return value != null ? "'" + value.toString() + "'" : "null";
	}

	

}
