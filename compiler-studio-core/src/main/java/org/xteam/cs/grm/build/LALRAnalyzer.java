package org.xteam.cs.grm.build;

import java.util.HashSet;
import java.util.Set;

import org.xteam.cs.grm.model.Rule;

/**
 * LALRAnalyzer is used to ...
 * 
 * @author ludo
 */
public class LALRAnalyzer extends AbstractLRAnalyser {
    
    private Word endWord;

    public LALRAnalyzer(GrammarAnalysisModel model) {
        super(model);
        this.endWord = new Word(model.endTerminal());
    }

    public LR0Item createItem(Rule p) {
        Set<Word> lookahead = new HashSet<Word>();
        lookahead.add(endWord);
        return new LALRkItem(p, lookahead, model);
    }

    public LR0Item createItem(Rule production, Set<Word> lookahead) {
        return new LALRkItem(production, lookahead, model);
    }
    
    public Set<Word> getLookahead(LR0Item item) {
        return ((LookaheadItem) item).lookahead();
    }

    /**
     * The links relation of <code>items</code> have been changed.
     * fix them with the correct items.
     */
    public void fixState(LRState newState, Set<LR0Item> items) {
        for (LR0Item i : items) {
            ((LALRkItem)i).fixPropagates(newState);
        }
    }

    public void propagateAllLookaheads(LRAutomaton automaton) {
        for (int i = 0; i < automaton.stateCount(); i++) {
            LRState state = automaton.at(i);
            for (int j = 0; j < state.itemCount(); j++) {
                LALRkItem item = (LALRkItem) state.itemAt(j);
                item.propagateLookaheads(null);
            }
        }
    }

}
