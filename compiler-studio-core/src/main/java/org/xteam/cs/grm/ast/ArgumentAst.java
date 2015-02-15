package org.xteam.cs.grm.ast;

import org.xteam.cs.runtime.AstNode;
import org.xteam.cs.runtime.Span;

public abstract class ArgumentAst extends AstNode {

	public ArgumentAst(Span span) {
		super(span);
	}

	public abstract void visit(IGrmVisitor visitor);

}
