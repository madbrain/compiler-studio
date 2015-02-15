package org.xteam.cs.ast.ast;

import org.xteam.cs.runtime.AstNode;
import org.xteam.cs.runtime.Span;

public abstract class TypeAst extends AstNode {

	public TypeAst(Span span) {
		super(span);
	}
	
	public abstract void visit(IAstVisitor visitor);

}
