package org.xteam.cs.lex.model;

public class InverseCharClassExpr extends CharClassExpr {

	@Override
	public void visit(IExprVisitor visitor) {
		visitor.visitInverseCharclass(this);
	}
}
