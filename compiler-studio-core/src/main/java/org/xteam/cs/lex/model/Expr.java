package org.xteam.cs.lex.model;


public abstract class Expr {

	public abstract void visit(IExprVisitor visitor);

}
