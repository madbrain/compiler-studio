package org.xteam.cs.lex.model;

public class PlusExpr extends UnaryExpr {

	public PlusExpr(Expr expr) {
		super(expr);
	}

	@Override
	public void visit(IExprVisitor visitor) {
		visitor.visitPlus(this);
	}

}
