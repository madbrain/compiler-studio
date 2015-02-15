package org.xteam.cs.grm.model;

public class AstListAction extends Action {

	private int listIndex;
	private int elementIndex;

	public void setIndexes(int listIndex, int elementIndex) {
		this.listIndex = listIndex;
		this.elementIndex = elementIndex;
	}

	public int getListIndex() {
		return listIndex;
	}

	public int getElementIndex() {
		return elementIndex;
	}

}
