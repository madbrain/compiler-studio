package org.xteam.cs.gui.lex;

import java.awt.Font;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JEditorPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Highlighter;

import org.xteam.cs.gui.syntax.Annotation;
import org.xteam.cs.gui.syntax.IResource;
import org.xteam.cs.gui.syntax.Reconciler;
import org.xteam.cs.gui.views.ResourceAdapter;
import org.xteam.cs.runtime.IToken;

public class LexerEditorPane extends JEditorPane {

	private static final long serialVersionUID = 7557723593496868956L;

	private TestLexerResource lexerResource;
	private ResourceAdapter resource;
	private Reconciler reconciler;
	private BoxHighlightPainter painter = new BoxHighlightPainter();
	private List<Annotation> annotations = new ArrayList<Annotation>();
	
	public LexerEditorPane(TestLexerResource resource) {
		this.lexerResource = resource;
		this.resource = new ResourceAdapter(resource);
		
		setFont(Font.decode("courier"));
		setToolTipText(".");
		
		reconciler = new LexerReconciler(getDocument(), this.resource, getHighlighter());
		reconciler.start();
	}
	
	@Override
	public String getToolTipText(MouseEvent me) {
		int offset = viewToModel(me.getPoint());
		for (Annotation annotation : annotations) {
			if (annotation.contains(offset)) {
				return annotation.getText();
			}
		}
		for (Annotation annotation : reconciler.getAnnotations()) {
			if (annotation.contains(offset)) {
				return annotation.getText();
			}
		}
		return null;
	}

	public void close() {
		reconciler.stop();
	}
	
	private class LexerReconciler extends Reconciler {

		public LexerReconciler(Document document, IResource resource,
				Highlighter highlighter) {
			super(document, resource, highlighter);
		}
		
		@Override
		protected void doReconcile() {
			super.doReconcile();
			for (Annotation annotation : annotations) {
				getHighlighter().removeHighlight(annotation.getTag());
			}
			annotations.clear();
			for (IToken token : lexerResource.getTokens()) {
				try {
					annotations.add(new Annotation(resource, 0,
							token.start(), token.start() + token.length(), (String)token.value(),
							getHighlighter().addHighlight(
									token.start(), token.start() + token.length(), painter)));
				} catch (BadLocationException e) {
				}
			}
		}
		
	}
	
}
