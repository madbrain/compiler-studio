package org.xteam.cs.grm.build;

import java.util.HashSet;
import java.util.Set;

import org.xteam.cs.grm.model.Rule;

/**
 * LALRItem is used to ...
 * 
 * @author ludo
 */
public class LookaheadItem extends LR0Item {

    protected Set<Word> lookahead;
    
    public LookaheadItem(Rule p) {
        super(p);
        this.lookahead = new HashSet<Word>();
    }
    
    protected LR0Item createItem(Rule p) {
        return new LookaheadItem(p);
    }
    
    public Set<Word> lookahead() {
        return lookahead;
    }
    
    public String toString() {
        StringBuffer buffer = new StringBuffer();
        buffer.append(super.toString());
        buffer.append(" {");
        boolean isFirst = true;
        for (Word w : lookahead) {
        	 if (! isFirst)
             	buffer.append(", ");
            buffer.append(w.toString());
           isFirst = false;
        }
        buffer.append('}');
        return buffer.toString();
    }
    
}
