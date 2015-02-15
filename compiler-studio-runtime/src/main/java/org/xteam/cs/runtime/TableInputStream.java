package org.xteam.cs.runtime;

import java.io.IOException;
import java.io.InputStream;

public class TableInputStream {
	
	private InputStream stream;

	public TableInputStream(InputStream str) {
		stream = str;
	}

	public short[] readRLETable() throws IOException {
		int length = readValue();
		int max = readValue();
		short[] table = new short[max+1];
		int index = 0;
		for (int i = 0; i < length; ++i) {
			int count = readValue();
			short value = (short) readValue();
			while (count-- > 0 && index <= max) {
				table[index++] = value;
			}
		}
		return table;
	}
	
	public short[] readSimpleTable(int length) throws IOException {
		short[] table = new short[length];
		for (int i = 0; i < length; ++i)
			table[i] = (short) readValue();
		return table;
	}
	
	public short[] readSimpleTable() throws IOException {
		return readSimpleTable(readValue());
	}
	
	public int readValue() throws IOException {
		int b = stream.read();
		return (b<<8) | stream.read();
	}
	
	public String readString() throws IOException {
		StringBuilder builder = new StringBuilder();
		int b;
		while ((b = stream.read()) != 0) {
			builder.append((char)b);
		}
		return builder.toString();
	}
}
