package org.xteam.cs.grm.build;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ConflictAction extends Action {

	private List<Action> actions;

	public ConflictAction() {
		this.actions = new ArrayList<Action>();
	}

	public ConflictAction(Action oldAction, Action action) {
		this();
		add(action);
		add(oldAction);
	}

	public int type() {
		return CONFLICT;
	}

	public boolean isSame(Action action) {
		// TODO Auto-generated method stub
		return false;
	}

	public void add(Action action) {
		actions.add(action);
	}

	public String toString() {
		StringBuffer buffer = new StringBuffer();
		buffer.append("<p>");
		Iterator<Action> i = actions.iterator();
		while (i.hasNext()) {
			Action action = (Action) i.next();
			buffer.append(action.toString());
			buffer.append("<br>");
		}
		buffer.append("</p>");
		return buffer.toString();
	}

	public void visit(IActionVisitor visitor) {
		visitor.visitConflict(this);
	}

	public Iterator<Action> actions() {
		return actions.iterator();
	}

	@Override
	public int length() {
		int length = 0;
		for (Action action : actions) {
			length += action.length();
		}
		return length;
	}

}
