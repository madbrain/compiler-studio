package org.xteam.cs.runtime;


public interface IErrorReporter {

	public static final int WARNING = 0;
	public static final int ERROR   = 1;
	
	void reportError(int level, Span span, String msg);

	boolean hasErrors();

}
