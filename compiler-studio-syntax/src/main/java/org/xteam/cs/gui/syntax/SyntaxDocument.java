package org.xteam.cs.gui.syntax;

import java.io.CharArrayReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.event.DocumentEvent;
import javax.swing.event.UndoableEditEvent;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;
import javax.swing.text.Segment;

import org.xteam.cs.runtime.ILexer;
import org.xteam.cs.runtime.IToken;

public class SyntaxDocument extends PlainDocument {

	private static final long serialVersionUID = -9180594402389582673L;
	
	private ILexer lexer;
	private List<IToken> tokens;
	
	public SyntaxDocument(ILexer lexer) {
		this.lexer = lexer;
		putProperty(PlainDocument.tabSizeAttribute, 4);
	}
	
	private void parse() {
        if (lexer == null) {
            tokens = null;
            return;
        }
        List<IToken> toks = new ArrayList<IToken>(getLength() / 10);
        try {
            Segment seg = new Segment();
            getText(0, getLength(), seg);
            CharArrayReader reader = new CharArrayReader(seg.array, seg.offset, seg.count);
            lexer.setInput(reader);
            IToken token;
            while ((token = lexer.nextToken()) != null) {
                toks.add(token);
            }
        } catch (BadLocationException ex) {
        } catch (IOException ex) {
        } finally {
            tokens = toks;
        }
    }
	
	@Override
    protected void fireChangedUpdate(DocumentEvent e) {
        parse();
        super.fireChangedUpdate(e);
    }

    @Override
    protected void fireInsertUpdate(DocumentEvent e) {
        parse();
        super.fireInsertUpdate(e);
    }

    @Override
    protected void fireRemoveUpdate(DocumentEvent e) {
        parse();
        super.fireRemoveUpdate(e);
    }

    @Override
    protected void fireUndoableEditUpdate(UndoableEditEvent e) {
        parse();
        super.fireUndoableEditUpdate(e);
    }
	
	class TokenIterator implements Iterator<IToken> {

        int start;
        int end;
        int ndx = 0;

        private TokenIterator(int start, int end) {
            this.start = start;
            this.end = end;
            if (tokens != null && !tokens.isEmpty()) {
            	
            	for (ndx = 0; ndx < tokens.size(); ++ndx) {
            		IToken t = tokens.get(ndx);
            		if (t.start() <= start && start < (t.start() + t.length())
            				|| start <= t.start())
            			break;
            	}
            }
        }

        @Override
        public boolean hasNext() {
            if (tokens == null) {
                return false;
            }
            if (ndx >= tokens.size()) {
                return false;
            }
            IToken t = tokens.get(ndx);
            if (t.start() >= end) {
                return false;
            }
            return true;
        }

        @Override
        public IToken next() {
            return tokens.get(ndx++);
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }

    }

	public Iterator<IToken> getTokens(int start, int end) {
		return new TokenIterator(start, end);
	}

}
