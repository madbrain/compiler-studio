package org.xteam.cs.grm.build;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.xteam.cs.runtime.IParseTables;

public class ParserTableFileEmiter {

	public void emit(IParseTables tables, File file) {
		TableOutputStream stream = null;
		try {
			stream = new TableOutputStream(new FileOutputStream(file));
			int stateCount = tables.actionTable().length;
			stream.writeValue(stateCount);
			for (int i = 0; i < stateCount; ++i) {
				stream.writeTable(tables.actionTable()[i]);
			}
			for (int i = 0; i < stateCount; ++i) {
				stream.writeTable(tables.reduceTable()[i]);
			}
			stream.writeValue(tables.productionTable().length);
			for (int i = 0; i < tables.productionTable().length; ++i) {
				stream.writeValue(tables.productionTable()[i][0]);
				stream.writeValue(tables.productionTable()[i][1]);
			}
			stream.writeValue(tables.acceptingProduction());
		} catch (IOException e) {

		} finally {
			try {
				if (stream != null)
					stream.close();
			} catch (IOException e) {
			}
		}
		
	}

}
