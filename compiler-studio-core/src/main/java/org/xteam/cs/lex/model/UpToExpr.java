package org.xteam.cs.lex.model;

public class UpToExpr extends UnaryExpr {

	public UpToExpr(Expr expr) {
		super(expr);
	}

	@Override
	public void visit(IExprVisitor visitor) {
		visitor.visitUpTo(this);
	}

}
