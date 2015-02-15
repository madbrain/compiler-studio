package org.xteam.cs.grm.build;

import java.util.Iterator;

public class ActionEmiter {

	private short[] stream;
	private GrammarMapping mapping;
	private int code;
	private int index;

	public ActionEmiter(GrammarMapping mapping) {
		this.mapping = mapping;
		this.stream = new short[10];
		this.index = 0;
	}
	
	public void emitAction(int code, Action action) {
		this.code = code;
		action.visit(new ActionVisitor());
	}
	
	public void emitGoto(int code, Action action) {
		this.code = code;
		action.visit(new GotoVisitor());
	}
	
	public short[] getTable() {
		short[] table = new short[index];
		System.arraycopy(stream, 0, table, 0, index);
		return table;
	}
	
	private void writeValue(int value) {
		if (index == stream.length) {
			short[] newStream = new short[stream.length*2];
			System.arraycopy(stream, 0, newStream, 0, stream.length);
			stream = newStream;
		}
		stream[index++] = (short) value;
	}

	private class ActionVisitor implements IActionVisitor {
		
		@Override
		public void visitAccept(AcceptAction action) {
			//System.out.println("How to encode accept?");
			writeValue(code);
			writeValue(-(mapping.getRules().get(
								action.production()) + 1));
		}

		@Override
		public void visitConflict(ConflictAction action) {
			Iterator<Action> i = action.actions();
			while (i.hasNext()) {
				i.next().visit(this);
			}
		}

		@Override
		public void visitReduce(ReduceAction action) {
			writeValue(code);
			writeValue(-(mapping.getRules().get(
								action.production()) + 1));
		}

		@Override
		public void visitShift(ShiftAction action) {
			writeValue(code);
			writeValue(action.to().number() + 1);
		}
	}
	
	private class GotoVisitor implements IActionVisitor {
		@Override
		public void visitAccept(AcceptAction action) {
			//System.out.println("How to encode accept?");
		}

		@Override
		public void visitConflict(ConflictAction action) {
			throw new RuntimeException("should not have conflicts");
		}

		@Override
		public void visitReduce(ReduceAction action) {
			throw new RuntimeException("should not have reduces");
		}

		@Override
		public void visitShift(ShiftAction action) {
			writeValue(code);
			writeValue(action.to().number());
		}
	}

}
