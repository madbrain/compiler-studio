package org.xteam.cs.gui.grm;

import java.util.ArrayList;
import java.util.List;

import org.xteam.cs.grm.build.GrammarMapping;
import org.xteam.cs.grm.model.Action;
import org.xteam.cs.grm.model.AstListAction;
import org.xteam.cs.grm.model.AstNodeAction;
import org.xteam.cs.grm.model.Binding;
import org.xteam.cs.grm.model.LexerStateAction;
import org.xteam.cs.grm.model.PropagateAction;
import org.xteam.cs.runtime.IRuleReducer;
import org.xteam.cs.runtime.IStatedLexer;

public class ASTSyntaxReducer implements IRuleReducer {

	private GrammarMapping mapping;
	private IStatedLexer lexer;

	public ASTSyntaxReducer(GrammarMapping mapping, IStatedLexer lexer) {
		this.mapping = mapping;
		this.lexer = lexer;
	}

	@Override
	public Object reduce(int r, Object[] values) {
		Action action = mapping.getRuleFor(r).getAction();
		if (action instanceof LexerStateAction) {
			LexerStateAction lsa = (LexerStateAction) action;
			if (lsa.getCondition() < 0
					|| lsa.getCondition() == lexer.getState()) {
				lexer.setState(lsa.getState());
			}
			return null;
		}
		if (action instanceof AstListAction){
			AstListAction la = (AstListAction) action;
			GenericAstList l = null;
			if (la.getListIndex() < 0) {
				l = new GenericAstList();
			} else {
				l = (GenericAstList) values[la.getListIndex()];
			}
			if (la.getElementIndex() >= 0) {
				if (la.getElementIndex() > la.getListIndex())
					l.add((GenericAST)values[la.getElementIndex()]);
				else
					l.prepend((GenericAST)values[la.getElementIndex()]);
			}
			return l;
		}
		if (action instanceof PropagateAction){
			PropagateAction pa = (PropagateAction) action;
			if (pa.getIndex() < 0)
				return null;
			return values[pa.getIndex()];
		}
		AstNodeAction na = (AstNodeAction) action;
		List<ChildBinding> children = new ArrayList<ChildBinding>();
		for (Binding b : na.getBindings()) {
			if (b.getValue() == null)
				children.add(new ChildBinding(b.getField().getName(), (GenericAST)values[b.getIndex()]));
			else
				children.add(new ChildBinding(b.getField().getName(), new ASTTokenNode(0, null, 0, 0, b.getValue())));
		}
		return new GenericNode(na.getNode().getName(), children);
	}

}
