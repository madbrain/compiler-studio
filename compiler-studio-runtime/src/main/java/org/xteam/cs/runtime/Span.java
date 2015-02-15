package org.xteam.cs.runtime;


public class Span {
	
	public static final Span NULL = new Span(-1, 0);

	private int start;
	private int length;

	public Span(int start, int length) {
		this.start = start;
		this.length = length;
	}

	public int start() {
		return start;
	}
	
	public int length() {
		return length;
	}

	public int end() {
		return start + length;
	}
	
	public Span merge(Span other) {
		if (other == null || other.start < 0)
			return this;
		if (start < 0)
			return other;
		int s = Math.min(start, other.start());
		int end = Math.max(end(), other.end());
		return new Span(s, end - s);
	}
	
	public static Span listSpan(AstList<? extends AstNode> nodes) {
		Span span = NULL;
		for (AstNode node : nodes) {
			span = span.merge(node.span());
		}
		return span;
	}
	
	@Override
	public String toString() {
		return "[" + start + ":" + end() + "]";
	}

}
