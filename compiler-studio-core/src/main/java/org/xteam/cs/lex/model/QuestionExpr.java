package org.xteam.cs.lex.model;

public class QuestionExpr extends UnaryExpr {

	public QuestionExpr(Expr expr) {
		super(expr);
	}

	@Override
	public void visit(IExprVisitor visitor) {
		visitor.visitQuestion(this);
	}

}
