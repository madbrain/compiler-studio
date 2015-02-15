package org.xteam.cs.lex.ast;

public interface ILexerAstVisitor {

	void visitCharclassExpr(CharclassExprAst charclassExprAst);

	void visitConcatExpr(ConcatExprAst concatExprAst);

	void visitMacroExpr(MacroExprAst macroExprAst);

	void visitStarExpr(StarExprAst starExprAst);

	void visitUnionExpr(UnionExprAst unionExprAst);

	void visitPlusExpr(PlusExprAst plusExprAst);

	void visitStringExpr(StringExprAst stringExprAst);

	void visitEof(EofExprAst eofExprAst);

	void visitQuestion(QuestionExprAst questionExprAst);

	void visitAnyChar(AnyCharExprAst anyCharExprAst);

	void visitUpToExpr(UpToExprAst upToExprAst);

}
