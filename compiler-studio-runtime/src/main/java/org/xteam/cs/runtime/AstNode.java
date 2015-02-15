package org.xteam.cs.runtime;


public abstract class AstNode {
	
	protected Span span;

	public AstNode(Span span) {
		this.span = span;
	}

	public Span span() {
		return span;
	}

}
