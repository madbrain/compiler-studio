package org.xteam.cs.lex.build;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.xteam.cs.lex.model.Action;
import org.xteam.cs.lex.model.ActionCode;
import org.xteam.cs.lex.model.Expr;
import org.xteam.cs.lex.model.LexerModel;
import org.xteam.cs.lex.model.LexicalState;
import org.xteam.cs.lex.model.RegularExpression;
import org.xteam.cs.lex.model.StringExpr;

public class LexerMapping {

	private Map<LexicalState, Integer> stateMap = new HashMap<LexicalState, Integer>();
	private Map<String, Integer> tokenMap = new HashMap<String, Integer>();
	private Map<Integer, String> inverseTokenMap = new HashMap<Integer, String>();
	private Map<String, Integer> tokenTypes = new HashMap<String, Integer>();
	private Map<Integer, String> tokenNames = new HashMap<Integer, String>();
	private Map<String, Integer> convertCodes = new HashMap<String, Integer>();
	private Map<String, Map<String, Integer>> keywordMappings = new HashMap<String, Map<String,Integer>>();

	public LexerMapping(LexerModel model) {
		buildStateMapping(model);
		buildTokenMapping(model);
	}
	
	public String getToken(int type) {
		return inverseTokenMap.get(type);
	}

	public int getStateNumber(LexicalState state) {
		if (state == null)
			return -1;
		return stateMap.get(state);
	}
	
	public List<LexicalState> getStates() {
		return new ArrayList<LexicalState>(stateMap.keySet());
	}
	
	public int getStateNumber(String name) {
		for (LexicalState state : stateMap.keySet()) {
			if (state.getName().equals(name))
				return stateMap.get(state);
		}
		return -1;
	}

	public int getTokenNumber(String token) {
		if (token == null || tokenMap.get(token) == null)
			return -1;
		return tokenMap.get(token);
	}

	private void buildStateMapping(LexerModel model) {
		int index = 0;
		LexicalState initial = model.getInitialState();
		if (initial != null) {
			this.stateMap.put(initial, index++);
		}
		for (LexicalState state : model.getStates()) {
			if (!stateMap.containsKey(state))
				this.stateMap.put(state, index++);
		}
	}
	
	private void buildTokenMapping(LexerModel model) {
		int index = 0;
		for (RegularExpression expr : model.getExpressions()) {
			Action action = expr.getAction();
			if (action.getToken() != null && ! tokenMap.containsKey(action.getToken())) {
				addToken(expr.getDefinition(), action, index);
				++index;
			}
		}
		List<ActionCode> codes = new ArrayList<ActionCode>();
		codes.add(ActionCode.TOKEN);
		addToken(null, new Action(codes, -1, "$EOF$", null, -1, null), index);
	}
	
	private void addToken(Expr expr, Action action, int index) {
		this.tokenMap.put(action.getToken(), index);
		this.inverseTokenMap.put(index, action.getToken());
		if (action.getCodes().contains(ActionCode.VALUE))
			this.tokenTypes.put(action.getToken(), action.getTokenType());
		if (action.getCodes().contains(ActionCode.CONVERT)
				&& ! convertCodes.containsKey(action.getConvertCode())) {
			this.convertCodes.put(action.getConvertCode(), convertCodes.size());
		}
		if (expr instanceof StringExpr) {
			this.tokenNames.put(index, "'" + ((StringExpr)expr).getValue() + "'");
		} else {
			this.tokenNames.put(index, "<" + action.getToken() + ">");
		}
	}
	
	public List<String> getTokens() {
		return new ArrayList<String>(tokenMap.keySet());
	}

	public boolean hasValue(String name) {
		return tokenTypes.containsKey(name);
	}

	public int getTokenType(String name) {
		return tokenTypes.get(name);
	}

	public int getStateCount() {
		return stateMap.size();
	}

	public List<Integer> getTokenValues() {
		return new ArrayList<Integer>(tokenNames.keySet());
	}

	public String getTokenName(int t) {
		return tokenNames.get(t);
	}

	public List<String> getConvertCodes() {
		return new ArrayList<String>(convertCodes.keySet());
	}

	public int getConvertCode(String code) {
		if (code == null)
			return -1;
		return convertCodes.get(code);
	}

	public void setKeywordMapping(String keywordToken,
			Map<String, Integer> keywordMapping) {
		keywordMappings.put(keywordToken, keywordMapping);
	}

	public Map<String, Integer> getKeywordMapping(String token) {
		return keywordMappings.get(token);
	}

}
