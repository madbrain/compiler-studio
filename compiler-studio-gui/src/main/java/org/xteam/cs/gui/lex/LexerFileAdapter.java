package org.xteam.cs.gui.lex;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;

import org.xteam.cs.gui.IWorkbench;
import org.xteam.cs.gui.syntax.StyleManager;
import org.xteam.cs.gui.views.Editor;
import org.xteam.cs.gui.views.IResourceAdapter;
import org.xteam.cs.lex.LexerFile;

public class LexerFileAdapter implements IResourceAdapter {

	private LexerFile resource;

	public LexerFileAdapter(LexerFile resource) {
		this.resource = resource;
	}

	@Override
	public void fillMenu(JPopupMenu menu, final IWorkbench workbench) {
		menu.add(new JSeparator());
		JMenuItem item = new JMenuItem("Open Test Editor");
		item.setEnabled(resource.getBuild() != null);
		item.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				workbench.openEditor(new TestLexerResource(
						resource.getProject(), resource.getBuild()));
			}
		});
		menu.add(item);
	}

	@Override
	public Editor createEditor(StyleManager styleManager) {
		return null;
	}
}
