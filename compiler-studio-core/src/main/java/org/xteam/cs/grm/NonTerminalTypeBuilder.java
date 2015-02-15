package org.xteam.cs.grm;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.xteam.cs.grm.ast.RuleAst;
import org.xteam.cs.grm.model.AstListAction;
import org.xteam.cs.grm.model.AstNodeAction;
import org.xteam.cs.grm.model.Grammar;
import org.xteam.cs.grm.model.LexerStateAction;
import org.xteam.cs.grm.model.NonTerminal;
import org.xteam.cs.grm.model.PropagateAction;
import org.xteam.cs.grm.model.Rule;
import org.xteam.cs.grm.model.Symbol;
import org.xteam.cs.grm.model.Terminal;
import org.xteam.cs.runtime.IErrorReporter;
import org.xteam.cs.runtime.Span;
import org.xteam.cs.types.ListType;
import org.xteam.cs.types.NodeType;
import org.xteam.cs.types.PrimitiveType;
import org.xteam.cs.types.Type;
import org.xteam.cs.types.VoidType;


public class NonTerminalTypeBuilder {
	
	private IErrorReporter reporter;
	private Map<NonTerminal, DependencyNode> dependencies =
		new HashMap<NonTerminal, DependencyNode>();

	public NonTerminalTypeBuilder(IErrorReporter reporter) {
		this.reporter = reporter;
	}
	
	private static class DependencyNode {

		NonTerminal nt;
		Type type;
		Type resolvedType;
		Map<ReferenceType, DependencyNode> children = new HashMap<ReferenceType, DependencyNode>();
		Map<Rule, List<DependencyNode>> pendings = new HashMap<Rule, List<DependencyNode>>();
		boolean isResolving;

		public DependencyNode(NonTerminal nt) {
			this.nt = nt;
			this.resolvedType = null;
		}

		public void add(ReferenceType type, DependencyNode dep) {
			children.put(type, dep);
		}
		
		public void add(Rule rule, List<DependencyNode> nodes) {
			pendings.put(rule, nodes);
		}
		
		private boolean collectCycle(Set<DependencyNode> nodes) {
			return collectCycle(nodes, null);
		}
		
		private boolean collectCycle(Set<DependencyNode> nodes, DependencyNode start) {
			if (start == this) {
				return true;
			}
			if (nodes.contains(this)) {
				return false;
			}
			nodes.add(this);
			if (start == null)
				start = this;
			boolean cycle = false;
			for (ReferenceType rt : children.keySet()) {
				if (!(rt instanceof ListReferenceType)) {
					DependencyNode dep = children.get(rt);
					if (dep != null) {
						cycle |= dep.collectCycle(nodes, start);
					}
				}
			}
			for (Rule rule : pendings.keySet()) {
				for (DependencyNode dep : pendings.get(rule)) {
					cycle |= dep.collectCycle(nodes, start);
				}
			}
			return cycle;
		}

		public Type getType() {
			if (resolvedType == null) {
				if (isResolving)
					return type; // XXX cycle is handled badly
				isResolving = true;
				for (ReferenceType rt : children.keySet()) {
					DependencyNode dep = children.get(rt);
					Type t = new VoidType(Span.NULL);
					if (dep != null)
						t = dep.getType();
					if (type == null) {
						type = rt.resolve(t);
					} else {
						type = merge(type, rt.resolve(t));
					}
				}
				for (Rule rule : pendings.keySet()) {
					Type ntype = null;
					for (DependencyNode dep : pendings.get(rule)) {
						Type t = dep.getType();
						if (!(t instanceof VoidType)) {
							if (ntype == null)
								ntype = t;
							else {
								ntype = new ErrorType(Span.NULL);
							}
						}
					}
					if (type == null) {
						type = ntype;
					} else {
						type = merge(type, ntype);
					}
				}
				if (type == null)
					type = new ErrorType(Span.NULL);
				resolvedType = type;
				isResolving = false;
			}
			return resolvedType;
		}
		
		@Override
		public String toString() {
			return nt.getName() + "(" + (resolvedType == null ? "?" : resolvedType.toString()) + ")";
		}
		
	}
	
	public void build(Grammar grammar, Map<Rule, RuleAst> ruleMap) {
		
		for (Symbol sym : grammar.getSymbols()) {
			if (!sym.isTerminal()) {
				dependencies.put((NonTerminal)sym, new DependencyNode((NonTerminal)sym));
			}
		}
		
		for (Symbol sym : grammar.getSymbols()) {
			if (!sym.isTerminal()) {
				NonTerminal nt = (NonTerminal)sym;
				DependencyNode ntNode = dependencies.get(nt);
				Type ntType = null;
				for (Rule rule : nt.getRules()) {
					RuleAst ruleAst = ruleMap.get(rule);
					Type type = makeType(rule, ruleAst.span());
					if (type == null) {
						List<DependencyNode> nodes = new ArrayList<DependencyNode>();
						for (Symbol s : rule.getRhs()) {
							if (! s.isTerminal()) {
								nodes.add(dependencies.get(s));
							}
						}
						ntNode.add(rule, nodes);
					} else if (type instanceof ReferenceType) {
						ReferenceType rt = (ReferenceType) type;
						ntNode.add(rt, dependencies.get(rt.ref()));
					} else {
						if (ntType == null) {
							ntType = type;
						} else {
							ntType = merge(nt, ntType, type, ruleAst.span());
						}
					}
				}
				ntNode.type = ntType;
			}
		}
		// check for cycles
		for (NonTerminal nt : dependencies.keySet()) {
			DependencyNode t = dependencies.get(nt);
			Set<DependencyNode> nodes = new HashSet<DependencyNode>();
			if (t.collectCycle(nodes)) {
				Type newType = new VoidType(Span.NULL);
				for (DependencyNode dep : nodes) {
					if (dep.type != null)
						newType = merge(newType, dep.type);
				}
				for (DependencyNode dep : nodes) {
					dep.type = newType;
				}
			}
		}
		for (NonTerminal nt : dependencies.keySet()) {
			Type t = dependencies.get(nt).getType();
			if (t instanceof ErrorType) {
				makeError(ruleMap.get(nt.getRules().get(0)).span(),
						"cannot resolve type of '" + nt.getName() + "'");
			}
			nt.setType(t);
		}
	}
	
	private Type merge(NonTerminal nt, Type old, Type newType, Span span) {
		Type mergedAction = merge(old, newType);
		if (mergedAction != null) {
			if (! mergedAction.equals(old)) {
				return mergedAction;
			}
			return old;
		} 
		makeError(span, "cannot merge " + old + " and "
				+ newType + " on " + nt.getName());
		return new ErrorType(span);
	}
	
	private static Type merge(Type old, Type newType) {
		if (old instanceof ListType) {
			if (newType instanceof ListType) {
				return new ListType(Span.NULL,
						mergeListElement(((ListType)old).getElementType(), ((ListType)newType).getElementType()));
			}
		} else if (old instanceof NodeType) {
			if (newType instanceof NodeType) {
				return ((NodeType)old).merge((NodeType)newType);
			}
			if (newType instanceof VoidType) {
				return old;
			}
		} else if (old instanceof PrimitiveType) {
			if (newType instanceof PrimitiveType) {
				if (old.equals(newType))
					return old;
			}
		} else if (old instanceof VoidType) {
			if (newType instanceof PrimitiveType
					|| newType instanceof NodeType
					|| newType instanceof VoidType)
				return newType;
		}
		return new ErrorType(Span.NULL);
	}
	
	private static Type mergeListElement(Type old, Type newType) {
		if (old instanceof ListType) {
			if (newType instanceof ListType) {
				return new ListType(Span.NULL,
						mergeListElement(((ListType)old).getElementType(), ((ListType)newType).getElementType()));
			}
			if (newType instanceof VoidType)
				return old;
		} else if (old instanceof VoidType) {
			return newType;
		} else if (newType instanceof VoidType) {
			return old;
		}
		return merge(old, newType);
	}

	private Type makeType(Rule rule, Span span) {
		if (rule.getAction() instanceof AstListAction) {
			return new ListReferenceType(span, findListElementSymbol(rule, span));
		}
		if (rule.getAction() instanceof LexerStateAction) {
			return new VoidType(span);
		}
		if (rule.getAction() instanceof PropagateAction) {
			PropagateAction action = (PropagateAction) rule.getAction();
			if (action.getIndex() == -2)
				return null;
			if (action.getIndex() == -1)
				return new VoidType(span);
			if (rule.getRhs().get(action.getIndex()).isTerminal())
				return ((Terminal)rule.getRhs().get(action.getIndex())).getType();
			return new ReferenceType(span, (NonTerminal) rule.getRhs().get(action.getIndex()));
		}
		if (rule.getAction() == null)
			return new ErrorType(span);
		return new NodeType(span, ((AstNodeAction) rule.getAction()).getNode());
	}
	
	private NonTerminal findListElementSymbol(Rule rule, Span span) {
		if (rule.getRhs().isEmpty())
			return null;
		NonTerminal lhs = rule.getLhs();
		for (Symbol ref : rule.getRhs()) {
			if (! ref.isTerminal() && ref != lhs) {
				return (NonTerminal)ref;
			}
		}
		makeError(span, "bad list rule '" + rule + "'");
		return null;
	}
	
	private void makeError(Span span, String msg) {
		if (span != null)
			reporter.reportError(IErrorReporter.ERROR, span, msg);
	}
	
	public static class ErrorType extends Type {

		public ErrorType(Span span) {
			super(span);
		}

		@Override
		public String toString() {
			return "Error";
		}
		
	}
	
	public static class ReferenceType extends Type {
		
		protected NonTerminal ref;

		public ReferenceType(Span span, NonTerminal ref) {
			super(span);
			this.ref = ref;
		}
		
		public NonTerminal ref() {
			return ref;
		}
		
		public Type resolve(Type newAction) {
			return newAction;
		}

		public String toString() {
			return "Ref(" + ref.getName() + ")";
		}
	}
	
	public static class ListReferenceType extends ReferenceType {

		public ListReferenceType(Span span, NonTerminal nonTerminal) {
			super(span, nonTerminal);
		}
		
		@Override
		public boolean isRepeatable() {
			return true;
		}

		public Type resolve(Type newType) {
			return new ListType(span, newType);
		}

		public boolean equals(Object o) {
			return o instanceof ListType;
		}
		
		public String toString() {
			return "ListRef<"+(ref==null ? "?" : ref.toString())+">";
		}
		
	}
	
}
