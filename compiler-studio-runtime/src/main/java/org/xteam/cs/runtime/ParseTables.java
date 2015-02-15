package org.xteam.cs.runtime;

import java.io.IOException;
import java.io.InputStream;


public class ParseTables implements IParseTables {

	private short[][] actionTable;
	private short[][] reduceTable;
	private short[][] productionTable;
	private int acceptingRule;
	
	public ParseTables(short[][] actionTable, short[][] reduceTable, short[][] productionTable, int acceptingRule) {
		this.actionTable = actionTable;
		this.reduceTable = reduceTable;
		this.productionTable = productionTable;
		this.acceptingRule = acceptingRule;
	}

	public ParseTables(Class<?> cls, String filename) throws IOException {
		load(cls.getResourceAsStream(filename));
	}

	private void load(InputStream str) throws IOException {
		TableInputStream stream = new TableInputStream(str);
		int stateCount = stream.readValue();
		actionTable = new short[stateCount][];
		for (int i = 0; i < stateCount; ++i) {
			actionTable[i] = stream.readSimpleTable();
		}
		reduceTable = new short[stateCount][];
		for (int i = 0; i < stateCount; ++i) {
			reduceTable[i] = stream.readSimpleTable();
		}
		productionTable = new short[stream.readValue()][];
		for (int i = 0; i < productionTable.length; ++i) {
			productionTable[i] = stream.readSimpleTable(2);
		}
		acceptingRule = stream.readValue();
	}

	public int acceptingProduction() {
		return acceptingRule;
	}

	public short[][] actionTable() {
		return actionTable;
	}

	public short[][] productionTable() {
		return productionTable;
	}

	public short[][] reduceTable() {
		return reduceTable;
	}

	public int startState() {
		return 0;
	}

}
