package org.xteam.cs.grm.ast;

import org.xteam.cs.runtime.AstList;
import org.xteam.cs.runtime.AstNode;
import org.xteam.cs.runtime.Span;

public class ConstructorAst extends AstNode {

	protected IdentAst name;
	protected AstList<ArgumentAst> arguments;

	public ConstructorAst(Span span, IdentAst name, AstList<ArgumentAst> arguments) {
		super(span);
		this.name = name;
		this.arguments = arguments;
	}

	public IdentAst getName() {
		return name;
	}

	public AstList<ArgumentAst> getArguments() {
		return arguments;
	}

	public void visit(IGrmVisitor visitor) {
		visitor.visitConstructor(this);
	}
}
