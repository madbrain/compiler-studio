package org.xteam.cs.gui.grm;

import java.util.List;

public abstract class GenericAST {

	public int size() {
		return 0;
	}

	public List<? extends GenericAST> getChildren() {
		return null;
	}

}
