package org.xteam.cs.runtime.inc;

import org.xteam.cs.runtime.IToken;

public class Token extends Node implements IToken {
    
    private String contents;
    
    private int state;
    private int loopback;
    private int lookahead;
    
    public Token(int type, String contents) {
        super();
        this.type = type;
        this.contents = contents;
        
        this.loopback = 0;
    }

    public Token(int type) {
        this(type, "");
    }
    
    public int lookahead() {
		return lookahead;
	}
    
    public int lookback() {
        return loopback;
    }
    
    public void setLookahead(int i) {
        lookahead = i;
    }
    
    public void setLookback(int i) {
    	loopback = i;
	}
    
    public int state() {
        return state;
    }
    
    public void setState(int state) {
        this.state = state;
    }
    
    public boolean isToken() {
        return true;
    }
    
	public int end() {
		return start() + length();
	}

	public int start() {
		return 0;
	}
    
    @Override
    public int length() {
        return contents.length();
    }
    
    public int charAt(int i) {
        return contents.charAt(i);
    }
    
	public Object value() {
		return contents;
	}
    
    public String getText(int offset, int length) {
    		int o = offset - getOffset();
    		int l = Math.min(contents.length() - o, length);
    		if (o >= contents.length() || l <= 0)
    			return "";
    		return contents.substring(o, o + l);
    }
    
    public void getText(StringBuffer buffer) {
    	buffer.append(contents);
	}
    
    // overloaded in order to work without parent
    public String getText() {
    	return contents;
    }
    
    public void clear() {
        contents = "";
    }
    
    /*public boolean isLayout(ITokenCategorizer categorizer) {
    	TokenCategory cat = categorizer.getCategory(this);
		return cat == TokenCategory.COMMENT || cat == TokenCategory.LAYOUT;
	}*/
    
    public Token previousToken() {
    		Node brother = parent.previousOf(this);
    		if (brother != null)
    			return brother.lastToken();
    		return null;
    }
    
    public Token firstToken() {
        return this;
    }
    
    public Token lastToken() {
        return this;
    }
    
    public void dettach() {
		parent.remove(this);
	}
    
    /*
     * Inserting text
     */
    
    public void insert(String text, int offset) {
        insert(text, offset, contents.length());
     }
    
    public void insert(String text, int offset, int toRemove) {
    		offset -= getOffset();
        int maxToRemove = Math.min(toRemove, contents.length() - offset);
        if (text.length() > 0 || maxToRemove > 0) {
            StringBuffer buffer = new StringBuffer();
            if (offset > 0)
                buffer.append(contents.substring(0, offset));
            buffer.append(text);
            buffer.append(contents.substring(offset + maxToRemove));
            contents = buffer.toString();
            setFlag(TEXT_CHANGE | LOCAL_CHANGE);
            // we modify the offset of all the followers
            parent.invalidateTextOffsetCacheFollowing(this);
            // and the extent of our ancestors
            parent.invalidateTextExtentCache();
        }
    }

    public String toString() {
        return "[" + contents + "]" + super.toString();
    }

}
