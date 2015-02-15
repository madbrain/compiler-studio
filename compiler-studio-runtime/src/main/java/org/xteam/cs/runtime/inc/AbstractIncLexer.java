package org.xteam.cs.runtime.inc;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.xteam.cs.runtime.ILexerInput;
import org.xteam.cs.runtime.IStatedLexer;
import org.xteam.cs.runtime.IToken;

public class AbstractIncLexer {

    private IStatedLexer lexer;
    private UltraRoot root;
    private Location readLocation;
    private Location constructionLocation;
    private List<IToken> tokenList;
    private ILexerInput input;
	private Token lastToken;

    public AbstractIncLexer(IStatedLexer lexer) {
        this.lexer = lexer;
        this.input = new ILexerInput() {
            public int next() {
                while (readLocation.atEnd()
                        && readLocation.token != root.eos()) {
                    readLocation.advanceToken();
                }
                if (readLocation.token == root.eos())
                	return -1;
                return readLocation.nextChar();
            }
            
            public int position() {
            	return 0; // XXX
            }

			public void putBack(String contents) {
				
			}
        };
    }
    
    public void intialize(UltraRoot root) {
    	this.root = root;
    }

    private void lexPhase() throws IOException {
        List<Token> newTokens = new ArrayList<Token>();
        Token tok = findNextRegion(root);
        Token insertPoint = tok.previousToken();
        while (tok != root.eos()) {
            tok = firstNewToken(tok);
            while (! canStopLexing()) {
                newTokens.add(tok);
                tok = nextNewToken();
            }
            newTokens.add(tok);
            //attachNewTokens(insertPoint, newTokens);
            newTokens.clear();
            tok = findNextRegion(tok);
            insertPoint = tok.previousToken();
        }
    }

    private Token findNextRegion(Node node) {
        if (node == root.eos()
                || (node.isToken() && node.isSet(Node.MARK)))
            return (Token) node;
        if (node.isSet(Node.NESTED_CHANGE))
            return findNextRegion(((ParentNode) node).childAt(0));
        return findNextRegion(node.nextSubtree());
    }
    
    public Token firstNewToken(Token tok) {
    	throw new RuntimeException("to be corected");
    	/*lexer.setInput(input); // reset the underlying lexer
        readLocation = new Location(tok);
        constructionLocation = new Location(tok);
        if (tok == root.bos())
            lexer.setState(lexer.initialState());
        else
            lexer.setState(tok.previousToken().state());
        tokenList = new ArrayList<IToken>();
        return nextNewToken();*/
    }
    
    public Token nextNewToken() throws IOException {
        if (tokenList.isEmpty()) {
            tokenList.add(lexer.nextToken());
        }
        for (int i = 0; i < tokenList.size(); ++i) {
            Token tok = (Token) tokenList.get(i);
            if (i == (tokenList.size() - 1))
                tok.setState(lexer.getState());
            else
                tok.setState(-1);
            constructionLocation.advance(tok.length());
            tok.setLookahead(readLocation.deltaInChars(constructionLocation));
        }
        lastToken = (Token) tokenList.remove(0);
        lastToken.setFlag(Token.RELEXED);
        return lastToken;
    }
    
    public boolean canStopLexing() {
        if (! tokenList.isEmpty()) return false;
        if (constructionLocation.offset != 0) return false;
        if (constructionLocation.token.isSet(Node.MARK)) return false;
        if (! isStartable(lastToken)) return false;
        return lastToken.state() == constructionLocation.token.previousToken().state();
    }
    
    private boolean isStartable(Token tok) {
        return tok.state() >= 0;
    }

}
