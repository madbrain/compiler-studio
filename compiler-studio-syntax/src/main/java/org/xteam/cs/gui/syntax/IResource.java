package org.xteam.cs.gui.syntax;

import org.xteam.cs.runtime.IErrorReporter;
import org.xteam.cs.runtime.ILexer;

public interface IResource {

	String getType();
	
	ILexer getLexer();
	
	String getContents();
	
	void markDirty();
	
	void analyse(String text, IErrorReporter reporter);

}
