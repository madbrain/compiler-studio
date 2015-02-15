package org.xteam.cs.gui.views;

import javax.swing.JPopupMenu;

import org.xteam.cs.gui.IWorkbench;
import org.xteam.cs.gui.syntax.StyleManager;

public interface IResourceAdapter {

	void fillMenu(JPopupMenu menu, IWorkbench workbench);

	Editor createEditor(StyleManager styleManager);
	
}
