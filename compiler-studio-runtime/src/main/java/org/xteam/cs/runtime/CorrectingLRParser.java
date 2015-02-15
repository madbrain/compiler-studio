package org.xteam.cs.runtime;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


public class CorrectingLRParser extends BaseParser {
	
	private static class Symbol {
		
	    public int parseState;
		public Object object;
		public Span span;

	    public Symbol(IToken token, int state) {
	    	this.span = new Span(token.start(), token.length());
	    	this.object = token;
	    	this.parseState = state;
	    }
	    
	    public Symbol(int state) {
			parseState = state;
	    }
	    
	    public Symbol(int state, Span span, Object obj) {
			parseState = state;
			this.span = span;
			this.object = obj;
	    }
	    
		public String toString() {
	    	return "Symbol(" + parseState + ", " + object +")";
	    }

	}

	protected IErrorReporter reporter;
	private int startState;
	private ILexer lexer;
	private IRuleReducer reducer;
	private int acceptingRule;
	private ISyntaxHelper helper;
	private ITokenFactory tokenFactory;

	public CorrectingLRParser(IParseTables tables, ILexer lexer, ITokenFactory factory, IRuleReducer reducer,
			ISyntaxHelper helper, IErrorReporter reporter) {
		super(tables);
		this.lexer = lexer;
		this.startState = tables.startState();
		this.acceptingRule = tables.acceptingProduction();
		this.tokenFactory = factory;
		this.reporter = reporter;
		this.reducer = reducer;
		this.helper = helper;
	}

	public Object parse() throws IOException {

		LexState lexState = new LexState();
		ParseStack stack = new ParseStack(new Symbol(startState));
		Fifo fifo = new Fifo();
		fifo.put(stack, lexState);
		//ParseState state = new ParseState(lexState, stack, fifo);
		
		while (true) {
			DistanceResult da = distanceParse(lexState, stack, fifo, lookahead - fifo.size());
			if (da.distance == 0 || da.action == DistanceResult.ACCEPT)
				return normalParse(da.lexState, da.stack, da.fifo);
			fixError(fifo);
			lexState = fifo.top().lexState;
			stack = fifo.top().stack;
		}
	}
	
	private static final int lookahead = 15;
	private static final int minDelta = 3;
	private static final int minAdvance = 1;
	private static final int maxAdvance = 5;
	
	/**
	 * Try to correct the input token stream using the parser states
	 * saved in the fifo.
	 * 
	 * @param fifo
	 * @throws Exception
	 */
	private void fixError(Fifo fifo) throws IOException {

		List<Change> changes = new ArrayList<Change>();
		
		// try several kinds of correction at every position in the queue
		int position = fifo.size() - 1;
		for (FifoElement element : fifo) {
			tryDelete(1, element.lexState, element.stack, position, changes);
			tryDelete(2, element.lexState, element.stack, position, changes);
			tryDelete(3, element.lexState, element.stack, position, changes);
			trySubst(element.lexState, element.stack, position, changes);
			tryInsert(element.lexState, element.stack, position, changes);
			--position;
		}
		Change change = findBestChange(changes);
		reporter.reportError(IErrorReporter.ERROR,
				new Span(change.leftPos, change.rightPos - change.leftPos),
				"syntax error: " + change.toString(this));
		FifoElement element = fifo.dropLast(change.pos);
		LexState lexState = element.lexState;
		for (int i = 0; i < change.orig.size(); ++i) {
			lexState = lexState.getNextState();
		}
		for (IToken sym : change._new) {
			lexState = new LexState(sym, lexState);
		}
		fifo.put(element.stack, lexState);
	}
	
	/**
	 * Use some heuristics to find the best change
	 * in the list of possible change.
	 * 
	 * @param changes
	 * @return the best change
	 */
	private Change findBestChange(List<Change> changes) {
		List<Change> maxChanges = new ArrayList<Change>();
		int maxDistance = 0;
		for (Change change : changes) {
			if (maxChanges.isEmpty() || change.distance >= maxDistance) {
				if (change.distance > maxDistance)
					maxChanges.clear();
				maxChanges.add(change);
				maxDistance = change.distance;
			}
		}
		if (maxChanges.isEmpty())
			throw new RuntimeException("no valuable correction");
		Change deletion = null;
		for (Change change : maxChanges) {
			if (change.isInsertion())
				return change;
			if (change.isDeletion())
				deletion = change;
		}
		if (deletion != null)
			return deletion;
		return maxChanges.get(0);
	}
	
	private void tryDelete(int n, LexState lex, ParseStack stack, int queuePos, List<Change> results) throws IOException {
		IToken term = lex.get();
		int left = term.start();
		int right = term.end();
		List<IToken> accum = new ArrayList<IToken>();
		while (n > 0) {
			if (noShift(term.type())) 
				return;
			n = n-1;
			accum.add(term);
			right = term.end();
			lex = lex.getNextState();
			term = lex.get();
		}
		tryChange(lex, stack, queuePos, left, right, accum, new ArrayList<IToken>(), results);
	}
	
	private void tryInsert(LexState lex, ParseStack stack, int queuePos, List<Change> results) throws IOException {
		for (int t : terms(stack.symbol.parseState)) {
			tryChange(lex, stack, queuePos,
					lex.leftPos(), lex.leftPos(),
					new ArrayList<IToken>(),
					makeList(createToken(t, lex.token.start(), lex.token.length())), results);
		}
	}
	
	private void trySubst(LexState lex, ParseStack stack, int queuePos, List<Change> results) throws IOException {
		IToken orig = lex.get();
		if (! noShift(orig.type())) {
			for (int t : terms(stack.symbol.parseState)) {
				tryChange(lex.getNextState(), stack, queuePos,
		            				orig.start(), orig.end(),
		            				makeList(orig),
		            				makeList(createToken(t, orig.end(), 0)), results);
			}
		}
	}
	
	private static List<IToken> makeList(IToken sym) {
		List<IToken> res = new ArrayList<IToken>();
		res.add(sym);
		return res;
	}
	
	private IToken createToken(int t, int pos, int length) {
		return tokenFactory.newToken(t, new Span(pos, length), errorTokenValue(t));
	}
	
	private class LexState {

		private IToken token;
		private LexState next;

		public LexState() throws IOException {
			token = lexer.nextToken();
			next = null;
		}
		
		public int leftPos() {
			return token.start();
		}

		public IToken get() {
			return token;
		}

		public LexState getNextState() throws IOException {
			if (next == null) {
				next = new LexState();
			}
			return next;
		}

		public LexState(IToken token, LexState next) {
			this.token = token;
			this.next = next;
		}
		
	}
	
	private static class ParseStack {

		private Symbol symbol;
		private ParseStack next;

		public ParseStack(Symbol symbol) {
			this(symbol, null);
		}

		public ParseStack(Symbol symbol, ParseStack next) {
			this.symbol = symbol;
			this.next = next;
		}

		public Symbol getSymbol() {
			return symbol;
		}

		public ParseStack push(Symbol sym) {
			return new ParseStack(sym, this);
		}

		public ParseStack pop() {
			return next;
		}
		
	}
	
	private static class Change {
		
		int pos;
		int leftPos;
		int rightPos;
		int distance;
		List<IToken> orig;
		List<IToken> _new;
		
		public Change(int pos, int leftPos, int rightPos, int distance,
				List<IToken> orig, List<IToken> _new) {
			this.pos = pos;
			this.leftPos = leftPos;
			this.rightPos = rightPos;
			this.distance = distance;
			this.orig = orig;
			this._new = _new;
		}
		
		public boolean isDeletion() {
			return _new.isEmpty();
		}
		
		public boolean isInsertion() {
			return orig.isEmpty();
		}
		
		public boolean isSubstitution() {
			return orig.size() > 0 && _new.size() > 0;
		}
		
		public String toString(CorrectingLRParser parser) {
			StringBuffer buffer = new StringBuffer();
			if (orig.size() > 0 && _new.isEmpty()) {
				buffer.append("deleting ");
				buffer.append(parser.printSymbols(orig));
			} else if (orig.isEmpty() && _new.size() > 0) {
				buffer.append("inserting ");
				buffer.append(parser.printSymbols(_new));
			} else {
				buffer.append("replacing ");
				buffer.append(parser.printSymbols(orig));
				buffer.append(" with ");
				buffer.append(parser.printSymbols(_new));
			}
			return buffer.toString();
		}
		
	}
	
	private void tryChange(LexState lex, ParseStack stack, int pos,
			int leftPos, int rightPos, List<IToken> orig, List<IToken> _new, List<Change> changes) throws IOException {
		
		 for (int i = _new.size() - 1; i >= 0; --i) {
			 lex = new LexState(_new.get(i), lex);
		 }
		 
		 int distance = tryParse(lex, stack, pos + _new.size() - orig.size());
		 
		 //System.out.println("CHANGE " + printSymbols(orig)
		 // + " TO " + printSymbols(_new) + " -> " + distance);
		 
		 if (distance >= (minAdvance + keywordsDelta(_new))) { 
			 changes.add(new Change(pos, leftPos, rightPos, distance, orig, _new));
		 }
	}
	
	private int tryParse(LexState lexState, ParseStack stack, int queuePos) throws IOException {
		DistanceResult da = distanceParse(lexState, stack, new Fifo(), queuePos + maxAdvance);
		if (da.action == DistanceResult.ACCEPT)
			System.out.println("try is accepting " + da.distance);
		if (da.action == DistanceResult.ACCEPT && (maxAdvance - da.distance) >= 0)
			return maxAdvance;
		return maxAdvance - da.distance;
	}
	
	private int keywordsDelta(List<IToken> _new) {
		for (IToken t : _new) {
			if (isKeyword(t.type()))
				return minDelta;
		}
		return 0;
	}
	
	private int[] terms(int state) {
		short[] row = actionTab[state];
		int[] result = new int[row.length];
		int index = 0;
		for (int i = 0; i < row.length; i += 2) {
			if (row[i] < 0)
				return allTerms();
			if (row[i + 1] != 0) {
				result[index++] = row[i];
			}
		}
		int[] res = new int[index];
		System.arraycopy(result, 0, res, 0, index);
		return res; 
	}
	
	protected int[] allTerms() {
		return new int[0];
	}
	
	protected boolean isKeyword(int t) {
		return false;
	}
	
	protected boolean noShift(int t) {
		return t == 0; // don't shift EOF
	}
	
	protected Object errorTokenValue(int t) {
		//return helper.getTokenValue(t);
		return null;
	}
	
	protected String errorTokenName(int t) {
		return helper.getTokenString(t);
	}
	
	private Object normalParse(LexState lexState, ParseStack stack, Fifo fifo) throws IOException {
		if (stack.getSymbol().parseState < 0)
			return stack.getSymbol(); // early accept
		while (true) {
			IToken cur_token = lexState.get();
			int act = getAction(stack.getSymbol().parseState, cur_token.type());

			if (isShift(act)) {
				int state = toShift(act);
				stack = stack.push(new Symbol(cur_token, state));
				lexState = lexState.getNextState();
				fifo.putAndGet(stack, lexState); 
			}
			else if (isReduce(act)) {
				int rule = toReduce(act);

				int lhs_sym_num = productionTab[rule][0];
				int handle_size = productionTab[rule][1];

				Object[] values = new Object[handle_size];
				Span span = Span.NULL;
                for (int i = 0; i < handle_size; i++) {
                	Symbol sym = stack.getSymbol();
                	span = span.merge(sym.span);
                	values[handle_size - i - 1] = sym.object;
                	stack = stack.pop();
                }
                Object semValue = reducer.reduce(rule, values);
                if (rule == acceptingRule)
					return semValue;
				int state = getReduce(stack.getSymbol().parseState, lhs_sym_num);
				if (state < 0) {
					System.out.println("bad goto state in reduce");
				}
				stack = stack.push(new Symbol(state, span, semValue));
			}
			else if (act == 0) {
				fixError(fifo);
				lexState = fifo.top().lexState;
				stack = fifo.top().stack;
			}
		}
	}
	
	private static class DistanceResult {
		
		public static final int DISTANCE = 0;
		public static final int ERROR = 1;
		public static final int ACCEPT = 2;
		
		LexState lexState;
		ParseStack stack;
		Fifo fifo;
		int action;
		int distance;
		
		public DistanceResult(LexState lexState, ParseStack stack, Fifo fifo, int distance, int action) {
			this.lexState = lexState;
			this.stack = stack;
			this.fifo = fifo;
			this.distance = distance;
			this.action = action;
		}
	}
	
	private static class FifoElement {
		
		LexState lexState;
		ParseStack stack;

		public FifoElement(ParseStack stack, LexState lexState) {
			this.stack = stack;
			this.lexState = lexState;
		}
		
	}
	
	private static class Fifo implements Iterable<FifoElement> {
		
		private List<FifoElement> elements = new ArrayList<FifoElement>();
		
		public void put(ParseStack stack, LexState lexState) {
			elements.add(new FifoElement(stack, lexState));
		}

		public int size() {
			return elements.size();
		}

		public void putAndGet(ParseStack stack, LexState lexState) {
			elements.remove(0);
			put(stack, lexState);
		}

		public FifoElement dropLast(int pos) {
			FifoElement e = null;
			while (pos-- >= 0) {
				e = elements.remove(elements.size()-1);
			}
			return e;
		}
		
		public FifoElement top() {
			return elements.get(elements.size()-1);
		}

		public Iterator<FifoElement> iterator() {
			return elements.iterator();
		}

	}
	
	private DistanceResult distanceParse(LexState lexState, ParseStack stack, Fifo fifo, int distance) throws IOException {
		//System.out.println("=== Distance parsing ("+distance+") ===");
		while (distance > 0) {
			IToken cur_token = lexState.get();
			int act = getAction(stack.getSymbol().parseState, cur_token.type());

			if (isShift(act)) {
				int state = toShift(act);
				//System.out.println("SHIFT " + cur_token + " to " + state);
				stack = stack.push(new Symbol(cur_token, state));
				lexState = lexState.getNextState();
				fifo.put(stack, lexState); 
				--distance;
			} else if (isReduce(act)) {
				int rule = toReduce(act);
				
				//System.out.println("REDUCE " + rule);

				int lhs_sym_num = productionTab[rule][0];
				int handle_size = productionTab[rule][1];

				Object[] values = new Object[handle_size];
				Span span = Span.NULL;
                for (int i = 0; i < handle_size; i++) {
                	Symbol sym = stack.getSymbol();
                	span = span.merge(sym.span);
                	values[handle_size - i - 1] = sym.object;
                	stack = stack.pop();
                }
                Object semValue = reducer.reduce(rule, values);
				int state = getReduce(stack.getSymbol().parseState, lhs_sym_num);
				if (state < 0) {
					System.out.println("bad goto state in reduce");
				}
				stack = stack.push(new Symbol(state, span, semValue));
				if (rule == acceptingRule)
					return new DistanceResult(lexState, stack, fifo, distance, DistanceResult.ACCEPT);
			} else if (act == 0) {
				return new DistanceResult(lexState, stack, fifo, distance, DistanceResult.ERROR);
			}
		}	
		return new DistanceResult(lexState, stack, fifo, 0, DistanceResult.DISTANCE);
	}
	
	private String printSymbols(List<IToken> symbols) {
		StringBuffer buffer = new StringBuffer();
		buffer.append("[ ");
		for (IToken sym : symbols) {
			buffer.append(printSymbol(sym));
			buffer.append(" ");
		}
		buffer.append("]");
		return buffer.toString();
	}
	
	private String printSymbol(IToken sym) {
		if (sym.value() != null)
			return sym.value().toString();
		Object t = errorTokenName(sym.type());
		if (t != null)
			return t.toString();
		return sym.toString();
	}
	
}
