package org.xteam.cs.lex.model;

public class UnionExpr extends NAryExpr {

	@Override
	public void visit(IExprVisitor visitor) {
		visitor.visitUnion(this);
	}

}
