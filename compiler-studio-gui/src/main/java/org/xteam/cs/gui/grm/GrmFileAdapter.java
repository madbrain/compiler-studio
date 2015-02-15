package org.xteam.cs.gui.grm;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;

import org.xteam.cs.grm.GrammarFile;
import org.xteam.cs.grm.build.ParserBuild;
import org.xteam.cs.gui.IWorkbench;
import org.xteam.cs.gui.syntax.StyleManager;
import org.xteam.cs.gui.views.Editor;
import org.xteam.cs.gui.views.IResourceAdapter;

public class GrmFileAdapter implements IResourceAdapter {

	private GrammarFile resource;

	public GrmFileAdapter(GrammarFile resource) {
		this.resource = resource;
	}

	@Override
	public void fillMenu(JPopupMenu menu, final IWorkbench workbench) {
		menu.add(new JSeparator());
		JMenuItem item = new JMenuItem("Open Test Concrete Parser Editor");
		final ParserBuild build = resource.getParserModel();
		item.setEnabled(build != null && build.isValidForConcrete());
		item.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				workbench.openEditor(new TestConcreteParserResource(resource.getProject(), build));
			}
		});
		menu.add(item);
		
		item = new JMenuItem("Open Test AST Parser Editor");
		item.setEnabled(build != null && build.isValidForAST());
		item.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				workbench.openEditor(new TestAstParserResource(resource.getProject(), build));
			}
		});
		menu.add(item);
	}

	@Override
	public Editor createEditor(StyleManager styleManager) {
		return null;
	}

}
