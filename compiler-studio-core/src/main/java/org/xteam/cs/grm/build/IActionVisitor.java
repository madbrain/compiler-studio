package org.xteam.cs.grm.build;

public interface IActionVisitor {

	void visitAccept(AcceptAction action);

	void visitConflict(ConflictAction action);

	void visitReduce(ReduceAction action);

	void visitShift(ShiftAction action);

}
