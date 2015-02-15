package org.xteam.cs.lex.build;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.xteam.cs.lex.LexerProperties;
import org.xteam.cs.lex.model.Action;
import org.xteam.cs.lex.model.ActionCode;
import org.xteam.cs.lex.model.LexerModel;
import org.xteam.cs.lex.model.LexicalState;
import org.xteam.cs.lex.model.RegularExpression;
import org.xteam.cs.lex.model.StringExpr;
import org.xteam.cs.model.IProgressMonitor;
import org.xteam.cs.runtime.ILexerTables;

public class LexerTableGenerator {

	public LexerBuild run(LexerModel model,
			LexerProperties lexerProperties, IProgressMonitor monitor) {
		int amountOfWork = 5;
		if (lexerProperties.doKeywordOptimize)
			amountOfWork++;
		
		monitor.beginTask("Generate Lexer Table", amountOfWork);
		
		// create the lexical state mapping
		LexerMapping lexerMapping = new LexerMapping(model);
		
		if (lexerProperties.doKeywordOptimize) {
			optimizeKeywords(model, monitor, lexerMapping);
		}
		
		monitor.subTask("Creating charclass");

		CharClassBuilder ccBuilder = new CharClassBuilder();
		for (RegularExpression r : model.getExpressions()) {
			if (r.getDefinition() != null)
				ccBuilder.visit(r.getDefinition());
			if (r.getLookahead() != null)
				ccBuilder.visit(r.getLookahead());
		}

		monitor.worked(1);
		monitor.subTask("Generating Automaton");
		NFA nfa = new NFA(ccBuilder.getCharClasses(), model);
		NFABuilder builder = new NFABuilder(nfa);
		for (RegularExpression r : model.getExpressions()) {
			if (r.isEof())
				addEOFActions(model, r.getStates(), r.getAction());
			else
				nfa.addRegExp(r, builder, lexerMapping);
		}

		// default EOF rule
		Action defaultAction = makeEOFAction();
		addEOFActions(model, null, defaultAction);

		monitor.worked(1);
		monitor.subTask("Determinizing Automaton");
		DFA dfa = nfa.getDFA();
		dfa.checkActions(model);
		nfa = null;

		monitor.worked(1);
		monitor.subTask("Minimizing Automaton");
		if (! dfa.minimize()) {
			monitor.done();
			return null;
		}
		monitor.worked(1);

		monitor.subTask("Building Tables");

		LexerTableBuilder tableBuilder = new LexerTableBuilder(model);
		ILexerTables tables = tableBuilder.build(ccBuilder.getCharClasses(), lexerMapping, dfa);
		
		monitor.done();
		
		return new LexerBuild(model.getName(), lexerMapping, tables);
	}

	private void optimizeKeywords(LexerModel model, IProgressMonitor monitor,
			LexerMapping lexerMapping) {
		monitor.subTask("Optimizing");
		Map<String, RegularExpression> keywords = new HashMap<String, RegularExpression>();
		Set<LexicalState> states = new HashSet<LexicalState>();
		for (RegularExpression r : model.getExpressions()) {
			if (r.isBol() || r.isEof()
					|| r.getLookahead() != null
					|| ! r.getAction().getCodes().contains(ActionCode.TOKEN)
					|| ! (r.getDefinition() instanceof StringExpr))
				continue;
			if (states.isEmpty()) {
				states.addAll(r.getStates());
			} else if (! states.containsAll(r.getStates())) {
				continue;
			}
			keywords.put(((StringExpr)r.getDefinition()).getValue(), r);
			System.out.println("keyword: \'" + ((StringExpr)r.getDefinition()).getValue() + "\'");
		}
		List<RegularExpression> bestMatched = new ArrayList<RegularExpression>();
		RegularExpression keywordRule = null;
		for (RegularExpression r : model.getExpressions()) {
			if (! states.equals(new HashSet<LexicalState>(r.getStates()))
					|| keywords.values().contains(r)
					|| r.isBol() || r.isEof()
					|| r.getLookahead() != null
					|| ! r.getAction().getCodes().contains(ActionCode.TOKEN))
				continue;
			List<RegularExpression> matched = new ArrayList<RegularExpression>();
			for (String keyword : keywords.keySet()) {
				if (keywords.get(keyword).getAction().getPriority() < r.getAction().getPriority()
						&& ExprMatcher.match(r.getDefinition(), keyword)) {
					matched.add(keywords.get(keyword));
				}
			}
			if (bestMatched == null || bestMatched.size() < matched.size()) {
				keywordRule = r;
				bestMatched = matched;
			}
		}
		if (bestMatched.size() > 0) {
			System.out.println("===> found keywords rule " + keywordRule.getAction().getToken() + " for:");
			Map<String, Integer> keywordMapping = new HashMap<String, Integer>();
			for (RegularExpression r : bestMatched) {
				keywordMapping.put(((StringExpr)r.getDefinition()).getValue(),
						lexerMapping.getTokenNumber(r.getAction().getToken()));
				model.removeExpression(r);
			}
			System.out.println(keywordMapping);
			lexerMapping.setKeywordMapping(
					keywordRule.getAction().getToken(), keywordMapping);
			keywordRule.getAction().getCodes().add(ActionCode.KEYWORDS);
		}
		monitor.worked(1);
	}

	private Action makeEOFAction() {
		List<ActionCode> codes = new ArrayList<ActionCode>();
		codes.add(ActionCode.TOKEN);
		return new Action(codes, Integer.MAX_VALUE, "$EOF$", null, -1, null);
	}

	private void addEOFActions(LexerModel model, List<LexicalState> stateList, Action action) {
		// Add the EOF code to action
		if (stateList != null && stateList.size() > 0) {
			for (LexicalState state : stateList) {
				updateEofAction(state, action);
			}
		} else {
			model.setDefaultAction(action.getHighestPriority(model
					.getDefaultAction()));
			for (LexicalState state : model.getStates()) {
				updateEofAction(state, action);
			}
		}
	}

	private void updateEofAction(LexicalState state, Action action) {
		if (state.getEofAction() == null)
			state.setEofAction(action);
		else {
			state.setEofAction(state.getEofAction().getHighestPriority(action));
		}
	}

}
