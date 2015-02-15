package org.xteam.cs.lex.ast;

import org.xteam.cs.runtime.AstList;
import org.xteam.cs.runtime.Span;

public class UnionExprAst extends ExprAst {

	private AstList<ExprAst> exprs;

	public UnionExprAst(Span span, AstList<ExprAst> exprs) {
		super(span);
		this.exprs = exprs;
	}
	
	public AstList<ExprAst> getExprs() {
		return exprs;
	}
	
	@Override
	public void visit(ILexerAstVisitor visitor) {
		visitor.visitUnionExpr(this);
	}

}
