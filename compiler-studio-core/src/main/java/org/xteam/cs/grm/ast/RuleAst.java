package org.xteam.cs.grm.ast;

import org.xteam.cs.runtime.AstList;
import org.xteam.cs.runtime.AstNode;
import org.xteam.cs.runtime.Span;

public class RuleAst extends AstNode {

	protected IdentAst lhs;
	protected AstList<IdentAst> rhs;
	protected ConstructorAst constructor;
	protected IdentAst prec;

	public RuleAst(Span span, IdentAst lhs, AstList<IdentAst> rhs,
			IdentAst prec, ConstructorAst constructor) {
		super(span);
		this.lhs = lhs;
		this.rhs = rhs;
		this.prec = prec;
		this.constructor = constructor;
	}

	public IdentAst getLhs() {
		return lhs;
	}

	public void setLhs(IdentAst lhs) {
		this.lhs = lhs;
	}

	public AstList<IdentAst> getRhs() {
		return rhs;
	}

	public ConstructorAst getConstructor() {
		return constructor;
	}

	public void visit(IGrmVisitor visitor) {
		visitor.visitRule(this);
	}
}
