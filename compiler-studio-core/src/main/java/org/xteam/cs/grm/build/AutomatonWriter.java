package org.xteam.cs.grm.build;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.Iterator;

import org.xteam.cs.grm.LogFormat;
import org.xteam.cs.grm.model.Grammar;
import org.xteam.cs.grm.model.Symbol;

/**
 * Write the parsing automaton to HTML, DOT and Text.
 * 
 * @author ludo
 *
 */
public class AutomatonWriter {

	private LRAutomaton automaton;
	private Grammar grammar;
	private File logFolder;

	public AutomatonWriter(LRAutomaton automaton, Grammar grammar, File logFolder) {
		this.automaton = automaton;
		this.grammar = grammar;
		this.logFolder = logFolder;
	}

	public static void writeAutomaton(LRAutomaton automaton, Grammar grammar, File logFolder, LogFormat logFormat) throws FileNotFoundException {
		AutomatonWriter writer = new AutomatonWriter(automaton, grammar, logFolder);
		if (logFormat == LogFormat.TEXT) {
			writer.writeText();
		} else if (logFormat == LogFormat.DOT) {
			writer.writeDot();
		} else if (logFormat == LogFormat.HTML) {
			writer.writeHTML();
		}
	}

	private void writeDot() throws FileNotFoundException {
		automaton.output(new PrintStream(new FileOutputStream(new File(logFolder, "auto.dot"))));
	}
	
	private void writeText() throws FileNotFoundException {
		PrintStream stream = new PrintStream(new FileOutputStream(new File(logFolder, "auto.txt")));
		for (LRState state : automaton.getStates()) {
			stream.println("===== state "+state.number() +" =====");
			for (int j = 0; j < state.itemCount(); ++j) {
				stream.println("\t"+state.itemAt(j));
			}
			stream.println();
			for (Symbol sym : state.getTransitionSymbols()) {
				Action action = state.getTransition(sym);
				stream.print("\t" + sym.getName() + " -> ");
				if (action.isConflict()) {
					ConflictAction ca = (ConflictAction) action;
					Iterator<Action> k = ca.actions();
					while (k.hasNext()) {
						stream.println("\t"+k.next());
					}
				} else
					stream.println("\t"+action);
			}
			stream.println();
		}
		stream.close();
	}

	private void writeHTML() throws FileNotFoundException {
		PrintStream html = new PrintStream(new FileOutputStream(new File(logFolder, "auto.html")));
		
		html.println("<html>");
		html.println("<body>");
		//html.println("<img src=\"auto.png\"/>");
		html.println("<table border=\"1\">");
		writeTableHeader(html);
		writeTableBody(html);
		html.println("</table>");
		html.println("</body>");
		html.println("</html>");
	}

	private void writeTableHeader(PrintStream html) {
		html.println("<tr>");
		html.print("<td></td>");
		for (Symbol sym : grammar.getSymbols()) {
			html.print("<td>"+sym.getName()+"</td>");
		}
		html.println("</tr>");
	}
	
	private void writeTableBody(PrintStream html) {
		for (int i = 0; i < automaton.stateCount(); ++i) {
			LRState state = automaton.at(i);
			html.println("<tr>");
			html.print("<td>"+state.number()+"</td>");
			for (Symbol sym : grammar.getSymbols()) {
				Action action = state.getTransition(sym);
				if (action != null) {
					if (action.isConflict())
						html.print("<td bgcolor=\"red\">"+action+"</td>");
					else
						html.print("<td>"+action+"</td>");
				} else
					html.print("<td>&nbsp;</td>");
			}
			html.println("</tr>");
		}
	}

}
