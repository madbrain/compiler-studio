package org.xteam.cs.runtime;

import java.io.IOException;
import java.io.Reader;
import java.util.Stack;

public class ReaderLexerInput implements ILexerInput {

	private int position = 0;
	private Reader reader;
	private Stack<Integer> putbacks = new Stack<Integer>();

	public ReaderLexerInput(Reader reader) {
		this.reader = reader;
	}

	public int next() {
		if (putbacks.isEmpty()) {
			try {
				int c = reader.read();
				++position;
				return c;
			} catch (IOException e) {
				return -1;
			}
		}
		++position;
		return putbacks.pop();
	}

	public int position() {
		return position;
	}

	public void putBack(String contents) {
		for (int i = contents.length()-1; i >= 0; --i) {
			putbacks.push((int)contents.charAt(i));
		}
		position -= contents.length();
	}

}
