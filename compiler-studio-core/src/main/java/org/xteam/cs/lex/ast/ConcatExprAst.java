package org.xteam.cs.lex.ast;

import org.xteam.cs.runtime.AstList;
import org.xteam.cs.runtime.Span;

public class ConcatExprAst extends ExprAst {

	private AstList<ExprAst> exprs;

	public ConcatExprAst(Span span, AstList<ExprAst> exprs) {
		super(span);
		this.exprs = exprs;
	}
	
	@Override
	public void visit(ILexerAstVisitor visitor) {
		visitor.visitConcatExpr(this);
	}

	public AstList<ExprAst> getExprs() {
		return exprs;
	}

}
