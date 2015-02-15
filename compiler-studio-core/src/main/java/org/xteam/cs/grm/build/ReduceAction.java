package org.xteam.cs.grm.build;

import org.xteam.cs.grm.model.Rule;


/**
 * ReduceAction is used to ...
 * 
 * @author ludo
 */
public class ReduceAction extends Action {

    private Rule production;
    
    /**
     * @param prod
     * @param gotoState
     */
    public ReduceAction(Rule prod) {
        this.production = prod;
    }
    
    public int type() {
        return REDUCE;
    }
    
    public Rule production() {
        return production;
    }

    public boolean equals(Object o) {
    	if (! (o instanceof ReduceAction))
    		return false;
        return ((ReduceAction)o).production.equals(production);
    }
    
    public String toString() {
        return "[" + production + "]";
    }

	public void visit(IActionVisitor visitor) {
		visitor.visitReduce(this);
	}

	/*public int code() {
		return makeCode(REDUCE, production.number());
	}*/
    
}
