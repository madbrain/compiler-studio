package org.xteam.cs.grm.build;

import java.util.HashSet;
import java.util.Set;

import org.xteam.cs.grm.model.Rule;

/**
 * LALRAnalyzer is used to ...
 * 
 * @author ludo
 */
public class LRAnalyzer extends AbstractLRAnalyser {
    
    private Word endWord;

    public LRAnalyzer(GrammarAnalysisModel model) {
        super(model);
        this.endWord = new Word(model.endTerminal());
    }

    public LR0Item createItem(Rule p) {
        Set<Word> lookahead = new HashSet<Word>();
        lookahead.add(endWord);
        return new LRkItem(p, lookahead, model);
    }

    public LR0Item createItem(Rule production, Set<Word> lookahead) {
        return new LRkItem(production, lookahead, model);
    }
    
    public Set<Word> getLookahead(LR0Item item) {
        return ((LookaheadItem) item).lookahead();
    }

}
