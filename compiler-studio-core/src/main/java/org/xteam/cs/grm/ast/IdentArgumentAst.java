package org.xteam.cs.grm.ast;

import org.xteam.cs.runtime.Span;

public class IdentArgumentAst extends ArgumentAst {

	private String name;

	public IdentArgumentAst(Span span, String value) {
		super(span);
		this.name = value;
	}

	@Override
	public void visit(IGrmVisitor visitor) {
		visitor.visitIdentArgument(this);
	}

	public String getName() {
		return name;
	}

}
