package org.xteam.cs.lex.build;

import java.util.Stack;

import org.xteam.cs.lex.model.AnyCharExpr;
import org.xteam.cs.lex.model.CaselessStringExpr;
import org.xteam.cs.lex.model.CharClassExpr;
import org.xteam.cs.lex.model.ConcatExpr;
import org.xteam.cs.lex.model.Expr;
import org.xteam.cs.lex.model.IExprVisitor;
import org.xteam.cs.lex.model.InverseCharClassExpr;
import org.xteam.cs.lex.model.NegationExpr;
import org.xteam.cs.lex.model.PlusExpr;
import org.xteam.cs.lex.model.QuestionExpr;
import org.xteam.cs.lex.model.StarExpr;
import org.xteam.cs.lex.model.StringExpr;
import org.xteam.cs.lex.model.UnionExpr;
import org.xteam.cs.lex.model.UpToExpr;

public class NFABuilder implements IExprVisitor {

	private NFA nfa;
	private Stack<IntPair> stack;

	public NFABuilder(NFA nfa) {
		this.nfa = nfa;
		this.stack = new Stack<IntPair>();
	}
	
	@Override
	public void visitAnyChar(AnyCharExpr anyCharExpr) {
		IntPair a = pushNew();
		for (int cl = 0; cl < nfa.classes.getNumClasses(); ++cl) {
			nfa.addTransition(a.start(), cl, a.end());
		}
	}

	public void visitCaselessStringExpr(CaselessStringExpr exp) {
		 push(nfa.insertStringNFA(true, exp.getValue()));
	}

	@Override
	public void visitString(StringExpr exp) {
		push(nfa.insertStringNFA(false, exp.getValue()));
	}

	@Override
	public void visitCharclass(CharClassExpr exp) {
		IntPair a = pushNew();
		nfa.insertClassNFA(exp.getIntervals(), a.start(), a.end());
	}
	
	@Override
	public void visitInverseCharclass(InverseCharClassExpr exp) {
		IntPair a = pushNew();
		nfa.insertNotClassNFA(exp.getIntervals(), a.start(), a.end());
	}
	
	@Override
	public void visitUnion(UnionExpr exp) {
		IntPair a = pushNew();
		for (Expr e : exp.getExprs()) {
			e.visit(this);
			IntPair nfa1 = pop();
			nfa.addEpsilonTransition(a.start, nfa1.start());
			nfa.addEpsilonTransition(nfa1.end(), a.end);
		}
	}

	@Override
	public void visitConcat(ConcatExpr exp) {
		int start = 0;
		IntPair last = null;
		for (Expr e : exp.getExprs()) {
			e.visit(this);
			IntPair pair = pop();
			if (last == null) {
				start = pair.start;
			} else {
				nfa.addEpsilonTransition(last.end(), pair.start());
			}
			last = pair;
		}
		push(start, last.end());
	}

	@Override
	public void visitNegation(NegationExpr exp) {
		exp.getExpr().visit(this);
		push(nfa.complement(pop()));
	}

	@Override
	public void visitPlus(PlusExpr exp) {
		exp.getExpr().visit(this);
		IntPair nfa1 = pop();
		
		int start = nfa1.end() + 1;
		int end = nfa1.end() + 2;               
	        
		nfa.addEpsilonTransition(nfa1.end(), end);     
		nfa.addEpsilonTransition(start, nfa1.start());
		nfa.addEpsilonTransition(nfa1.end(), nfa1.start());
	        
		push(start, end);
	}

	@Override
	public void visitQuestion(QuestionExpr exp) {
		exp.getExpr().visit(this);
		IntPair nfa1 = pop();
		nfa.addEpsilonTransition(nfa1.start(), nfa1.end());
		push(nfa1);
	}

	@Override
	public void visitStar(StarExpr exp) {
		exp.getExpr().visit(this);
		IntPair nfa1 = pop();
	        
		int start = nfa1.end()+1;
		int end = nfa1.end()+2;               
	        
		nfa.addEpsilonTransition(nfa1.end(), end);     
		nfa.addEpsilonTransition(start, nfa1.start());
	        
		nfa.addEpsilonTransition(start, end);
		nfa.addEpsilonTransition(nfa1.end(), nfa1.start());
	        
		push(start, end);
	}

	@Override
	public void visitUpTo(UpToExpr exp) {
		exp.getExpr().visit(this);
		IntPair nfa1 = pop();
          
		int start = nfa1.end() + 1;
		int s1    = start+1;
		int s2    = s1+1;
		int end   = s2+1;

		for (int i = 0; i < nfa.numInput(); i++) {
			nfa.addTransition(s1,i,s1);
			nfa.addTransition(s2,i,s2);
		}

		nfa.addEpsilonTransition(start, s1);
		nfa.addEpsilonTransition(s1, nfa1.start());
		nfa.addEpsilonTransition(nfa1.end(), s2);
		nfa.addEpsilonTransition(s2, end);

		nfa1 = nfa.complement(new IntPair(start, end));
		exp.getExpr().visit(this);
		IntPair nfa2 = pop();
	        
		nfa.addEpsilonTransition(nfa1.end(), nfa2.start());

		push(nfa1.start(), nfa2.end());
	}
	
	private void push(IntPair pair) {
		stack.push(pair);
	}
	
	private void push(int start, int end) {
		stack.push(new IntPair(start, end));
	}
	
	private IntPair pushNew() {
		IntPair a = nfa.newIntPair();
		push(a);
		return a;
	}
	
	private IntPair pop() {
		return (IntPair) stack.pop();
	}

	public IntPair build(Expr exp) {
		exp.visit(this);
		return pop();
	}

}
