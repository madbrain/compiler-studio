package org.xteam.cs.runtime;

import java.io.IOException;
import java.io.Reader;


public interface ILexer {
	
	void skipComments(boolean doSkip);

	void setInput(Reader reader);
	
	IToken nextToken() throws IOException;

	void setErrorReporter(IErrorReporter reporter);

}
