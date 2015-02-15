package org.xteam.cs.grm.model;

public class PropagateAction extends Action {

	private int elementIndex;

	public PropagateAction(int index) {
		this.elementIndex = index;
	}

	public int getIndex() {
		return elementIndex;
	}

	public void setIndex(int elementIndex) {
		this.elementIndex = elementIndex;
	}

}
