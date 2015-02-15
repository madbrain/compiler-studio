package org.xteam.cs.grm.ast;

public class DefaultGrmVisitor implements IGrmVisitor {

	@Override
	public void visitGrmFile(GrammarFileAst aGrmFile) {
		for (ImportAst imp : aGrmFile.getImports()) {
			imp.visit(this);
		}
		for (DeclarationAst e : aGrmFile.getDeclarations()) {
			e.visit(this);
		}
		for (RuleAst e : aGrmFile.getRules()) {
			e.visit(this);
		}
	}

	@Override
	public void visitAstImport(AstImportAst aAstImportAst) {
		
	}

	@Override
	public void visitLexerImport(LexerImportAst aLexerImportAst) {
		
	}

	@Override
	public void visitTerminalDeclaration(
			TerminalDeclarationAst aTerminalDeclaration) {
		for (IdentAst e : aTerminalDeclaration.getTerminals()) {
			e.visit(this);
		}
	}

	@Override
	public void visitStartDeclaration(StartDeclarationAst aStartDeclaration) {
		aStartDeclaration.getNonTerminal().visit(this);
	}

	@Override
	public void visitLexerActionDeclaration(
			LexerActionDeclarationAst aLexerActionDeclaration) {
		for (IdentAst e : aLexerActionDeclaration.getActions()) {
			e.visit(this);
		}
	}

	@Override
	public void visitRule(RuleAst aRule) {
		aRule.getLhs().visit(this);
		for (IdentAst e : aRule.getRhs()) {
			e.visit(this);
		}
		aRule.getConstructor().visit(this);
	}

	@Override
	public void visitConstructor(ConstructorAst aConstructor) {
		aConstructor.getName().visit(this);
		for (ArgumentAst e : aConstructor.getArguments()) {
			e.visit(this);
		}
	}

	@Override
	public void visitIdent(IdentAst aIdent) {
	}

	@Override
	public void visitStringLiteral(StringLiteralAst aStringLiteral) {
	}

	@Override
	public void visitIdentArgument(IdentArgumentAst identArgumentAst) {
		
	}

	@Override
	public void visitTupleArgument(TupleArgumentAst tupleArgumentAst) {
		
	}

}
