package org.xteam.cs.model;

import org.xteam.cs.runtime.Span;

public class ErrorMark implements IMark {

	private int level;
	private Span span;
	private String msg;
	private ProjectResource resource;

	public ErrorMark(ProjectResource resource, int level, Span span, String msg) {
		this.resource = resource;
		this.level = level;
		this.span = span;
		this.msg = msg;
	}
	
	public int getLevel() {
		return level;
	}

	public String getMessage() {
		return msg;
	}

	public ProjectResource getResource() {
		return resource;
	}

	public Span getSpan() {
		return span;
	}

}
