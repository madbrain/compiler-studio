package org.xteam.cs.ast;

import org.xteam.cs.runtime.DefaultToken;
import org.xteam.cs.runtime.IToken;
import org.xteam.cs.runtime.ITokenFactory;
import org.xteam.cs.runtime.Span;
import org.xteam.cs.syntax.ISyntaxToken;

public class AstTokenMapper implements ITokenFactory {

	@Override
	public IToken newToken(int type, Span span, Object content) {
		if (type == AstTokens.IDENT) {
			String name = (String) content;
			if (name.equals("string") || name.equals("bool") || name.equals("int"))
				return new DefaultToken(ISyntaxToken.TYPES, span.start(), span.length());
			return new DefaultToken(ISyntaxToken.DEFAULT, span.start(), span.length());
		}
		if (type == AstTokens.COMMENT) {
			return new DefaultToken(ISyntaxToken.COMMENT, span.start(), span.length());
		}
		if (type == AstTokens.EOF) {
			return null;
		}
		return new DefaultToken(ISyntaxToken.DEFAULT, span.start(), span.length());
	}

}
