package org.xteam.cs.runtime;


public interface ITokenFactory {

	IToken newToken(int type, Span span, Object content);

}
