package org.xteam.cs.lex.model;

import java.util.ArrayList;
import java.util.List;

public abstract class NAryExpr extends Expr {

	protected List<Expr> exprs = new ArrayList<Expr>();
	
	public void add(Expr expr) {
		exprs.add(expr);
	}
	
	public List<Expr> getExprs() {
		return exprs;
	}
}
