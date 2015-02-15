package org.xteam.cs.model;

import org.xteam.cs.runtime.Span;

public class ParseError extends Exception {

	private static final long serialVersionUID = -2665705252440462640L;
	
	private Span span;
	
	public ParseError(Span span, String msg) {
		super(msg);
		this.span = span;
	}

	public Span getSpan() {
		return span;
	}

}
