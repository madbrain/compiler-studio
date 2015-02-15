
package org.xteam.cs.grm.build;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.xteam.cs.grm.model.Grammar;
import org.xteam.cs.grm.model.NonTerminal;
import org.xteam.cs.grm.model.Rule;
import org.xteam.cs.grm.model.Symbol;
import org.xteam.cs.grm.model.Terminal;

/**
 * FirstSet represent the FIRST sets of a grammar.
 * 
 * @author llhours
 *
 */
public class FirstSet {

	private Map<Symbol, Set<Word>> firsts;
	private Grammar grammar;
	private int k;
	
	public FirstSet(Grammar grammar, int k) {
		this.grammar = grammar;
		this.firsts = new HashMap<Symbol, Set<Word>>();
		this.k = k;
		build();
	}
	
	public FirstSet(Grammar grammar) {
		this(grammar, 1);
	}
	
	public Grammar grammar() {
		return grammar;
	}
	
	public Set<Word> at(Symbol e) {
		return firsts.get(e);
	}
	
	public Set<Word> at(Symbol[] elements) {
		Set<Word> set = null;
		for (int i = 0; i < elements.length; ++i) {
			if (i == 0)
				set = new HashSet<Word>(at(elements[i]));
			else
				set = cartesianProduct(set, at(elements[i]));
		}
		return set;
	}
	
	private void build() {
		
		initializeSets();
		
		boolean changed = true;
		while (changed) {
			changed = false;
			for (Rule production : grammar.getRules()) {
				NonTerminal nt = production.getLhs();
				Set<Word> set = at(nt);
			
				if (production.getRhs().isEmpty()) {
					changed |= addToSet(set, new Word());
				} else {
					Set<Word> total = null;
					boolean isAborted = false;
					for (Symbol sym : production.getRhs()) {
						Set<Word> s = at(sym);
						if (s.isEmpty()) {
							changed = true;
							isAborted = true;
							break;
						}
						if (total == null)
							total = s;
						else
							total = cartesianProduct(total, s);
						boolean allOk = true;
						for (Word t : total) {
							if (t.size() < k) {
								allOk = false;
								break;
							}
						}
						if (allOk)
							break;
					}
					if (! isAborted) {
						for (Word w : total) {
							changed |= addToSet(set, w);
						}
					}
				}
			}
		}
	}
	
	private boolean addToSet(Set<Word> set, Word word) {
		if (! set.contains(word)) {
			set.add(word);
			return true;
		}
		return false;
	}
	
	private Set<Word> cartesianProduct(Set<Word> a, Set<Word> b) {
		Set<Word> res = new HashSet<Word>();
		for (Word wa : a) {
			if (wa.size() == k) {
				res.add(wa);
				continue;
			}
			for (Word wb : b) {
				res.add(wa.concat(k-wa.size(), wb));
			}
		}
		return res;
	}

	private void initializeSets() {
		for (Symbol symbol : grammar.getSymbols()) {
			Set<Word> set = new HashSet<Word>();
			if (symbol.isTerminal())
				set.add(new Word((Terminal) symbol));
			firsts.put(symbol, set);
		}
	}
	
	public String toString() {
        StringBuffer buffer = new StringBuffer();
		for (Symbol e : firsts.keySet()) {
            buffer.append(e.getName());
            buffer.append(" -> { ");
			for (Word word : at(e)) {
				buffer.append(word);
                buffer.append(" ");
			}
			buffer.append(" }\n");
		}
        return buffer.toString();
	}
	
}
