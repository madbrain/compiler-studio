package org.xteam.cs.grm.ast;

import org.xteam.cs.runtime.Span;

public class LexerImportAst extends ImportAst {

	public LexerImportAst(Span span, IdentAst name) {
		super(span, name);
	}

	@Override
	public void visit(IGrmVisitor visitor) {
		visitor.visitLexerImport(this);
	}

}
