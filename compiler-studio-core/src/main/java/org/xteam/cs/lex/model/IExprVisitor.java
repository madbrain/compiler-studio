package org.xteam.cs.lex.model;

public interface IExprVisitor {
	
	void visitUnion(UnionExpr unionExpr);

	void visitConcat(ConcatExpr concatExpr);

	void visitString(StringExpr stringExpr);

	void visitStar(StarExpr starExpr);
	
	void visitPlus(PlusExpr plusExpr);

	void visitCharclass(CharClassExpr charClassExpr);
	
	void visitInverseCharclass(InverseCharClassExpr inverseCharClassExpr);

	void visitQuestion(QuestionExpr questionExpr);

	void visitNegation(NegationExpr negationExpr);

	void visitUpTo(UpToExpr upToExpr);

	void visitAnyChar(AnyCharExpr anyCharExpr);

}
