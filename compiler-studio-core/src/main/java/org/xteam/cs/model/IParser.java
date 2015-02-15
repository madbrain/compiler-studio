package org.xteam.cs.model;

import java.io.IOException;
import java.io.Reader;

import org.xteam.cs.runtime.AstNode;
import org.xteam.cs.runtime.IErrorReporter;

public interface IParser {

	void setInput(Reader reader);

	void setErrorReporter(IErrorReporter reporter);

	AstNode parse() throws IOException, ParseError;

}
