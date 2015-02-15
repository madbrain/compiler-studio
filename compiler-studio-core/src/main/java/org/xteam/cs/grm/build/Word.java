
package org.xteam.cs.grm.build;

import java.util.ArrayList;
import java.util.List;

import org.xteam.cs.grm.model.Terminal;

/**
 * Word represent a word over the alphabet of the
 * terminals of the grammar.
 * 
 * @author llhours
 *
 */
public class Word {

	private List<Terminal> values;
	
	public Word() {
		values = new ArrayList<Terminal>();
	}
	
    /**
     * @param terminal
     */
	public Word(Terminal terminal) {
		this();
		if (terminal == null)
			throw new RuntimeException("toto");
		values.add(terminal);
	}
	
	public int size() { return values.size(); }
    
	public Terminal at(int i) {
		return (Terminal) values.get(i);
	}

	public Word concat(int n, Word t) {
		Word w = new Word();
		w.values = new ArrayList<Terminal>(values);
		int bound = n > t.size() ? t.size() : n;
		for (int i = 0; i < bound; ++i)
			w.values.add(t.at(i));
		return w;
	}
	
	public boolean equals(Object obj) {
		if (! (obj instanceof Word)) return false;
		Word other = (Word) obj;
		if (size() != other.size()) return false;
		for (int i = 0; i < size(); ++i)
			if (! other.at(i).equals(at(i)))
				return false;
		return true;
	}
	
	public int hashCode() {
		int res = 0;
		for (int i = 0; i < size(); ++i)
			res = (res << 8) ^ at(i).hashCode();
		return res;
	}
	
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		if (values.size() == 1) {
            buffer.append(values.get(0).getName());
        } else {
            buffer.append('(');
            for (int i = 0; i < values.size(); ++i) {
                if (i != 0)
                    buffer.append(' ');
                buffer.append(values.get(i).getName());
            }
            buffer.append(')');
        }
		return buffer.toString();
	}	
}