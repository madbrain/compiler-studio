package org.xteam.cs.ast.ast;

import org.xteam.cs.runtime.Span;

public class SimpleTypeAst extends TypeAst {

	private String name;

	public SimpleTypeAst(Span span, String name) {
		super(span);
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
	
	@Override
	public void visit(IAstVisitor visitor) {
		visitor.visitSimpleType(this);
	}

}
