/**
 * 
 */
package org.xteam.cs.runtime.semantic;

public class Ok<T> extends Result<T> {
	
	T value;

	public Ok(T value) {
		this.value = value;
	}

	@Override
	public T value() {
		return value;
	}

}