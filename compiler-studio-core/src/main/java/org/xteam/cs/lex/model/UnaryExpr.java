package org.xteam.cs.lex.model;

public abstract class UnaryExpr extends Expr {
	
	protected Expr expr;
	
	public UnaryExpr(Expr expr) {
		this.expr = expr;
	}
	
	public Expr getExpr() {
		return expr;
	}
	
}
