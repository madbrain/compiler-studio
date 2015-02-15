package org.xteam.cs.runtime;



public class DefaultToken implements IToken {
	
	protected int type;
	protected int start;
	protected int length;
	protected Object value;
	
	public DefaultToken(int type, int start, int length) {
		this(type, start, length, null);
	}

	public DefaultToken(int type, int start, int length, Object value) {
		this.type = type;
		this.start = start;
		this.length = length;
		this.value = value;
	}
	
	public int type() {
		return type;
	}

	public int length() {
		return length;
	}

	public int start() {
		return start;
	}

	public Object value() {
		return value;
	}

	public int end() {
		return start + length;
	}

}
