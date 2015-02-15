package org.xteam.cs.lex.model;

public class AnyCharExpr extends Expr {

	@Override
	public void visit(IExprVisitor visitor) {
		visitor.visitAnyChar(this);
	}

}
