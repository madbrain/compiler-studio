package org.xteam.cs.lex.ast;

import org.xteam.cs.runtime.AstNode;
import org.xteam.cs.runtime.Span;

public abstract class ExprAst extends AstNode {

	public ExprAst(Span span) {
		super(span);
	}
	
	public abstract void visit(ILexerAstVisitor visitor);

}
