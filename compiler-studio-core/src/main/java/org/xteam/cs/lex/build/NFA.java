/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 * JFlex 1.4.1                                                             *
 * Copyright (C) 1998-2004  Gerwin Klein <lsf@jflex.de>                    *
 * All rights reserved.                                                    *
 *                                                                         *
 * This program is free software; you can redistribute it and/or modify    *
 * it under the terms of the GNU General Public License. See the file      *
 * COPYRIGHT for more information.                                         *
 *                                                                         *
 * This program is distributed in the hope that it will be useful,         *
 * but WITHOUT ANY WARRANTY; without even the implied warranty of          *
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the           *
 * GNU General Public License for more details.                            *
 *                                                                         *
 * You should have received a copy of the GNU General Public License along *
 * with this program; if not, write to the Free Software Foundation, Inc., *
 * 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA                 *
 *                                                                         *
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
package org.xteam.cs.lex.build;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import org.xteam.cs.lex.model.Action;
import org.xteam.cs.lex.model.IntCharSet;
import org.xteam.cs.lex.model.LexerModel;
import org.xteam.cs.lex.model.LexicalState;
import org.xteam.cs.lex.model.RegularExpression;


/**
 * NFA representation in JFlex.
 * 
 * Contains algorithms RegExp -> NFA and NFA -> DFA.
 * 
 * @author Gerwin Klein
 * @version JFlex 1.4.1, $Revision: 2.8 $, $Date: 2004/11/06 23:03:32 $
 */
final public class NFA {

    private static final int DEFAULT_SIZE = 30;

	// table[current_state][next_char] is the set of states that can be reached
    // from current_state with an input next_char
    StateSet[][] table;

    // epsilon[current_state] is the set of states that can be reached
    // from current_state via epsilon-edges
    StateSet[] epsilon;

    // isFinal[state] == true <=> state is a final state of the NFA
    boolean[] isFinal;

    // isPushback[state] == true <=> state is the final state of a regexp that
    // should only be matched when followed by a certain lookahead.
    boolean[] isPushback;

    // action[current_state]: the action associated with the state
    // current_state (null, if there is no action for the state)
    Action[] action;

    // the number of states in this NFA
    int numStates;

    // the current maximum number of input characters
    int numInput;

    // the number of lexical States. Lexical states have the indices
    // 0..numLexStates-1 in the transition table
    int numLexStates;

    CharClasses classes;

	private LexerModel model;
	
	private Set<Action> lookActions = new HashSet<Action>();

    // will be reused by several methods (avoids excessive object creation)
    private static StateSetEnumerator stateEnum = new StateSetEnumerator();

    private static StateSet tempStateSet = new StateSet();

    public NFA(int numInput) {
        this.numInput = numInput;
        numStates = 0;
        epsilon = new StateSet[DEFAULT_SIZE];
        action = new Action[DEFAULT_SIZE];
        isFinal = new boolean[DEFAULT_SIZE];
        isPushback = new boolean[DEFAULT_SIZE];
        table = new StateSet[DEFAULT_SIZE][numInput];
    }

    public NFA(CharClasses charClasses, LexerModel model) {
		this(charClasses.getNumClasses());
		this.model = model;
		this.classes = charClasses;
		numLexStates = model.getStates().size();
		ensureCapacity(2 * numLexStates);
		numStates = 2 * numLexStates;
	}

	public void addStandaloneRule(Action act) {
        // standalone rule has least priority, fires
        // transition on all characters and has "print it rule"
        int start = numStates;
        int end = numStates + 1;

        for (int c = 0; c < classes.getNumClasses(); c++)
            addTransition(start, c, end);

        for (int i = 0; i < numLexStates * 2; i++)
            addEpsilonTransition(i, start);
        action[end] = act;
        isFinal[end] = true;
    }

    public void addRegExp(RegularExpression expr, NFABuilder builder, LexerMapping lexerMapping) {
    	
    	IntPair nfa = builder.build(expr.getDefinition());
        
        List<LexicalState> lexStates = expr.getStates();

        if (lexStates.size() == 0)
            lexStates = model.getInclusiveStates();

        for (LexicalState state : lexStates) {
        	int lexicalStart = lexerMapping.getStateNumber(state);
            if (!expr.isBol())
                addEpsilonTransition(2 * lexicalStart, nfa.start());
            addEpsilonTransition(2 * lexicalStart + 1, nfa.start());
        }

        if (expr.getLookahead() != null) {
        	IntPair look = builder.build(expr.getLookahead());

            addEpsilonTransition(nfa.end(), look.start());

            Action a = expr.getAction();
            lookActions.add(a);
            //a.setLookAction(true); XXX -> replace by a Set

            isPushback[nfa.end()] = true;
            action[look.end()] = a;
            isFinal[look.end()] = true;
        } else {
            action[nfa.end()] = expr.getAction();
            isFinal[nfa.end()] = true;
        }
    }

    public void ensureCapacity(int newNumStates) {
        int oldLength = epsilon.length;

        if (newNumStates < oldLength)
            return;

        int newStatesLength = Math.max(oldLength * 2, newNumStates);

        boolean[] newFinal = new boolean[newStatesLength];
        boolean[] newIsPush = new boolean[newStatesLength];
        Action[] newAction = new Action[newStatesLength];
        StateSet[][] newTable = new StateSet[newStatesLength][numInput];
        StateSet[] newEpsilon = new StateSet[newStatesLength];

        System.arraycopy(isFinal, 0, newFinal, 0, numStates);
        System.arraycopy(isPushback, 0, newIsPush, 0, numStates);
        System.arraycopy(action, 0, newAction, 0, numStates);
        System.arraycopy(epsilon, 0, newEpsilon, 0, numStates);
        System.arraycopy(table, 0, newTable, 0, numStates);

        isFinal = newFinal;
        isPushback = newIsPush;
        action = newAction;
        epsilon = newEpsilon;
        table = newTable;
    }

    public void addTransition(int start, int input, int dest) {
        int maxS = Math.max(start, dest) + 1;

        ensureCapacity(maxS);

        if (maxS > numStates)
            numStates = maxS;

        if (table[start][input] != null)
            table[start][input].addState(dest);
        else
            table[start][input] = new StateSet(DEFAULT_SIZE, dest);
    }

    public void addEpsilonTransition(int start, int dest) {
        int max = Math.max(start, dest) + 1;
        ensureCapacity(max);
        if (max > numStates)
            numStates = max;

        if (epsilon[start] != null)
            epsilon[start].addState(dest);
        else
            epsilon[start] = new StateSet(DEFAULT_SIZE, dest);
    }

    /**
     * Returns <code>true</code>, iff the specified set of states contains a
     * final state.
     * 
     * @param set
     *            the set of states that is tested for final states.
     */
    private boolean containsFinal(StateSet set) {
        stateEnum.reset(set);

        while (stateEnum.hasMoreElements())
            if (isFinal[stateEnum.nextElement()])
                return true;

        return false;
    }

    /**
     * Returns <code>true</code>, iff the specified set of states contains a
     * pushback-state.
     * 
     * @param set
     *            the set of states that is tested for pushback-states.
     */
    private boolean containsPushback(StateSet set) {
        stateEnum.reset(set);

        while (stateEnum.hasMoreElements())
            if (isPushback[stateEnum.nextElement()])
                return true;

        return false;
    }

    /**
     * Returns the action with highest priority in the specified set of states.
     * 
     * @param set
     *            the set of states for which to determine the action
     */
    private Action getAction(StateSet set) {

        stateEnum.reset(set);

        Action maxAction = null;

        while (stateEnum.hasMoreElements()) {

        	Action currentAction = action[stateEnum.nextElement()];

            if (currentAction != null) {
                if (maxAction == null)
                    maxAction = currentAction;
                else
                    maxAction = maxAction.getHighestPriority(currentAction);
            }

        }

        return maxAction;
    }

    /**
     * Calculates the epsilon closure for a specified set of states.
     * 
     * The epsilon closure for set a is the set of states that can be reached by
     * epsilon edges from a.
     * 
     * @param set
     *            the set of states to calculate the epsilon closure for
     * 
     * @return the epsilon closure of the specified set of states in this NFA
     */
    private StateSet closure(int startState) {

        // Out.debug("Calculating closure of "+set);

        StateSet notvisited = tempStateSet;
        StateSet closure = new StateSet(numStates, startState);

        notvisited.clear();
        notvisited.addState(startState);

        while (notvisited.containsElements()) {
            // Out.debug("closure is now "+closure);
            // Out.debug("notvisited is "+notvisited);
            int state = notvisited.getAndRemoveElement();
            // Out.debug("removed element "+state+" of "+notvisited);
            // Out.debug("epsilon[states] = "+epsilon[state]);
            notvisited.add(closure.complement(epsilon[state]));
            closure.add(epsilon[state]);
        }

        // Out.debug("Closure is : "+closure);

        return closure;
    }

    /**
     * Returns the epsilon closure of a set of states
     */
    private StateSet closure(StateSet startStates) {
        StateSet result = new StateSet(numStates);

        if (startStates != null) {
            stateEnum.reset(startStates);
            while (stateEnum.hasMoreElements())
                result.add(closure(stateEnum.nextElement()));
        }

        return result;
    }

    private void epsilonFill() {
        for (int i = 0; i < numStates; i++) {
            epsilon[i] = closure(i);
        }
    }

    /**
     * Calculates the set of states that can be reached from another set of
     * states <code>start</code> with an specified input character
     * <code>input</code>
     * 
     * @param start
     *            the set of states to start from
     * @param input
     *            the input character for which to search the next states
     * 
     * @return the set of states that are reached from <code>start</code> via
     *         <code>input</code>
     */
    private StateSet DFAEdge(StateSet start, char input) {

        tempStateSet.clear();

        stateEnum.reset(start);
        while (stateEnum.hasMoreElements())
            tempStateSet.add(table[stateEnum.nextElement()][input]);

        StateSet result = new StateSet(tempStateSet);

        stateEnum.reset(tempStateSet);
        while (stateEnum.hasMoreElements())
            result.add(epsilon[stateEnum.nextElement()]);

        return result;
    }

    /**
     * Returns an DFA that accepts the same language as this NFA. This DFA is
     * usualy not minimal.
     */
    public DFA getDFA() {

        Hashtable dfaStates = new Hashtable(numStates);
        Vector dfaVector = new Vector(numStates);

        DFA dfa = new DFA(2 * numLexStates, numInput);

        int numDFAStates = 0;
        int currentDFAState = 0;

        epsilonFill();

        StateSet currentState, newState;

        for (int i = 0; i < 2 * numLexStates; i++) {
            newState = epsilon[i];

            dfaStates.put(newState, new Integer(numDFAStates));
            dfaVector.addElement(newState);

            dfa.setLexState(i, numDFAStates);

            dfa.setFinal(numDFAStates, containsFinal(newState));
            dfa.setPushback(numDFAStates, containsPushback(newState));
            dfa.setAction(numDFAStates, getAction(newState), lookActions);

            numDFAStates++;
        }

        numDFAStates--;

        currentDFAState = 0;

        StateSet tempStateSet = NFA.tempStateSet;
        StateSetEnumerator states = NFA.stateEnum;

        // will be reused
        newState = new StateSet(numStates);

        while (currentDFAState <= numDFAStates) {

            currentState = (StateSet) dfaVector.elementAt(currentDFAState);

            for (char input = 0; input < numInput; input++) {

                tempStateSet.clear();
                states.reset(currentState);
                while (states.hasMoreElements())
                    tempStateSet.add(table[states.nextElement()][input]);

                newState.copy(tempStateSet);

                states.reset(tempStateSet);
                while (states.hasMoreElements())
                    newState.add(epsilon[states.nextElement()]);

                if (newState.containsElements()) {

                    Integer nextDFAState = (Integer) dfaStates.get(newState);

                    if (nextDFAState != null) {
                        dfa.addTransition(currentDFAState, input, nextDFAState
                                .intValue());
                    } else {
                        numDFAStates++;

                        // make a new copy of newState to store in dfaStates
                        StateSet storeState = new StateSet(newState);

                        dfaStates.put(storeState, new Integer(numDFAStates));
                        dfaVector.addElement(storeState);

                        dfa.addTransition(currentDFAState, input, numDFAStates);
                        dfa.setFinal(numDFAStates, containsFinal(storeState));
                        dfa.setPushback(numDFAStates,
                                containsPushback(storeState));
                        dfa.setAction(numDFAStates, getAction(storeState), lookActions);
                    }
                }
            }
            currentDFAState++;
        }
        return dfa;
    }

    public String toString() {
        StringBuffer result = new StringBuffer();

        for (int i = 0; i < numStates; i++) {
            result.append("State");
            if (isFinal[i])
                result.append("[FINAL]");
            if (isPushback[i])
                result.append(" [PUSHBACK]");
            result.append(" " + i + "\n");

            for (char input = 0; input < numInput; input++) {
                if (table[i][input] != null
                        && table[i][input].containsElements())
                    result.append("  with " + ((int) input) + " in "
                            + table[i][input] + "\n");

            }

            if (epsilon[i] != null && epsilon[i].containsElements())
                result.append("  with epsilon in " + epsilon[i] + "\n");
        }

        return result.toString();
    }

    public void writeDot(File file) {
        try {
            PrintWriter writer = new PrintWriter(new FileWriter(file));
            writeDot(writer);
            writer.close();
        } catch (IOException e) {
            throw new RuntimeException("FILE_WRITE");
        }
    }

    public void writeDot(PrintWriter writer) {
        writer.println("digraph NFA {");
        writer.println("rankdir = LR");

        for (int i = 0; i < numStates; i++) {
            if (isFinal[i] || isPushback[i])
                writer.print(i);
            if (isFinal[i])
                writer.print(" [shape = doublecircle]");
            if (isPushback[i])
                writer.print(" [shape = box]");
            if (isFinal[i] || isPushback[i])
                writer.println();
        }

        for (int i = 0; i < numStates; i++) {
            for (int input = 0; input < numInput; input++) {
                if (table[i][input] != null) {
                    StateSetEnumerator states = table[i][input].states();

                    while (states.hasMoreElements()) {
                        int s = states.nextElement();
                        writer.print(i + " -> " + s);
                        writer.println(" [label=\""
                                + classes.toString(input) + "\"]");
                    }
                }
            }
            if (epsilon[i] != null) {
                StateSetEnumerator states = epsilon[i].states();
                while (states.hasMoreElements()) {
                    int s = states.nextElement();
                    writer.println(i + " -> " + s + " [style=dotted]");
                }
            }
        }
        writer.println("}");
    }

    // -----------------------------------------------------------------------
    // Functions for constructing NFAs out of regular expressions.
    // XXX should be moved to NFABuilder

    public void insertLetterNFA(boolean caseless, char letter, int start,
            int end) {
        if (caseless) {
            int lower = classes.getClassCode(Character.toLowerCase(letter));
            int upper = classes.getClassCode(Character.toUpperCase(letter));
            addTransition(start, lower, end);
            if (upper != lower)
                addTransition(start, upper, end);
        } else {
            addTransition(start, classes.getClassCode(letter), end);
        }
    }

    public IntPair insertStringNFA(boolean caseless, String letters) {
        int start = numStates;
        int i;

        for (i = 0; i < letters.length(); i++) {
            if (caseless) {
                char c = letters.charAt(i);
                int lower = classes.getClassCode(Character.toLowerCase(c));
                int upper = classes.getClassCode(Character.toUpperCase(c));
                addTransition(i + start, lower, i + start + 1);
                if (upper != lower)
                    addTransition(i + start, upper, i + start + 1);
            } else {
                addTransition(i + start, classes
                        .getClassCode(letters.charAt(i)), i + start + 1);
            }
        }

        return new IntPair(start, i + start);
    }

    public void insertClassNFA(List<IntCharSet.Interval> intervals, int start, int end) {
        // empty char class is ok:
        if (intervals == null)
            return;

        int[] cl = classes.getClassCodes(intervals);
        for (int i = 0; i < cl.length; i++)
            addTransition(start, cl[i], end);
    }

    public void insertNotClassNFA(List<IntCharSet.Interval> intervals, int start, int end) {
        int[] cl = classes.getNotClassCodes(intervals);

        for (int i = 0; i < cl.length; i++)
            addTransition(start, cl[i], end);
    }

    /**
     * Constructs an NFA accepting the complement of the language of a given
     * NFA.
     * 
     * Converts the NFA into a DFA, then negates that DFA. Exponential state
     * blowup possible and common.
     * 
     * @param the
     *            NFA to construct the complement for.
     * 
     * @return a pair of integers denoting the index of start and end state of
     *         the complement NFA.
     */
    public IntPair complement(IntPair nfa) {

        int dfaStart = nfa.end() + 1;

        // fixme: only need epsilon closure of states reachable from nfa.start
        epsilonFill();

        Hashtable dfaStates = new Hashtable(numStates);
        Vector dfaVector = new Vector(numStates);

        int numDFAStates = 0;
        int currentDFAState = 0;

        StateSet currentState, newState;

        newState = epsilon[nfa.start()];
        dfaStates.put(newState, new Integer(numDFAStates));
        dfaVector.addElement(newState);

        currentDFAState = 0;

        while (currentDFAState <= numDFAStates) {

            currentState = (StateSet) dfaVector.elementAt(currentDFAState);

            for (char input = 0; input < numInput; input++) {
                newState = DFAEdge(currentState, input);

                if (newState.containsElements()) {

                    // Out.debug("DFAEdge for input "+(int)input+" and state set
                    // "+currentState+" is "+newState);

                    // Out.debug("Looking for state set "+newState);
                    Integer nextDFAState = (Integer) dfaStates.get(newState);

                    if (nextDFAState != null) {
                        // Out.debug("FOUND!");
                        addTransition(dfaStart + currentDFAState, input,
                                dfaStart + nextDFAState.intValue());
                    } else {
                        numDFAStates++;

                        dfaStates.put(newState, new Integer(numDFAStates));
                        dfaVector.addElement(newState);

                        addTransition(dfaStart + currentDFAState, input,
                                dfaStart + numDFAStates);
                    }
                }
            }

            currentDFAState++;
        }

        // We have a dfa accepting the positive regexp.

        // Now the complement:
        int start = dfaStart + numDFAStates + 1;
        int error = dfaStart + numDFAStates + 2;
        int end = dfaStart + numDFAStates + 3;

        addEpsilonTransition(start, dfaStart);

        for (int i = 0; i < numInput; i++)
            addTransition(error, i, error);

        addEpsilonTransition(error, end);

        for (int s = 0; s <= numDFAStates; s++) {
            currentState = (StateSet) dfaVector.elementAt(s);

            currentDFAState = dfaStart + s;

            // if it was not a final state, it is now in the complement
            if (!currentState.isElement(nfa.end()))
                addEpsilonTransition(currentDFAState, end);

            // all inputs not present (formerly leading to an implicit error)
            // now lead to an explicit (final) state accepting everything.
            for (int i = 0; i < numInput; i++)
                if (table[currentDFAState][i] == null)
                    addTransition(currentDFAState, i, error);
        }

        // eliminate transitions leading to dead states
        if (live == null || live.length < numStates) {
            live = new boolean[2 * numStates];
            visited = new boolean[2 * numStates];
        }

        _end = end;
        _dfaStates = dfaVector;
        _dfaStart = dfaStart;
        removeDead(dfaStart);

        return new IntPair(start, end);
    }

    // "global" data for use in method removeDead only:
    // live[s] == false <=> no final state can be reached from s
    private boolean[] live; // = new boolean [estSize];

    private boolean[] visited; // = new boolean [estSize];

    private int _end; // final state of original nfa for dfa (nfa coordinates)

    private Vector _dfaStates;

    private int _dfaStart; // in nfa coordinates

    private void removeDead(int start) {
        // Out.debug("removeDead ("+start+")");

        if (visited[start] || live[start])
            return;
        visited[start] = true;

        // Out.debug("not yet visited");

        if (closure(start).isElement(_end))
            live[start] = true;

        // Out.debug("is final :"+live[start]);

        for (int i = 0; i < numInput; i++) {
            StateSet nextState = closure(table[start][i]);
            StateSetEnumerator states = nextState.states();
            while (states.hasMoreElements()) {
                int next = states.nextElement();

                if (next != start) {
                    removeDead(next);

                    if (live[next])
                        live[start] = true;
                    else
                        table[start][i] = null;
                }
            }
        }

        StateSet nextState = closure(epsilon[start]);
        StateSetEnumerator states = nextState.states();
        while (states.hasMoreElements()) {
            int next = states.nextElement();

            if (next != start) {
                removeDead(next);

                if (live[next])
                    live[start] = true;
            }
        }
    }
    
    public int numInput() {
        return numInput;
    }

    public int numStates() {
        return numStates;
    }
    
    public IntPair newIntPair() {
        int start = numStates();
        int end = start + 1;

        ensureCapacity(end + 1);
        if ((end + 1) > numStates)
            numStates = end + 1;
        return new IntPair(start, end);
    }
}   
