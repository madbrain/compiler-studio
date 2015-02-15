package org.xteam.cs.runtime.inc;


public class Location {

    Token token;
    int offset;
    
    public Location(Token token) {
        this.token = token;
        this.offset = 0;
    }

    public void advance(int length) {
    	while (length > 0) {
    		if (atEnd())
    			advanceToken();
    		int l = token.length() - offset;
    		if (length < l)
    			l = length; 
    		offset += l;
    		length -= l;
    	}
    }
    
    public boolean atEnd() {
        return offset >= token.length();
    }

    public int deltaInChars(Location other) {
        int delta = 0;
        Token oToken = other.token;
        int off = other.offset;
        while (oToken != token) {
            delta += oToken.length() - off;
            oToken = oToken.nextToken();
            off = 0;
        }
        delta += offset - off;
        offset = off;
        return delta;
    }

    public void advanceToken() {
        token = token.nextToken();
        offset = 0;
    }

    public int nextChar() {
        return token.charAt(offset++);
    }
    
    public String toString() {
        StringBuffer buffer = new StringBuffer();
        buffer.append('[');
        String str = token.getText();
        buffer.append(str.substring(0, offset));
        buffer.append('|');
        buffer.append(str.substring(offset));
        buffer.append(']');
        return buffer.toString();
    }
}
