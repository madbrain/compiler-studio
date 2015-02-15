package org.xteam.cs.runtime;

public interface ILexerTables {

	short[] charMapTable();
	short[] rowMapTable();
	short[] transitionTable();
	short[] attributeTable();
	LexerAction getAction(int action);
	LexerAction getEOFAction(int lexicalState);
}
