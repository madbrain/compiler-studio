/**
 * 
 */
package org.xteam.cs.model;

import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.io.Reader;
import java.util.Iterator;
import java.util.List;


public class ErrorMarkConsoleDiagnostic {
	
	private PrintStream out;

	public ErrorMarkConsoleDiagnostic(PrintStream out) {
		this.out = out;
	}

	public void printDiagnostic(String filename, List<ErrorMark> marks)
			throws IOException {
		printDiagnostic(new FileReader(filename), marks);
	}

	public void printDiagnostic(Reader stream, List<ErrorMark> marks)
			throws IOException {
		int lineNumber = 1;
		int lineStart = 0;
		StringBuffer line = new StringBuffer();
		while (stream.ready() && marks.size() > 0) {
			int c = stream.read();
			if (c == -1)
				break;
			if (c == '\n') {
				printError(lineNumber, lineStart, line, marks);
				lineStart += line.length() + 1;
				++lineNumber;
				line.setLength(0);
			} else
				line.append((char) c);
		}
		if (marks.size() > 0)
			printError(lineNumber, lineStart, line, marks);
	}

	private void printError(int lineNumber, int lineStart, StringBuffer line,
			List<ErrorMark> marks) {
		Iterator<ErrorMark> e = marks.iterator();
		while (e.hasNext()) {
			ErrorMark error = e.next();
			if (error.getSpan().start() >= lineStart
					&& error.getSpan().start() <= (lineStart + line
							.length())) {
				out.println(line.toString().replace('\t', ' '));
				int offset = error.getSpan().start() - lineStart;
				for (int i = 0; i < offset; ++i) {
					out.print(" ");
				}
				for (int i = 0; i < error.getSpan().length()
						&& i < (line.length() - offset); ++i) {
					out.print("^");
				}
				out.println();
				out.println("[" + lineNumber + "]: " + error.getMessage());
				out.println();
				e.remove();
			}
		}
	}
}