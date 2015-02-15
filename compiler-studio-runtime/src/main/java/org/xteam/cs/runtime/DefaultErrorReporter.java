package org.xteam.cs.runtime;

import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


public class DefaultErrorReporter implements IErrorReporter {

	private static class Error {
		
		Span span;
		String msg;
		int level;

		public Error(int level, Span span, String msg) {
			this.level = level;
			this.span = span;
			this.msg = msg;
		}
	}
	
	private List<Error> errors = new ArrayList<Error>();
	private PrintStream out;
	private boolean hasErrors = false;
	
	public DefaultErrorReporter(PrintStream out) {
		this.out = out;
	}

	public void reportError(int level, Span span, String msg) {
		hasErrors |= level == IErrorReporter.ERROR;
		errors.add(new Error(level, span, msg));
	}
	
	@Override
	public boolean hasErrors() {
		return hasErrors;
	}

	public void printDiagnostic(String filename) throws IOException {
		printDiagnostic(new FileReader(filename));
	}
	
	public void printDiagnostic(Reader stream) throws IOException {
		int lineNumber = 1;
		int lineStart = 0;
		StringBuffer line = new StringBuffer();
		while (stream.ready() && errors.size() > 0) {
			int c = stream.read();
			if (c == -1)
				break;
			if (c == '\n') {
				printError(lineNumber, lineStart, line);
				lineStart += line.length() + 1;
				++lineNumber;
				line.setLength(0);
			} else
				line.append((char)c);
		}
		if (errors.size() > 0) {
			printError(lineNumber, lineStart, line);
		}
	}

	private void printError(int lineNumber, int lineStart, StringBuffer line) {
		Iterator<Error> e = errors.iterator();
		while (e.hasNext()) {
			Error error = e.next();
			if (error.span.start() >= lineStart && error.span.start() <= (lineStart + line.length())) {
				out.println(line.toString().replace('\t', ' '));
				int offset = error.span.start() - lineStart;
				for (int i = 0; i < offset; ++i) {
					out.print(" ");
				}
				for (int i = 0; i < error.span.length() && i < (line.length() - offset); ++i) {
					out.print("^");
				}
				out.println();
				out.println("[" + lineNumber + "] " + getLevel(error) + ": " + error.msg);
				out.println();
				e.remove();
			}
		}
	}

	private String getLevel(Error error) {
		return error.level == IErrorReporter.WARNING ? "warning" : "error";
	}
}
