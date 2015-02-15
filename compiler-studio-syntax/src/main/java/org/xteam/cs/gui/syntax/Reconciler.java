package org.xteam.cs.gui.syntax;

import java.util.ArrayList;
import java.util.List;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Highlighter;
import javax.swing.text.Highlighter.HighlightPainter;

import org.xteam.cs.runtime.IErrorReporter;
import org.xteam.cs.runtime.Span;

public class Reconciler implements Runnable, DocumentListener {
	
	public static final int WAIT_TIME = 500;

	private Thread thread;
	private boolean running = true;
	private boolean doReconciling = false;
	private boolean interupted;
	private Document document;
	private IResource resource;

	private List<Annotation> annotations = new ArrayList<Annotation>();
	private Highlighter highlighter;
	private HighlightPainter highlightPainter = new ErrorHighlightPainter();
	
	public Reconciler(Document document, IResource resource, Highlighter highlighter) {
		this.document = document;
		this.resource = resource;
		this.highlighter = highlighter;
		document.addDocumentListener(this);
	}

	public void start() {
		thread = new Thread(this, "Reconciler");
		thread.start();
	}
	
	public synchronized void stop() {
		document.removeDocumentListener(this);
		running = false;
		notify();
	}

	@Override
	public synchronized void run() {
		doReconcile();
		while(running) {
			try {
				wait();
				while (doReconciling) {
					interupted = false;
					wait(WAIT_TIME);
					if (doReconciling && !interupted) {
						doReconcile();
						doReconciling = false;
					}
				}
			} catch (InterruptedException e) {
			}
		}
	}
	
	protected void doReconcile() {
		for (Annotation annotation : annotations) {
			highlighter.removeHighlight(annotation.getTag());
		}
		annotations.clear();
		try {
			ErrorReporter reporter = new ErrorReporter();
			//System.out.println("==========================");
			resource.analyse(document.getText(0, document.getLength()), reporter);
		} catch (BadLocationException e) {
		}
	}

	@Override
	public void changedUpdate(DocumentEvent e) {
		markReconcilingWanted();
	}

	@Override
	public void insertUpdate(DocumentEvent e) {
		markReconcilingWanted();
	}

	@Override
	public void removeUpdate(DocumentEvent e) {
		markReconcilingWanted();
	}
	
	public synchronized void markReconcilingWanted() {
		resource.markDirty();
		doReconciling = true;
		interupted = true;
		notify();
	}
	
	private class ErrorReporter implements IErrorReporter {
		
		@Override
		public void reportError(int level, Span span, String msg) {
			//System.out.println(span + ": " + msg);
			try {
				int line = document.getDefaultRootElement().getElementIndex(span.start()) + 1;
				annotations.add(new Annotation(resource, line, span.start(), span.end(), msg,
						highlighter.addHighlight(span.start(), span.end(),
								highlightPainter)));
			} catch (BadLocationException e) {
			}
		}

		@Override
		public boolean hasErrors() {
			return annotations.size() > 0;
		}
	}

	public List<Annotation> getAnnotations() {
		return annotations;
	}
	
}