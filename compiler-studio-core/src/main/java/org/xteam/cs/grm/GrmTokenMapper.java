package org.xteam.cs.grm;

import org.xteam.cs.runtime.DefaultToken;
import org.xteam.cs.runtime.IToken;
import org.xteam.cs.runtime.ITokenFactory;
import org.xteam.cs.runtime.Span;
import org.xteam.cs.syntax.ISyntaxToken;

public class GrmTokenMapper implements ITokenFactory {

	@Override
	public IToken newToken(int type, Span span, Object content) {
		if (type == IGrammarTokens.GRAMMAR
				|| type == IGrammarTokens.TERMINAL
				|| type == IGrammarTokens.START
				|| type == IGrammarTokens.LEXER
				|| type == IGrammarTokens.AST) {
			return new DefaultToken(ISyntaxToken.KEYWORD, span.start(), span.length());
		}
		if (type == IGrammarTokens.EQUALS || type == IGrammarTokens.ARROW) {
			return new DefaultToken(ISyntaxToken.OPERATOR, span.start(), span.length());
		}
		if (type == IGrammarTokens.COMMENT) {
			return new DefaultToken(ISyntaxToken.COMMENT, span.start(), span.length());
		}
		if (type == IGrammarTokens.STRING) {
			return new DefaultToken(ISyntaxToken.STRING, span.start(), span.length());
		}
		if (type == IGrammarTokens.EOF) {
			return null;
		}
		return new DefaultToken(ISyntaxToken.DEFAULT, span.start(), span.length());
	}

}
