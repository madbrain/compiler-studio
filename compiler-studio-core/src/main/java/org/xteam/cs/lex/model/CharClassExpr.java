package org.xteam.cs.lex.model;

import java.util.ArrayList;
import java.util.List;

public class CharClassExpr extends Expr {

	private List<IntCharSet.Interval> intervals = new ArrayList<IntCharSet.Interval>();
	
	public List<IntCharSet.Interval> getIntervals() {
		return intervals;
	}

	public void add(IntCharSet.Interval interval) {
		this.intervals.add(interval);
	}

	@Override
	public void visit(IExprVisitor visitor) {
		visitor.visitCharclass(this);
	}

}
