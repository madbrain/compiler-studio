package org.xteam.cs.gui.grm;

import javax.swing.JPopupMenu;

import org.xteam.cs.gui.IWorkbench;
import org.xteam.cs.gui.syntax.StyleManager;
import org.xteam.cs.gui.views.Editor;
import org.xteam.cs.gui.views.IResourceAdapter;

public class TestAstParserResourceAdapter implements IResourceAdapter {

	private TestAstParserResource resource;

	public TestAstParserResourceAdapter(TestAstParserResource resource) {
		this.resource = resource;
	}

	@Override
	public void fillMenu(JPopupMenu menu, IWorkbench workbench) {

	}

	@Override
	public Editor createEditor(StyleManager styleManager) {
		return new TestASTParserEditor(resource, styleManager);
	}

}
