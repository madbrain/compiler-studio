package org.xteam.cs.runtime.inc;

public interface IIncToken {

	int getLength();

	IIncToken nextToken();

	int charAt(int index);

	String getText();

}
