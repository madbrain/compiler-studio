package org.xteam.cs.runtime;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Stack;

public class GLRParser extends BaseParser {

	private ILexer lexer;
	private ISyntaxHelper helper;
	private IGLRRuleReducer reducer;
	private IErrorReporter reporter;
	
	private Set<StackNode> topmost;
	private List<Path> pathQueue;
	private int startState;
	private int acceptingRule;
	
	private boolean debug = false;

	public GLRParser(IParseTables tables, ILexer lexer,
			ISyntaxHelper helper, IGLRRuleReducer reducer, IErrorReporter reporter) {
		super(tables);
		this.startState = tables.startState();
		this.lexer = lexer;
		this.helper = helper;
		this.reducer = reducer;
		this.acceptingRule = tables.acceptingProduction();
		this.reporter = reporter;
		topmost = new HashSet<StackNode>();
		pathQueue = new ArrayList<Path>();
	}
	
	private void debug(String msg) {
		if (debug)
			System.out.println(msg);
	}

	public Set<Object> parse() throws IOException {
		StackNode start = new StackNode(startState);
		topmost.add(start);
		pathQueue.clear();
		IToken t;
		do {
			debug("=== topmost: " + topmost);
			t = nextToken();
			debug("token: " + helper.getTokenString(t.type()));
			doReductions(t);
			doShifts(t);
			if (topmost.isEmpty()) {
				reporter.reportError(IErrorReporter.ERROR, new Span(t.start(), t.length()),
						"Syntax error on token " + helper.getTokenString(t.type()));
				break;
			}
		} while (! helper.isEof(t.type()));
		
		if (topmost.isEmpty())
			return null;
		// at this point all topmost states accepted to shift EOF
		// they are thus accepting states
		Set<Object> results = new HashSet<Object>();
		for (StackNode node : topmost) {
			// skip first link which contains EOF
			for (Link link : node.links) {
				for (Link l2 : link.to.links) {
					results.add(l2.semanticValue);
				}
			}
		}
		return results;
	}

	private IToken nextToken() throws IOException {
		return lexer.nextToken();
	}

	private void doReductions(IToken t) {
		for (StackNode current : topmost) {
			short[] reduces = getReduces(current.state, t);
			for (int j = 0; j < reduces.length; ++j) {
				int len = productionTab[reduces[j]][1];
				addPathesFrom(current, len, reduces[j]);
			}
		}
		while (pathQueue.size() > 0) {
			reduceViaPath(pathQueue.remove(0), t);
		}
	}

	/**
	 * the length is in number of link.
	 * @param current
	 * @param len
	 * @param prod 
	 * @return
	 */
	private void addPathesFrom(StackNode current, int len, short prod) {
		addPathesFrom(current, len, prod, null);
	}
	
	private void addPathesFrom(StackNode current, int len, short prod, Link linkToUse) {
		//List pathes = new ArrayList();
		Stack<Path> work = new Stack<Path>();
		work.add(new Path(prod, current, false));
		while (work.size() > 0) {
			Path p = work.remove(0);
			if (p.size() == len) {
				if (linkToUse == null || p.linkIsUsed
						|| (p.size() == 0 && current == linkToUse.from))
					pathQueue.add(p);
			} else {
				StackNode node = p.left();
				if (node == null)
					node = current;
				for (Link link : node.links) {
					work.add(p.add(link, linkToUse));
				}
			}
		}
	}
	
	private StackNode lookup(short state) {
		for (StackNode node : topmost) {
			if (node.state == state)
				return node;
		}
		return null;
	}

	private void doShifts(IToken t) {
		Set<StackNode> prevTops = topmost;
		topmost = new HashSet<StackNode>();
		for (StackNode current : prevTops) {
			short dest = getShift(current.state, t);
			if (dest >= 0) {
				StackNode rightSib = lookup(dest);
				if (rightSib != null) {
					rightSib.addLink(current, t);
				} else {
					rightSib = new StackNode(dest);
					topmost.add(rightSib);
					rightSib.addLink(current, t);
				}
				debug("shift " + helper.getTokenString(t.type())
						+ " from " + current + " to " + rightSib);
			}
		}
	}

	private void reduceViaPath(Path p, IToken t) {
		int prod = p.production();
		Object[] toPass = p.collectValues();
		Object newSemanticValue = reducer.reduce(prod, toPass);
		//boolean isAccepting = prod == acceptingRule;
		int symbol = productionTab[prod][0]; // lhs symbol of production
		debug("reduce to " + newSemanticValue);
		StackNode leftSib = p.left();
		debug("left: " + leftSib);
		StackNode rightSib = lookup(getReduce(leftSib.state, symbol));
		if (rightSib != null) {
			Link link = rightSib.getLink(leftSib);
			if (link != null) {
				link.semanticValue = reducer.merge(symbol,
						link.semanticValue,
						newSemanticValue);
			} else {
				link = rightSib.addLink(leftSib, newSemanticValue);
				enqueueLimitedReductions(link, t);
			}
		} else {
			rightSib = new StackNode(getReduce(leftSib.state, symbol));
			Link link = rightSib.addLink(leftSib, newSemanticValue);
			topmost.add(rightSib);
			enqueueLimitedReductions(link, t);
			debug("top: " + rightSib);
		}
	}

	private void enqueueLimitedReductions(Link link, IToken t) {
		for (StackNode current : topmost) {
			short[] reduces = getReduces(current.state, t);
			for (int j = 0; j < reduces.length; ++j) {
				int len = productionTab[reduces[j]][1];
				addPathesFrom(current, len, reduces[j], link);
			}
		}
	}
	
	public short[] getReduces(int state, IToken t) {
		short[] actions = getActions(state, t.type());
		List<Short> list = new ArrayList<Short>();
		for (int i = 0; i < actions.length; ++i) {
			if (isReduce(actions[i]))
				list.add(toReduce(actions[i]));
		}
		short[] reduces = new short[list.size()];
		for (int i = 0; i < list.size(); ++i) {
			reduces[i] = list.get(i);
		}
		return reduces;
	}
	
	// XXX should have only one ?
	public short getShift(int state, IToken t) {
		short[] actions = getActions(state, t.type());
		//List list = new ArrayList();
		for (int i = 0; i < actions.length; ++i) {
			if (isShift(actions[i])) {
				return (short) (toShift(actions[i]));
			}
		}
		return -1;
	}
	
	private static class StackNode {
		int state;
		List<Link> links;
		int deterministicDepth;
		public StackNode(int state) {
			this.state = state;
			this.links = new ArrayList<Link>();
			this.deterministicDepth = 1;
		}
		public Link getLink(StackNode leftSib) {
			for (Link link : links) {
				if (link.to == leftSib)
					return link;
			}
			return null;
		}
		public Link addLink(StackNode to, Object semanticValue) {
			Link link = new Link(this, to, semanticValue);
			if (link.size() != 0)
				deterministicDepth = 0;
			to.deterministicDepth = deterministicDepth + 1;
			links.add(link);
			return link;
		}
		public String toString() {
			return "State(" + state + ")";
		}
	}

	private static class Link {
		StackNode from;
		StackNode to;
		Object semanticValue;
		public Link(StackNode from, StackNode to, Object semanticValue) {
			this.from = from;
			this.to = to;
			this.semanticValue = semanticValue;
		}
		public int size() {
			// TODO Auto-generated method stub
			return 0;
		}
		public String toString() {
			return to + " <- " + from;
		}
	}

	private static class Path {
		private List<Link> elements;
		private StackNode right;
		private short prod;
		private boolean linkIsUsed;
		
		public Path(short prod, StackNode node, boolean linkIsUsed) {
			this.right = node;
			this.prod = prod;
			this.linkIsUsed = linkIsUsed;
			elements = new ArrayList<Link>();
		}

		public int production() {
			return prod;
		}

		public StackNode left() {
			if (elements.isEmpty())
				return right;
			return ((Link)elements.get(0)).to;
		}

		public Path add(Link link, Link linkToUse) {
			Path p = new Path(prod, right, linkIsUsed || link == linkToUse);
			p.elements = new ArrayList<Link>(elements);
			p.elements.add(0, link);
			return p;
		}

		public int size() {
			return elements.size();
		}

		// XXX should duplicate ?
		public Object[] collectValues() {
			Object[] objs = new Object[elements.size()];
			for(int i = 0; i < elements.size(); ++i) {
				objs[i] = elements.get(i).semanticValue;
			}
			return objs;
		}
		
		public String toString() {
			StringBuffer buffer = new StringBuffer();
			for(int i = 0; i < elements.size(); ++i) {
				Link link = elements.get(i);
				buffer.append(link.to);
				buffer.append(" <- ");
			}
			buffer.append(right);
			return buffer.toString();
		}
	}

}
