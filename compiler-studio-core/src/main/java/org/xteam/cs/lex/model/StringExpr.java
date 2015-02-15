package org.xteam.cs.lex.model;


public class StringExpr extends Expr {

	protected String value;
	
	public StringExpr(String value) {
		this.value = value;
	}

	@Override
	public void visit(IExprVisitor visitor) {
		visitor.visitString(this);
	}

	public String getValue() {
		return value;
	}
}
