package org.xteam.cs.gui.views;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;

import org.xteam.cs.gui.syntax.AnnotableEditorPane;
import org.xteam.cs.gui.syntax.StyleManager;
import org.xteam.cs.model.ProjectResource;

public class SyntaxEditor extends Editor {

	private static final long serialVersionUID = -5560224208670332661L;
	
	private AnnotableEditorPane editor;
	
	public SyntaxEditor(ProjectResource resource, StyleManager styleManager) {
		super(resource);
		this.editor = new AnnotableEditorPane(new ResourceAdapter(resource), styleManager);
		this.editor.getKeymap().addActionForKeyStroke(
				KeyStroke.getKeyStroke("control S"),
				new SaveAction());
		add(new JScrollPane(editor), BorderLayout.CENTER);
	}
	
	public void close() {
		this.editor.close();
	}
	
	private class SaveAction extends AbstractAction {

		private static final long serialVersionUID = -7654683699025590412L;

		@Override
		public void actionPerformed(ActionEvent e) {
			getResource().save(editor.getText());
		}
		
	}

}
