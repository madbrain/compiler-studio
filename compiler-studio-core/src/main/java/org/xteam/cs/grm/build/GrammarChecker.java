package org.xteam.cs.grm.build;

import java.util.HashSet;
import java.util.Set;

import org.xteam.cs.grm.model.Grammar;
import org.xteam.cs.grm.model.NonTerminal;
import org.xteam.cs.grm.model.Rule;
import org.xteam.cs.grm.model.Symbol;

public class GrammarChecker {

	private boolean hasError;
	private Set<NonTerminal> ntReported = new HashSet<NonTerminal>();

	public GrammarChecker() {
		this.hasError = false;
	}

	public boolean check(Grammar grammar) {
		checkNonTerminal(grammar.getStart());
		for (Rule prod : grammar.getRules()) {
			checkProduction(prod);
		}
		return ! hasError;
	}
	
	public void checkProduction(Rule prod) {
		for (Symbol sym : prod.getRhs()) {
			if (! sym.isTerminal()) {
				checkNonTerminal((NonTerminal) sym);
			}
		}
	}
	
	public void checkNonTerminal(NonTerminal nt) {
		if (nt == null)
			hasError = true;
		else if (nt.getRules().isEmpty() && ! ntReported.contains(nt)) {
			ntReported.add(nt);
			hasError = true;
		}
	}

}
