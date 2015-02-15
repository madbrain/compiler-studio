package org.xteam.cs.grm.build;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.xteam.cs.grm.model.NonTerminal;
import org.xteam.cs.grm.model.Rule;
import org.xteam.cs.grm.model.Symbol;

public class LALRkItem extends LookaheadItem {

    private List<LALRkItem> links;
    private boolean needsPropagation;
	private GrammarAnalysisModel model;
    
    public LALRkItem(Rule production, Set<Word> lookahead, GrammarAnalysisModel model) {
        super(production);
        if (lookahead.contains(new Word()))
        	System.out.println("stop");
        this.lookahead.addAll(lookahead);
        this.links = new ArrayList<LALRkItem>();
        this.needsPropagation = true;
        this.model = model;
    }

    protected LR0Item createItem(Rule p) {
        return new LALRkItem(p, lookahead, model);
    }

    public void addPropagate(LR0Item item) {
        links.add((LALRkItem)item);
        needsPropagation = true;
    }
    
    public LR0Item shift(Symbol e) {
        LR0Item item = super.shift(e);
        if (item == null) return null;
        addPropagate(item);
        return item;
    }
    
    public void merge(LR0Item item) {
        LookaheadItem other = (LookaheadItem) item;
        lookahead.addAll(other.lookahead());
    }

    public boolean computeLookahead(Set<Word> result) {
        if (atEnd())
            throw new RuntimeException("cannot be at end");
        for (int pos = position + 1; pos < rhs.size(); ++pos) {
            Symbol sym = rhs.get(pos);
            if (sym.isTerminal()) {
            	Set<Word> res = model.getFirstSet().at(getFrom(production, pos));
                result.addAll(removeEmpty(res));
                return false;
            } else {
                NonTerminal nt = (NonTerminal) sym;
                result.addAll(removeEmpty(model.getFirstSet().at(nt)));
                if (! model.getNullables().has(nt))
                    return false;
            }
        }
        result.addAll(lookahead);
        return true;
    }

    private Symbol[] getFrom(Rule production, int pos) {
    	Symbol[] results = new Symbol[production.getRhs().size()-pos];
    	for (int i = 0; i < results.length; ++i) {
    		results[i] = production.getRhs().get(pos + i);
    	}
    	return results;
	}

	private Set<Word> removeEmpty(Set<Word> set) {
    	Set<Word> res = new HashSet<Word>();
    	for (Word word : set) {
			if (word.size() != 0) {
				res.add(word);
			}
		}
    	return res;
	}

	public void fixPropagates(LRState newState) {
        for (int i = 0; i < links.size(); ++i) {
            LALRkItem newItem = links.get(i);

            /* find corresponding item in the existing state */
            LALRkItem existing = (LALRkItem) newState.findItem(newItem);

            /* fix up the item so it points to the existing set */
            if (existing != null)
                links.set(i, existing);
        }
    }

    public void propagateLookaheads(Set<Word> incoming) {
        boolean changed = false;

        /* if we don't need to propagate, then bail out now */
        if (!needsPropagation && (incoming == null || incoming.isEmpty()))
            return;

        /* if we have null incoming, treat as an empty set */
        if (incoming != null) {
            changed = lookahead.addAll(incoming);
        }

        /* if we changed or need it anyway, propagate across our links */
        if (changed || needsPropagation) {
            /* don't need to propagate again */
            needsPropagation = false;

            /* propagate our lookahead into each item we are linked to */
            for (int i = 0; i < links.size(); i++) {
                links.get(i).propagateLookaheads(lookahead);
            }
        }
    }
    
}
