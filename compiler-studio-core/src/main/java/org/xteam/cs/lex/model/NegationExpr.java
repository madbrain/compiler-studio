package org.xteam.cs.lex.model;

public class NegationExpr extends UnaryExpr {

	public NegationExpr(Expr expr) {
		super(expr);
	}

	@Override
	public void visit(IExprVisitor visitor) {
		visitor.visitNegation(this);
	}

}
