
package org.xteam.cs.grm.ast;

import org.xteam.cs.runtime.AstNode;
import org.xteam.cs.runtime.Span;

public class StringLiteralAst extends AstNode {
	
	protected String value;
	
	public StringLiteralAst(Span span, String value) {
		super(span);
		this.value = value;
	}
	
	public String getValue() {
		return value;
	}
	
	public void visit(IGrmVisitor visitor) {
		visitor.visitStringLiteral(this);
	}
}
