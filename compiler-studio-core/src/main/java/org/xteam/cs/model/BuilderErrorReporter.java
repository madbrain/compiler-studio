package org.xteam.cs.model;

import org.xteam.cs.runtime.IErrorReporter;
import org.xteam.cs.runtime.Span;

public class BuilderErrorReporter implements IErrorReporter {
	
	private ProjectResource file;
	private boolean hasErrors = false;
	
	public BuilderErrorReporter(ProjectResource file) {
		this.file = file;
	}

	@Override
	public void reportError(int level, Span span, String msg) {
		file.addMark(new ErrorMark(file, level, span, msg));
		hasErrors |= level == IErrorReporter.ERROR;
	}

	@Override
	public boolean hasErrors() {
		return hasErrors;
	}
	
}