package org.xteam.cs.lex.model;

public class StarExpr extends UnaryExpr {

	public StarExpr(Expr expr) {
		super(expr);
	}

	@Override
	public void visit(IExprVisitor visitor) {
		visitor.visitStar(this);
	}

}
