package org.xteam.cs.lex.build;

import java.util.List;

import org.xteam.cs.lex.model.Action;
import org.xteam.cs.lex.model.ActionCode;
import org.xteam.cs.lex.model.LexerModel;
import org.xteam.cs.lex.model.LexicalState;
import org.xteam.cs.runtime.ILexerTables;
import org.xteam.cs.runtime.IStateAttributes;
import org.xteam.cs.runtime.LexerAction;
import org.xteam.cs.runtime.LexerTable;

public class LexerTableBuilder implements IStateAttributes {

	private int[] rowMap;

	private boolean[] rowKilled;

	private int numRows;

	private boolean[] isTransition;

	private LexerMapping lexerMapping;
	
	private LexerModel model;

	public LexerTableBuilder(LexerModel model) {
		this.model = model;
	}

	public ILexerTables build(CharClasses charClasses,
			LexerMapping lexerMapping, DFA dfa) {
		reduceRows(dfa);
		reduceColumns(dfa, charClasses);
		findActionStates(dfa);
		this.lexerMapping = lexerMapping;
		return buildLexerTables(charClasses, dfa);
	}
	
	private ILexerTables buildLexerTables(CharClasses charClasses, DFA dfa) {
		short[] charmapTable = buildCharMapTable(charClasses);
		short[] rowmapTable = buildRowMapTable(dfa);
		short[] transitionTable = buildTransitionTable(dfa);
		short[] attributeTable = buildAttributeTable(dfa);
		LexerAction[] actionTable = buildActions(dfa);
		LexerAction[] eofActionsTable = buildEOFActions(model);
		return new LexerTable(charmapTable, rowmapTable, transitionTable, attributeTable, actionTable, eofActionsTable);
	}
	
	private short[] buildCharMapTable(CharClasses charClasses) {
		short[] table = new short[charClasses.getMaxCharCode()];
		CharClassInterval[] intervals = charClasses.getIntervals();
		for (int i = 0; i < intervals.length; ++i) {
			for (int c = intervals[i].start; c <= intervals[i].end; ++c) {
				if (c >= table.length)
					break;
				table[c] = (short)intervals[i].charClass;
			}
		}
		return table;
	}
	
	private short[] buildRowMapTable(DFA dfa) {
		short[] table = new short[dfa.numStates];
		for (int i = 0; i < dfa.numStates; ++i) {
			table[i] = (short)(rowMap[i] * dfa.numInput);
		}
		return table;
	}
	
	private short[] buildTransitionTable(DFA dfa) {
		short[] transitionTable = new short[numRows * dfa.numInput];
		int j = 0;
		for (int i = 0; i < dfa.numStates; i++) {
			if (!rowKilled[i]) {
				for (int c = 0; c < dfa.numInput; c++) {
					transitionTable[j++] = (short) dfa.table[i][c];
				}
			}
		}
		short[] table = new short[j];
		System.arraycopy(transitionTable, 0, table, 0, j);
		return table;
	}
	
	private short[] buildAttributeTable(DFA dfa) {
		short[] attributeTable = new short[dfa.numStates];
		int j = 0;
		for (int i = 0; i < dfa.numStates; i++) {
			int attribute = 0;
			if (dfa.isFinal[i])
				attribute = FINAL;
			if (dfa.isPushback[i])
				attribute |= PUSHBACK;
			if (dfa.isLookEnd[i])
				attribute |= LOOKEND;
			if (!isTransition[i])
				attribute |= NOLOOK;
			attributeTable[j++] = (short) attribute;
		}
		short[] table = new short[j];
		System.arraycopy(attributeTable, 0, table, 0, j);
		return table;
	}
	
	private LexerAction[] buildActions(DFA dfa) {
		LexerAction[] table = new LexerAction[dfa.numStates];
		for (int i = 0; i < dfa.numStates; ++i) {
			if (dfa.action[i] != null) {
				table[i] = makeAction(dfa.action[i]);
			}
		}
		return table;
	}
	
	private LexerAction[] buildEOFActions(LexerModel lexerModel) {
		LexerAction[] table = new LexerAction[lexerModel.getStates().size()];
		for (LexicalState state : lexerModel.getStates()) {
			Action action = state.getEofAction();
			if (action == null) {
				action = lexerModel.getDefaultAction();
			}
			table[lexerMapping.getStateNumber(state)] = makeAction(action);
		}
		return table;
	}
	
	private LexerAction makeAction(Action action) {
		int code = getCodeValue(action.getCodes());
		LexerAction lexerAction = new LexerAction(code,
				lexerMapping.getTokenNumber(action.getToken()),
				lexerMapping.getStateNumber(action.getNext()),
				lexerMapping.getConvertCode(action.getConvertCode()));
		if ((code & LexerAction.KEYWORDS) != 0) {
			lexerAction.setKeywordMap(lexerMapping.getKeywordMapping(action.getToken()));
		}
		return lexerAction;
	}
	
	private int getCodeValue(List<ActionCode> codes) {
		int code = 0;
		for (ActionCode c : codes) {
			code |= c.getValue();
		}
		return code;
	}

	protected void findActionStates(DFA dfa) {
		isTransition = new boolean[dfa.numStates];
		for (int i = 0; i < dfa.numStates; i++) {
			char j = 0;
			while (!isTransition[i] && j < dfa.numInput)
				isTransition[i] = dfa.table[i][j++] != DFA.NO_TARGET;
		}
	}
	
	protected void reduceColumns(DFA dfa, CharClasses classes) {
		int numCols = dfa.numInput;
		for (int i = 0; i < dfa.numInput; i++) {

			//rowMap[i] = i - translate;

			for (int j = 0; j < i; j++) {

				int k = -1;
				boolean equal = true;
				while (equal && ++k < dfa.numStates)
					equal = dfa.table[k][i] == dfa.table[k][j];

				if (equal) {
					//translate++;
					//rowMap[i] = rowMap[j];
					//rowKilled[i] = true;
					//System.out.println("col "+classes.toString(i)+"=="+classes.toString(j));
					numCols--;
					break;
				} // if
			} // for j
		} // for i
	}

	protected void reduceRows(DFA dfa) {
		rowMap = new int[dfa.numStates];
		rowKilled = new boolean[dfa.numStates];

		int translate = 0;

		numRows = dfa.numStates;

		// i is the state to add to the new table
		for (int i = 0; i < dfa.numStates; i++) {

			rowMap[i] = i - translate;

			// check if state i can be removed (i.e. already
			// exists in entries 0..i-1)
			for (int j = 0; j < i; j++) {

				// test for equality:
				int k = -1;
				boolean equal = true;
				while (equal && ++k < dfa.numInput)
					equal = dfa.table[i][k] == dfa.table[j][k];

				if (equal) {
					translate++;
					rowMap[i] = rowMap[j];
					rowKilled[i] = true;
					numRows--;
					break;
				} // if
			} // for j
		} // for i
	}

}
