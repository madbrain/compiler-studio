package org.xteam.cs.grm.ast;

import org.xteam.cs.runtime.Span;

public class TupleArgumentAst extends ArgumentAst {

	private IdentAst first;
	private IdentAst second;

	public TupleArgumentAst(Span span, IdentAst first, IdentAst second) {
		super(span);
		this.first = first;
		this.second = second;
	}

	@Override
	public void visit(IGrmVisitor visitor) {
		visitor.visitTupleArgument(this);
	}

	public IdentAst getFirst() {
		return first;
	}
	
	public IdentAst getSecond() {
		return second;
	}

}
