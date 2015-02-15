/**
 * 
 */
package org.xteam.cs.runtime.semantic;

public class Error<T> extends Result<T> {
	public boolean isError() {
		return true;
	}

	@Override
	public T value() {
		return null;
	}
}