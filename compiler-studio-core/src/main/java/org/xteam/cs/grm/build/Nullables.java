package org.xteam.cs.grm.build;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.xteam.cs.grm.model.Grammar;
import org.xteam.cs.grm.model.NonTerminal;
import org.xteam.cs.grm.model.Rule;
import org.xteam.cs.grm.model.Symbol;

public class Nullables {

    private Grammar grammar;
    private Set<NonTerminal> nullables;

    public Nullables(Grammar grammar) {
        this.grammar = grammar;
        this.nullables = new HashSet<NonTerminal>();
        build();
    }
    
    public boolean has(NonTerminal nt) {
        return nullables.contains(nt);
    }
    
    public Set<NonTerminal> getNullables() {
    	return nullables;
    }

    private void build() {
        boolean changed = true;
        while (changed) {
            changed = false;
            for (Rule production : grammar.getRules()) {
                NonTerminal nt = production.getLhs();
            
                boolean isNullable = true;
                List<Symbol> rhs = production.getRhs();
                if (rhs.isEmpty()) {
                    isNullable = true;
                } else {
                    for (Symbol sym : rhs) {
                        if (sym.isTerminal() ||
                                ! has((NonTerminal) sym)) {
                            isNullable = false;
                            break;
                        }
                    }
                }
                if (isNullable && ! has(nt)) {
                    nullables.add(nt);
                    changed = true;
                }
            }
        }
    }
    
    public String toString() {
        StringBuffer buffer = new StringBuffer();
        buffer.append("{ ");
        for (NonTerminal nt : nullables) {
            buffer.append(nt);
            buffer.append(' ');
        }
        buffer.append('}');
        return buffer.toString();
    }
}
