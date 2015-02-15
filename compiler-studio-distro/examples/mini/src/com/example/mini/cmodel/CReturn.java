package com.example.mini.cmodel;


public class CReturn extends CStatement {

	private CExpr expr;

	public CReturn(CExpr e) {
		this.expr = e;
	}
	
	public CExpr getExpr() {
		return expr;
	}

	@Override
	public void visit(IStatementVisitor visitor) {
		visitor.visitReturn(this);
	}

}
