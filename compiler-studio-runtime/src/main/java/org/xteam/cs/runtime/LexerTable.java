package org.xteam.cs.runtime;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class LexerTable implements ILexerTables {

	private short[] charMapTable;
	private short[] rowMapTable;
	private short[] transitionTable;
	private short[] attributeTable;
	private short[] actionMap;
	private LexerAction[] actions;
	private LexerAction[] eofActions;

	public LexerTable(InputStream stream) throws IOException {
		if (stream == null) {
			throw new IllegalArgumentException("stream cannot be null");
		}
		initializeFrom(stream);
	}
	
	public LexerTable(Class<?> cls, String filename) throws IOException {
		this(cls.getResourceAsStream(filename));
	}

	public LexerTable(short[] charmapTable, short[] rowmapTable,
			short[] transitionTable, short[] attributeTable,
			LexerAction[] actionTable,
			LexerAction[] eofActionsTable) {
		this.charMapTable = charmapTable;
		this.rowMapTable = rowmapTable;
		this.transitionTable = transitionTable;
		this.attributeTable = attributeTable;
		this.actions = actionTable;
		this.eofActions = eofActionsTable;
	}

	private void initializeFrom(InputStream str) throws IOException {
		TableInputStream stream = new TableInputStream(str);
		charMapTable = stream.readRLETable();
		rowMapTable = stream.readSimpleTable();
		transitionTable = stream.readRLETable();
		attributeTable = stream.readRLETable();
		actionMap = stream.readRLETable();
		actions = readActions(stream);
		eofActions = readActions(stream);
	}

	private static LexerAction[] readActions(TableInputStream stream) throws IOException {
		LexerAction[] actions = new LexerAction[stream.readValue()];
		for (int i = 0; i < actions.length; ++i) {
			int code = stream.readValue();
			int token = -1;
			int nextState = -1;
			int convertCode = -1;
			Map<String, Integer> keywordMap = new HashMap<String, Integer>();
			if ((code & LexerAction.ERROR) != 0) {
				//emitValue(action.getErrorString());
				throw new RuntimeException("error not implemented yet");
			}
			if ((code & LexerAction.NEXT) != 0) {
				nextState = stream.readValue();
			}
			if ((code & LexerAction.TOKEN) != 0) {
				token = stream.readValue();
			}
			if ((code & LexerAction.CONVERT) != 0) {
				convertCode = stream.readValue();
			}
			actions[i] = new LexerAction(code, token, nextState, convertCode);
			if ((code & LexerAction.KEYWORDS) != 0) {
				int len = stream.readValue();
				for (int j = 0; j < len; ++j) {
					String keyword = stream.readString();
					int value = stream.readValue();
					keywordMap.put(keyword, value);
				}
				actions[i].setKeywordMap(keywordMap);
			}
		}
		return actions;
	}
	
	public short[] charMapTable() {
		return charMapTable;
	}
	
	public short[] rowMapTable() {
		return rowMapTable;
	}

	public short[] transitionTable() {
		return transitionTable;
	}
	
	public short[] attributeTable() {
		return attributeTable;
	}

	public LexerAction getAction(int state) {
		if (actionMap == null)
			return actions[state];
		return actions[actionMap[state]];
	}

	public LexerAction getEOFAction(int lexicalState) {
		return eofActions[lexicalState];
	}

}
