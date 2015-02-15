package org.xteam.cs.runtime;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public abstract class LineColumnErrorReporter implements IErrorReporter {

	protected static class LineInfo {

		private int start;
		private int end;
		private int lineNumber;

		public LineInfo(int start, int lineNumber) {
			this.start = start;
			this.lineNumber = lineNumber;
		}

		public void setLength(int length) {
			this.end = this.start + length;
		}
		
		public int getLine() {
			return lineNumber;
		}

		public int getColumn(int position) {
			return position - start + 1;
		}

		@Override
		public String toString() {
			return lineNumber + " -> [" + start + "," + end + "]";
		}

	}

	private File file;
	private List<LineInfo> lineInfos;

	public LineColumnErrorReporter(File file) {
		this.file = file;
	}
	
	protected File getFile() {
		return file;
	}

	protected LineInfo getLineInfo(int start) {
		if (lineInfos == null) {
			buildLineMapping();
		}
		for (LineInfo info : lineInfos) {
			if (info.start <= start && start < info.end) {
				return info;
			}
		}
		return null;
	}

	private void buildLineMapping() {
		lineInfos = new ArrayList<LineInfo>();
		try {
			FileReader reader = new FileReader(file);
			int lineLength = 0;
			int lineStart = 0;
			int lineNumber = 1;
			LineInfo current = new LineInfo(lineStart, lineNumber);
			lineInfos.add(current);
			while (reader.ready()) {
				int c = reader.read();
				if (c == -1) {
					break;
				}
				++lineLength;
				if (c == '\n') {
					lineStart += lineLength;
					current.setLength(lineLength);
					++lineNumber;
					lineInfos.add(current = new LineInfo(lineStart, lineNumber));
					lineLength = 0;
				}
			}
			reader.close();
		} catch (IOException e) {
		}
	}
}
