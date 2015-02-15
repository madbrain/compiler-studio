package com.example.mini.cmodel;

import com.example.mini.IExprVisitor;


public class CBinary extends CExpr {

	public static final int ADD = 0;

	public static final int MUL = 1;
	
	private int op;
	private MiniType type;
	private CExpr left;
	private CExpr right;

	public CBinary(int op, MiniType type, CExpr left, CExpr right) {
		this.op = op;
		this.type = type;
		this.left = left;
		this.right = right;
	}

	@Override
	public MiniType getType() {
		return type;
	}

	@Override
	public void visit(IExprVisitor visitor) {
		visitor.visitBinary(this);
	}

	public CExpr getLeft() {
		return left;
	}
	
	public CExpr getRight() {
		return right;
	}

	public int getOp() {
		return op;
	}

}
