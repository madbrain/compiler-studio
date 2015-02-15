package org.xteam.cs.runtime;

import java.io.IOException;
import java.io.Reader;
import java.util.Map;


public class TableBasedLexer implements IStatedLexer, IStateAttributes {

	private int lexicalState;
	private ILexerInput input;
	private short[] charMap;
	private short[] rowMap;
	private short[] transitions;
	private short[] attributes;
	private boolean atEOF;
	private StringBuffer buffer;
	private ILexerTables tables;
	private ITokenFactory factory;
	private IErrorReporter reporter;
	private int previousState;
	private boolean skipComments = false;
	private IConverter converter;

	public TableBasedLexer(ILexerTables tables, IConverter converter, ITokenFactory factory) {
		this.tables = tables;
		this.factory = factory;
		this.converter = converter;
		charMap = tables.charMapTable();
		rowMap = tables.rowMapTable();
		transitions = tables.transitionTable();
		attributes = tables.attributeTable();
		buffer = new StringBuffer();
	}

	public int initialState() {
		return 0;
	}

	public int getState() {
		return lexicalState;
	}

	public void setState(int state) {
		lexicalState = state;
	}
	
	public void skipComments(boolean doSkip) {
		skipComments  = doSkip;
	}

	public void setInput(ILexerInput input) {
		this.input = input;
		this.atEOF = false;
	}
	
	public void setInput(Reader reader) {
		this.input = new ReaderLexerInput(reader);
		this.atEOF = false;
	}
	
	public void setErrorReporter(IErrorReporter reporter) {
		this.reporter = reporter;
	}
	
	public IToken nextToken() throws IOException {
		buffer.setLength(0);
		int position = input.position();
		while (true) {
			LexerAction action = scanOneToken();
			if (action == null) {
				// XXX should accumulate bad token in a single token
				error(new Span(position, buffer.length()), "Undefined token '" + buffer + "'");
				buffer.setLength(0);
			} else {
				if ((action.code & LexerAction.NEXT) != 0) {
					previousState = lexicalState;
					setState(action.nextValue);
				}
				
				if ((action.code & LexerAction.RETURN) != 0) {
					lexicalState = previousState;
				}
				
				if ((action.code & LexerAction.COMMENT) != 0 && skipComments) {
					buffer.setLength(0);
					position = input.position();
					continue;
				}
				
				if ((action.code & LexerAction.TOKEN) != 0) {
					Object contents = null;
					int type = action.returnValue;
					if ((action.code & LexerAction.KEYWORDS) != 0)
						type = keywordMatch(action.keywordMap, buffer.toString(), type);
					if ((action.code & LexerAction.VALUE) != 0) {
						contents = buffer.toString();
						if ((action.code & LexerAction.CONVERT) != 0 && converter != null)
							contents = converter.convert(action.convertCode, buffer.toString());
					}
					return factory.newToken(type, new Span(position, buffer.length()), contents);
				}
				
				if ((action.code & LexerAction.KEEP) == 0) {
					buffer.setLength(0);
					position = input.position();
				}
				
				if ((action.code & LexerAction.ERROR) != 0) {
					error(new Span(position, buffer.length()), action.error);
				}
			}
		}
	}

	private int keywordMatch(Map<String, Integer> keywordMap, String value,
			int type) {
		for (String keyword : keywordMap.keySet()) {
			if (value.equals(keyword))
				return keywordMap.get(keyword);
		}
		return type;
	}

	private void error(Span span, String msg) throws IOException {
		if (reporter != null)
			reporter.reportError(IErrorReporter.ERROR, span, msg);
		else
			throw new IOException(msg);
	}

	protected LexerAction scanOneToken() {
		int state = lexicalState;
		int c = 0;
		boolean wasPushback = false;
		int action = -1;
		int lookPushback = 0;
		int mark = 0;
		
		while (state >= 0 && ! atEOF) {
			c = input.next();
			if (c < 0) {
				atEOF = true;
				break;
			}
			int next = transitions[rowMap[state] + charMap[c]];
			if (next < 0) {
				if (action < 0) {
					buffer.append((char)c);
					mark = buffer.length();
				} else
					input.putBack(String.valueOf((char)c));
				break;
			}
			buffer.append((char) c);
			state = next;
			++lookPushback;

			int attr = attributes[state];
			if ((attr & PUSHBACK) != 0)
				lookPushback = 0;

			if ((attr & FINAL) != 0) {
				wasPushback = (attr & LOOKEND) != 0;
				action = state;
				mark = buffer.length();
				// early exit if don't need anything more to decide
				if ((attr & NOLOOK) != 0)
					break;
			}
		}
		if (buffer.length() > mark) {
			input.putBack(buffer.substring(mark, buffer.length()));
			buffer.setLength(mark);
		}
		if (wasPushback) {
			input.putBack(buffer.substring(buffer.length()-lookPushback));
			buffer.setLength(buffer.length() - lookPushback);
		}
		
		if (buffer.length() == 0 && atEOF)
			return tables.getEOFAction(lexicalState);
		return action < 0 ? null : tables.getAction(action);
	}

}
