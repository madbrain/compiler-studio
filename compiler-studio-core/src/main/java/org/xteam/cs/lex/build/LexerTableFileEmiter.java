package org.xteam.cs.lex.build;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.xteam.cs.grm.build.TableOutputStream;
import org.xteam.cs.runtime.ILexerTables;
import org.xteam.cs.runtime.IStateAttributes;
import org.xteam.cs.runtime.LexerAction;

public class LexerTableFileEmiter implements IStateAttributes {

	private TableOutputStream stream;

	public void emit(ILexerTables tables, int numStates, File file) {
		try {
			stream = new TableOutputStream(new FileOutputStream(file));
			stream.writeRLETable(tables.charMapTable());
			stream.writeTable(tables.rowMapTable());
			stream.writeRLETable(tables.transitionTable());
			stream.writeRLETable(tables.attributeTable());
			int num = emitActionMap(tables);
			emitActions(num, tables);
			emitEOFActions(tables, numStates);
		} catch (IOException e) {

		} finally {
			try {
				stream.close();
			} catch (IOException e) {
			}
		}
	}
	
	private int emitActionMap(ILexerTables tables) throws IOException {
		int count = 0;
		int numStates = tables.rowMapTable().length;
		short[] actionMapTable = new short[numStates];
		for (int i = 0; i < numStates; i++) {
			LexerAction action = tables.getAction(i);
			if (action != null) {
				actionMapTable[i] = (short)count++;
			} else {
				actionMapTable[i] = -1;
			}
		}
		stream.writeRLETable(actionMapTable);
		return count;
	}
	
	private void emitActions(int num, ILexerTables tables) throws IOException {
		int numStates = tables.rowMapTable().length;
		stream.writeValue(num);
		for (int i = 0; i < numStates; ++i) {
			LexerAction action = tables.getAction(i);
			if (action != null) {
				emitAction(action);
			}
		}
	}

	private void emitEOFActions(ILexerTables tables, int numStates) throws IOException {
		stream.writeValue(numStates);
		for (int i = 0; i < numStates; ++i) {
			emitAction(tables.getEOFAction(i));
		}
	}
	
	private void emitAction(LexerAction action) throws IOException {
		stream.writeValue(action.code);
		if ((action.code & LexerAction.ERROR) != 0) {
			//emitValue(action.getErrorString());
			throw new RuntimeException("error not implemented yet");
		}
		if ((action.code & LexerAction.NEXT) != 0) {
			stream.writeValue(action.nextValue);
		}
		if ((action.code & LexerAction.TOKEN) != 0) {
			stream.writeValue(action.returnValue);
		}
		if ((action.code & LexerAction.CONVERT) != 0) {
			stream.writeValue(action.convertCode);
		}
		if ((action.code & LexerAction.KEYWORDS) != 0) {
			stream.writeValue(action.keywordMap.size());
			for (String keyword : action.keywordMap.keySet()) {
				stream.writeString(keyword);
				stream.writeValue(action.keywordMap.get(keyword));
			}
		}
	}
	
}
