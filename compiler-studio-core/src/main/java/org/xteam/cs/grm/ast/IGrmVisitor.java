
package org.xteam.cs.grm.ast;

public interface IGrmVisitor {

	void visitGrmFile(GrammarFileAst aGrmFile);
	
	void visitLexerImport(LexerImportAst aLexerImportAst);
	
	void visitAstImport(AstImportAst aAstImportAst);
	
	void visitTerminalDeclaration(TerminalDeclarationAst aTerminalDeclaration);
	
	void visitStartDeclaration(StartDeclarationAst aStartDeclaration);
	
	void visitLexerActionDeclaration(LexerActionDeclarationAst aLexerActionDeclaration);
	
	void visitRule(RuleAst aRule);
	
	void visitConstructor(ConstructorAst aConstructor);
	
	void visitIdent(IdentAst aIdent);
	
	void visitStringLiteral(StringLiteralAst aStringLiteral);

	void visitTupleArgument(TupleArgumentAst tupleArgumentAst);

	void visitIdentArgument(IdentArgumentAst identArgumentAst);
	
}
