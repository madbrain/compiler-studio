package org.xteam.cs.runtime;

import java.util.ArrayList;
import java.util.List;

public abstract class BaseParser {

	protected short[][] actionTab;
	protected short[][] reduceTab;
	protected short[][] productionTab;
	
	public BaseParser(IParseTables tables) {
		productionTab = tables.productionTable();
        actionTab = tables.actionTable();
        reduceTab = tables.reduceTable();
	}
	
	protected static final boolean isShift(int act) {
		return act > 0;
	}
	
	protected static final boolean isReduce(int act) {
		return act < 0;
	}
	
	protected static final short toShift(int act) {
		return (short) (act - 1);
	}
	
	protected static final short toReduce(int act) {
		return (short)(- act - 1);
	}
	
	/**
	 * In case of conflicts, many actions can appear
	 * for the same symbol. Collect all the actions.
	 * 
	 * @param state
	 * @param sym
	 * @return
	 */
	protected final short[] getActions(int state, int sym) {
		List<Short> actions = new ArrayList<Short>();
		if (state >= actionTab.length)
			System.out.println("BaseParser.getActions()");
		short[] row = actionTab[state];

		boolean found = false;
		for (int probe = 0; probe < row.length; probe++) {
			/* is this entry labeled with our Symbol or the default? */
			short tag = row[probe++];
			if (tag == sym || tag == -1) {
				actions.add(row[probe]);
				found = true;
			} else if (found)
				break;
		}
		if (! found)
			return new short[] { 0 };
		short[] acts = new short[actions.size()];
		for (int i = 0; i < actions.size(); ++i) {
			acts[i] = actions.get(i);
		}
		return acts;
	}

	protected final short getAction(int state, int sym) {
		if (state >= actionTab.length)
			System.out.println("BaseParser.getAction()");
        short[] row = actionTab[state];

        // linear search if we are < 10 entries
        //if (row.length < 20) {
            for (int probe = 0; probe < row.length; probe++) {
                // is this entry labeled with our Symbol or the default?
                short tag = row[probe++];
                if (tag == sym || tag == -1) {
                    // return the next entry
                    return row[probe];
                }
            }
			/* shouldn't happened, but if we run off the end we return the 
			   default (error == 0) */
			return 0;
        //}
        // otherwise binary search XXX table is not ordered
		/*int first = 0;
		// leave out trailing default entry
		int last = (row.length - 1) / 2 - 1;
		
		while (first <= last) {
			int probe = (first + last) / 2;
			if (sym == row[probe * 2])
				return row[probe * 2 + 1];
			else if (sym > row[probe * 2])
				first = probe + 1;
			else
				last = probe - 1;
		}
		// not found, use the default at the end
		return row[row.length - 1];*/
	}
    
    protected final short getReduce(int state, int sym) {
        short tag;
        short[] row = reduceTab[state];

        /* if we have a null row we go with the default */
        if (row == null)
            return -1;

        for (int probe = 0; probe < row.length; probe++) {
            /* is this entry labeled with our Symbol or the default? */
            tag = row[probe++];
            if (tag == sym || tag == -1) {
                /* return the next entry */
                return row[probe];
            }
        }
        /* if we run off the end we return the default (error == -1) */
        return -1;
    }
}
