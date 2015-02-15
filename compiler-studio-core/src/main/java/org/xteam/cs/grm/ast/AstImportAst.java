package org.xteam.cs.grm.ast;

import org.xteam.cs.runtime.Span;

public class AstImportAst extends ImportAst {

	public AstImportAst(Span span, IdentAst name) {
		super(span, name);
	}

	@Override
	public void visit(IGrmVisitor visitor) {
		visitor.visitAstImport(this);
	}

}
