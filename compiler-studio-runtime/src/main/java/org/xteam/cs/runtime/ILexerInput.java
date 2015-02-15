package org.xteam.cs.runtime;

public interface ILexerInput {

	/**
	 * Return -1 if input is at EOF
	 * @return the next character of the stream
	 */
    int next();

    /**
	 * Restore size character from the input
	 * @param contents the number of character to restore
	 */
    void putBack(String contents);
    
    /**
     * Return the current position in the input.
     * @return the current position
     */
    int position();

}
