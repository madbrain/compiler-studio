package org.xteam.cs.ast.ast;

import org.xteam.cs.runtime.AstNode;
import org.xteam.cs.runtime.Span;

public class AbstractFlagAst extends AstNode {

	public AbstractFlagAst(Span span) {
		super(span);
	}

	public void visit(IAstVisitor visitor) {
		visitor.visitAbstractFlag(this);
	}

}