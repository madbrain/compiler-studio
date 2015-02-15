package com.example.mini.cmodel;


public class CAssignment extends CStatement {

	private String var;
	private CExpr expr;

	public CAssignment(String var, CExpr expr) {
		this.var = var;
		this.expr = expr;
	}

	@Override
	public void visit(IStatementVisitor visitor) {
		visitor.visitAssignment(this);
	}

	public CExpr getExpr() {
		return expr;
	}

	public String getVar() {
		return var;
	}

}
