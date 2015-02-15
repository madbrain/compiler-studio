package org.xteam.cs.runtime;

public interface IToken {

	int type();

	int start();
	
	int length();
	
	int end();
	
	Object value();

}
