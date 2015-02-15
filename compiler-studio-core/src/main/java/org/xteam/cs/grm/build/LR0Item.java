package org.xteam.cs.grm.build;

import java.util.List;
import java.util.Set;

import org.xteam.cs.grm.model.NonTerminal;
import org.xteam.cs.grm.model.Rule;
import org.xteam.cs.grm.model.Symbol;

public class LR0Item {

	protected Rule production;
	protected int position;
	protected List<Symbol> rhs;

	public LR0Item(Rule p) {
		production = p;
		rhs = production.getRhs();
		position = 0;
	}
	
	public Rule production() {
		return production;
	}

	public boolean atEnd() {
		return position == rhs.size();
	}
	
	public Symbol atPosition() {
	    if (atEnd()) return null;
	    return rhs.get(position);
	}
    
    public NonTerminal nonTerminalAtPosition() {
        if (atEnd()) return null;
        Symbol sym = atPosition();
        if (sym.isTerminal())
        	return null;
        return (NonTerminal) sym;
    }

	public void getTransitionElement(Set<Symbol> v) {
		if (atEnd()) return;
		v.add(rhs.get(position));
	}

	public LR0Item shift(Symbol e) {
		if (rhs.size() <= position
				|| ! rhs.get(position).equals(e))
            return null;
		LR0Item item = createItem(production);
		item.position = position + 1;
		return item;
	}

	public boolean isSame(LR0Item i) {
		return i.production == production
                && i.position == position;
	}
	
	protected LR0Item createItem(Rule p) {
	    return new LR0Item(p);
	}
    
	/**
	 * 
	 * @param lookahead
	 * @return true if this item is nullable
	 */
    public boolean computeLookahead(Set<Word> lookahead) {
        // Don't need any lookhead computations
        return false;
    }
    
    public void merge(LR0Item item) {
        // only used for LALR lookahead merging
    }
    
    public void addPropagate(LR0Item link) {
    	// only used for LALR
    }

	public String toString() {
		StringBuffer buffer = new StringBuffer();
		buffer.append(production.getLhs().getName());
		buffer.append(" -> ");
		for (int i = 0; i < rhs.size(); ++i) {
			if (i != 0) buffer.append(' ');
			if (i == position) buffer.append(". ");
			buffer.append(rhs.get(i).getName());
		}
		if (rhs.size() == position)
			buffer.append(" .");
		return buffer.toString();
	}
    
}
