package org.xteam.cs.lex.model;

public class ConcatExpr extends NAryExpr {

	@Override
	public void visit(IExprVisitor visitor) {
		visitor.visitConcat(this);
	}

}
