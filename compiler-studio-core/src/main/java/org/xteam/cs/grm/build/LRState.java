package org.xteam.cs.grm.build;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.xteam.cs.grm.model.NonTerminal;
import org.xteam.cs.grm.model.Rule;
import org.xteam.cs.grm.model.Symbol;

public class LRState {

	private int number;
	private List<LR0Item> items;
	private Map<Symbol, Action> transitions;

	public LRState() {
		this.number = -1;
		this.items = new ArrayList<LR0Item>();
		this.transitions = new HashMap<Symbol, Action>();
	}
	
	public int number() { return number; }
	public void setNumber(int n) { number = n; }
	public int itemCount() { return items.size(); }
	
	public LR0Item itemAt(int i) {
		return items.get(i);
	}
	
	 /**
     * @param elem
     * @return
     */
    public Action getTransition(Symbol elem) {
        return transitions.get(elem);
    }
    
    public Set<Symbol> getTransitionSymbols() {
        return transitions.keySet();
    }
    
    public static LRState buildFrom(NonTerminal nt, ILRAnalyser analyzer) {
		LRState state = new LRState();
		for (Rule p : nt.getRules()) {
			state.addItem(analyzer.createItem(p));
		}
		state.makeClosure(analyzer);
		return state;
	}

	public void makeClosure(ILRAnalyser analyzer) {
        List<LR0Item> consider = new ArrayList<LR0Item>(items);
		while(! consider.isEmpty()) {
            LR0Item item = consider.remove(0);
            NonTerminal nt = item.nonTerminalAtPosition();
            if (nt == null)
            	continue;
            Set<Word> lookahead = new HashSet<Word>();
            boolean needProp = item.computeLookahead(lookahead);
            for (Rule prod : nt.getRules()) {
                LR0Item newItem = analyzer.createItem(prod, lookahead);
                LR0Item addItem = addItem(newItem);
                if (needProp)
                    item.addPropagate(addItem);
                if (addItem == newItem)
                    consider.add(newItem);
            }
		}
	}

	public LR0Item addItem(LR0Item item) {
		for (LR0Item t : items) {
			if (t.isSame(item)) {
                t.merge(item);
                return t;
            }
		}
		items.add(item);
		return item;
	}
	
	public void addTransition(Symbol elem, Action action) {
		addTransition(elem, action, false);
	}

	public void addTransition(Symbol elem, Action action, boolean force) {
		if (transitions.containsKey(elem) && ! force) {
			Action oldAction = transitions.get(elem);
			if (!oldAction.equals(action)) {
				if (oldAction.isConflict()) {
					ConflictAction conflict = (ConflictAction) oldAction;
					conflict.add(action);
				} else {
					transitions.put(elem,
							new ConflictAction(oldAction, action));
				}
			}
		} else {
			transitions.put(elem, action);
		}
	}

	public Set<Symbol> getTransitions() {
		Set<Symbol> s = new HashSet<Symbol>();
		for (LR0Item item : items) {
			item.getTransitionElement(s);
		}
		return s;
	}

	public LRState computeTransition(Symbol e,
            ILRAnalyser analyser, Set<LR0Item> linkedItems) {
		LRState n = new LRState();
		for (LR0Item item : items) {
			LR0Item a = item.shift(e);
			if (a != null) {
                linkedItems.add(item);
                n.addItem(a);
            }
		}
		n.makeClosure(analyser);
		return n;
	}

	public boolean isSame(LRState s) {
		if (items.size() != s.items.size()) return false;
		for (int i = 0; i < items.size(); ++i) {
			LR0Item ii = itemAt(i);
			boolean found = false;
			for (int j = 0; j < s.items.size(); ++j) {
				LR0Item ij = s.itemAt(j);
				if (ii.isSame(ij)) {
					found = true;
					break;
				}
			}
			if (! found) return false;
		}
		return true;
	}
    
    public LR0Item findItem(LR0Item newItem) {
        for (LR0Item item : items) {
            if (item.isSame(newItem))
                return item;
        }
        return null;
    }

	public String toString() {
		StringBuffer buffer = new StringBuffer();
		buffer.append(number);
		buffer.append(":\n");
		for (LR0Item item : items) {
			buffer.append(item);
			buffer.append('\n');
		}
		return buffer.toString();
	}

	public void output(PrintStream stream) {
        stream.println("n" + number + " [label=\""
                + toString().replaceAll("\n", "\\\\n") + "\"];");
        for (Symbol elem : transitions.keySet()) {
            Action action = transitions.get(elem);
            if (action.isShift()) {
            	LRState next = ((ShiftAction) action).to();
            	stream.println("n" + number + " -> n" + next.number
            			+ " [label=\"" + elem.getName() + "\"];");
            }
        }
    }
    
}
