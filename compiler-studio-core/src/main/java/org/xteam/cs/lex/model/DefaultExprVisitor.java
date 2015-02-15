package org.xteam.cs.lex.model;

public class DefaultExprVisitor implements IExprVisitor {
	
	@Override
	public void visitUnion(UnionExpr unionExpr) {
		for (Expr expr : unionExpr.getExprs()) {
			expr.visit(this);
		}
	}

	@Override
	public void visitConcat(ConcatExpr concatExpr) {
		for (Expr expr : concatExpr.getExprs()) {
			expr.visit(this);
		}
	}
	
	@Override
	public void visitStar(StarExpr starExpr) {
		starExpr.getExpr().visit(this);
	}
	
	@Override
	public void visitPlus(PlusExpr plusExpr) {
		plusExpr.getExpr().visit(this);
	}
	
	@Override
	public void visitQuestion(QuestionExpr questionExpr) {
		questionExpr.getExpr().visit(this);
	}
	
	@Override
	public void visitNegation(NegationExpr negationExpr) {
		negationExpr.getExpr().visit(this);
	}

	@Override
	public void visitUpTo(UpToExpr upToExpr) {
		upToExpr.getExpr().visit(this);
	}

	@Override
	public void visitString(StringExpr stringExpr) {
		
	}

	@Override
	public void visitCharclass(CharClassExpr charClassExpr) {
		
	}

	@Override
	public void visitInverseCharclass(InverseCharClassExpr inverseCharClassExpr) {
		
	}

	@Override
	public void visitAnyChar(AnyCharExpr anyCharExpr) {
		
	}

}
