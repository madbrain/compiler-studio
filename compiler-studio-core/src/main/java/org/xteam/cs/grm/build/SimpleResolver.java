package org.xteam.cs.grm.build;

import java.util.Iterator;

import org.xteam.cs.grm.model.Symbol;

public class SimpleResolver implements IConflictResolver {

	@Override
	public void resolve(LRState state) {
		for (Symbol sym : state.getTransitionSymbols()) {
			Action action = state.getTransition(sym);
			if (action.isConflict()) {
				ConflictAction ca = (ConflictAction) action;
				ShiftAction shift = null;
				Action firstReduce = null;
				Iterator<Action> k = ca.actions();
				while (k.hasNext()) {
					Action a = k.next();
					if(a.isShift())
						shift = (ShiftAction) a;
					else if (firstReduce == null)
						firstReduce = a;
				}
				state.addTransition(sym, shift != null ? shift : firstReduce, true);
			}
		}
	}

}
