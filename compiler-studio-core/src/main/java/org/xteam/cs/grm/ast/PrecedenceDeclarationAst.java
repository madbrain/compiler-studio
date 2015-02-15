package org.xteam.cs.grm.ast;

import org.xteam.cs.runtime.AstList;
import org.xteam.cs.runtime.Span;


public class PrecedenceDeclarationAst extends DeclarationAst {

	public static final int NONASSOC = 0;
	public static final int LEFT     = 1;
	public static final int RIGHT    = 2;
	
	private int kind;
	private AstList<IdentAst> idents;

	public PrecedenceDeclarationAst(Span span, int kind, AstList<IdentAst> idents) {
		super(span);
		this.kind = kind;
		this.idents = idents;
	}

	@Override
	public void visit(IGrmVisitor visitor) {
		throw new RuntimeException();
	}

}
