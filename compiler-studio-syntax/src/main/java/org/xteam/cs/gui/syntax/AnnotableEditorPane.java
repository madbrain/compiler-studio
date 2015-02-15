package org.xteam.cs.gui.syntax;

import java.awt.Font;
import java.awt.event.MouseEvent;

import javax.swing.JEditorPane;

import org.xteam.cs.runtime.ILexer;

public class AnnotableEditorPane extends JEditorPane {

	private static final long serialVersionUID = 7557723593496868956L;

	protected IResource resource;
	private Reconciler reconciler;
	
	public AnnotableEditorPane(IResource resource,
			StyleManager styleManager) {
		this.resource = resource;
		
		setFont(Font.decode("courier"));
		installEditorKit(resource, styleManager);
		
		setText(resource.getContents());
		
		setToolTipText(".");
		reconciler = createReconciler();
		reconciler.start();
	}
	
	protected Reconciler createReconciler() {
		return new Reconciler(getDocument(), resource, getHighlighter());
	}
	
	private void installEditorKit(IResource resource, StyleManager styleManager) {
		ILexer lexer = resource.getLexer();
		setEditorKitForContentType(resource.getType(),
				new SyntaxEditorKit(lexer, styleManager));
		setContentType(resource.getType());
	}
	
	@Override
	public String getToolTipText(MouseEvent me) {
		int offset = viewToModel(me.getPoint());
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

	public Reconciler getReconciler() {
		return reconciler;
	}
	
}
