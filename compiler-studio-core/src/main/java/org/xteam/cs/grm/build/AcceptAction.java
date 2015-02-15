package org.xteam.cs.grm.build;

import org.xteam.cs.grm.model.Rule;



/**
 * AcceptAction is used to ...
 * 
 * @author ludo
 */
public class AcceptAction extends Action {

    private Rule production;

	public AcceptAction(Rule production) {
    	this.production = production;
    }
	
	public Rule production() {
		return production;
	}

	public int type() {
        return ACCEPT;
    }

    public boolean equals(Object o) {
    	if (! (o instanceof AcceptAction))
    		return false;
        return ((AcceptAction)o).production.equals(production);
    }
    
    public String toString() {
        return "acc";
    }

	public void visit(IActionVisitor visitor) {
		visitor.visitAccept(this);
	}

}
