
package org.xteam.cs.grm.build;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.xteam.cs.grm.model.NonTerminal;
import org.xteam.cs.grm.model.Rule;
import org.xteam.cs.grm.model.Symbol;

/**
 * FollowSet represent the FOLLOW set of a grammar
 * 
 * @author llhours
 *
 */
public class FollowSet {

	private GrammarAnalysisModel model;
	private Map<NonTerminal, Set<Word>> follows;
	
	public FollowSet(GrammarAnalysisModel model) {
		follows = new HashMap<NonTerminal, Set<Word>>();
		this.model = model;
		build(model.getFirstSet(), model.getNullables());
	}
	
	/**
     * @return the grammar associated with this follow set.
     */
    public GrammarAnalysisModel grammar() {
        return model;
    }
	
	public Set<Word> at(NonTerminal sym) {
		return follows.get(sym);
	}

	private void build(FirstSet firsts, Nullables nullables) {
		
		initializeSets();
		
		boolean changed = true;
		while (changed) {
			changed = false;
			for (Rule production : model.grammar().getRules()) {
				NonTerminal lhs = production.getLhs();
				List<Symbol> rhs = production.getRhs();
				for (int i = 0; i < rhs.size(); ++i) {
					Symbol e = rhs.get(i);
					if (! e.isTerminal()) {
						Set<Word> set = at((NonTerminal) e);
						if (i != (rhs.size() - 1)) {
							Symbol[] syms = makeSubArray(production, i+1);
							changed |= addToSet(set, firsts.at(syms));
							if (reduceToEpsilon(nullables, syms))
								changed |= addToSet(set, at(lhs));
						} else
							changed |= addToSet(set, at(lhs));
					}
				}
			}
		}
	}
	
	private boolean reduceToEpsilon(Nullables nullables, Symbol[] syms) {
		for (int i = 0; i < syms.length; ++i) {
			if (syms[i].isTerminal()
					|| !nullables.has((NonTerminal)syms[i]))
				return false;
		}
		return true;
	}

	private boolean addToSet(Set<Word> set, Set<Word> newElements) {
		boolean changed = false;
		for (Word word : newElements) {
			if (word.size() > 0 && ! set.contains(word)) {
				changed = true;
				set.add(word);
			}
		}
		return changed;
	}

	private Symbol[] makeSubArray(Rule production, int offset) {
        List<Symbol> res = new ArrayList<Symbol>();
        List<Symbol> symbols = production.getRhs();
		for (int i = offset; i < symbols.size(); ++i) {
			res.add(symbols.get(i));
        }
		return res.toArray(new Symbol[res.size()]);
	}
	
	private void initializeSets() {
		for (Symbol symbol : model.grammar().getSymbols()) {
			if (! symbol.isTerminal())
				follows.put((NonTerminal)symbol, new HashSet<Word>());
		}
	}

	public String toString() {
        StringBuffer buffer = new StringBuffer();
		for (Symbol e : follows.keySet()) {
			if (! e.isTerminal()) {
                buffer.append(e.getName());
                buffer.append(" -> { ");
				for (Word word : at((NonTerminal) e)) {
                    buffer.append(word.toString());
                    buffer.append(" ");
				}
                buffer.append(" }\n");
			}
		}
        return buffer.toString();
	}
    
}
