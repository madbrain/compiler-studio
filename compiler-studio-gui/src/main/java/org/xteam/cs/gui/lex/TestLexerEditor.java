package org.xteam.cs.gui.lex;

import java.awt.BorderLayout;

import javax.swing.JScrollPane;

import org.xteam.cs.gui.views.Editor;

public class TestLexerEditor extends Editor {
	
	private static final long serialVersionUID = 2070675427943275867L;
	private LexerEditorPane editor;
	
	public TestLexerEditor(TestLexerResource resource) {
		super(resource);
		this.editor = new LexerEditorPane(resource);
		add(new JScrollPane(editor), BorderLayout.CENTER);
	}

	public void close() {
		this.editor.close();
	}

}
