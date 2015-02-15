package org.xteam.cs.gui.syntax;

import javax.swing.text.DefaultEditorKit;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.View;
import javax.swing.text.ViewFactory;

import org.xteam.cs.runtime.ILexer;

public class SyntaxEditorKit extends DefaultEditorKit implements ViewFactory {

	private static final long serialVersionUID = -886965529593889274L;
	
	private ILexer lexer;
	private StyleManager styleManager;

	public SyntaxEditorKit(ILexer lexer, StyleManager styleManager) {
		this.lexer = lexer;
		this.styleManager = styleManager;
	}

	@Override
	public ViewFactory getViewFactory() {
		return this;
	}

	@Override
	public View create(Element element) {
		return new SyntaxView(element, styleManager);
	}
	
	@Override
    public Document createDefaultDocument() {
        return new SyntaxDocument(lexer);
    }
}
