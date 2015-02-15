package org.xteam.cs.grm.build;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.xteam.cs.ast.model.AstModel;
import org.xteam.cs.grm.AnalysisMethod;
import org.xteam.cs.grm.ConflictResolverMethod;
import org.xteam.cs.grm.GrammarProperties;
import org.xteam.cs.grm.model.Grammar;
import org.xteam.cs.grm.model.Rule;
import org.xteam.cs.grm.model.Symbol;
import org.xteam.cs.grm.model.Terminal;
import org.xteam.cs.lex.build.LexerBuild;
import org.xteam.cs.lex.build.LexerMapping;
import org.xteam.cs.model.IProgressMonitor;
import org.xteam.cs.model.Project;
import org.xteam.cs.model.ProjectProperties;
import org.xteam.cs.runtime.IErrorReporter;
import org.xteam.cs.runtime.IParseTables;
import org.xteam.cs.runtime.Span;

public class ParserBuilder {
	
	private IErrorReporter reporter;

	public ParserBuilder(IErrorReporter reporter) {
		this.reporter = reporter;
	}

	public ParserBuild run(Grammar grammar,
			Project project, GrammarProperties grammarProperties,
			LexerBuild lexerBuild, AstModel astModel, IProgressMonitor monitor) {
		try {
			monitor.beginTask("Generating Parser", 4);
			monitor.subTask("Checking grammar");
			GrammarChecker checker = new GrammarChecker();
			if (! checker.check(grammar)) {
				return null;
			}
			monitor.worked(1);
			
			GrammarAnalysisModel model = new GrammarAnalysisModel(
					grammar, grammarProperties.lookahead);
			
			model.augment();
			
			monitor.subTask("Build Automaton");
			
			ILRAnalyser analyser = createAnalyser(model, grammarProperties.analysisMethod);
			AutomatonBuilder builder = new AutomatonBuilder(model, analyser);

			LRAutomaton automaton = builder.buildAutomaton();
			
			File logFolder = project.makeAbsolute(new File(project.getProperties().logFolder));
			logFolder.mkdirs();
			AutomatonWriter.writeAutomaton(automaton, grammar, logFolder, grammarProperties.logFormat);
			
			IConflictResolver resolver = createResolver(grammarProperties.conflictResolverMethod);
			for (LRState state : automaton.getStates()) {
				boolean hasConflict = false;
				for (Symbol sym : state.getTransitionSymbols()) {
					if (state.getTransition(sym).isConflict()) {
						hasConflict = true;
					}
				}
				if (hasConflict) {
					reporter.reportError(IErrorReporter.WARNING, new Span(-1, -1), "*** conflict in state " + state);
					/*System.out.println("*** conflict in state " + state);
					System.out.println();
					for (Symbol sym : state.getTransitionSymbols()) {
						Action action = state.getTransition(sym);
						System.out.print("\t" + sym.getName() + " -> ");
						if (action.isConflict()) {
							ConflictAction ca = (ConflictAction) action;
							Iterator<Action> k = ca.actions();
							System.out.print("\t");
							while (k.hasNext()) {
								System.out.print(k.next());
							}
							System.out.println();
						} else
							System.out.println("\t"+action);
					}
					System.out.println();*/
					resolver.resolve(state);
				}
			}
			monitor.worked(1);
			
			monitor.subTask("Generating Table");
			TableBuilder buidler = new TableBuilder();
			GrammarMapping mapping = makeMapping(model, lexerBuild);
			IParseTables table = buidler.run(model, automaton, mapping);
			monitor.worked(1);
			return new ParserBuild(model.grammar().getName(), mapping, lexerBuild, astModel, table);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	private GrammarMapping makeMapping(GrammarAnalysisModel model,
			LexerBuild lexerBuild) {
		GrammarMapping mapping = new GrammarMapping();
		Grammar grammar = model.grammar();
		int index = 0;
		if (lexerBuild == null) {
			for (Symbol sym : grammar.getSymbols()) {
				if (sym.isTerminal())
					mapping.addGrammarToken((Terminal)sym, index++);
			}
		} else {
			LexerMapping lexerMapping = lexerBuild.getMapping();
			List<Terminal> unfound = new ArrayList<Terminal>();
			for (Symbol sym : grammar.getSymbols()) {
				if (sym.isTerminal()) {
					int value = lexerMapping.getTokenNumber(sym.getName());
					if (value >= index)
						index = value + 1;
					if (value >= 0)
						mapping.addSymbol(sym, value);
					else
						unfound.add((Terminal) sym);
				}
			}
			for (Terminal sym : unfound) {
				mapping.addGrammarToken((Terminal)sym, index++);
			}
		}
		for (Symbol sym : grammar.getSymbols()) {
			if (!sym.isTerminal())
				mapping.addSymbol(sym, index++);
		}
		index = 0;
		for (Rule rule : grammar.getRules()) {
			mapping.addRule(rule, index++);
		}
		return mapping;
	}

	private ILRAnalyser createAnalyser(GrammarAnalysisModel model,
			AnalysisMethod analysisMethod) {
		if (analysisMethod == AnalysisMethod.LR0)
			return new LR0Analyzer(model);
		if (analysisMethod == AnalysisMethod.SLR)
			return new SLRAnalyzer(model);
		if (analysisMethod == AnalysisMethod.LALR)
			return new LALRAnalyzer(model);
		if (analysisMethod == AnalysisMethod.LR)
			return new LRAnalyzer(model);
		
		// default case should never happen
		return new SLRAnalyzer(model);
	}
	
	private IConflictResolver createResolver(ConflictResolverMethod method) {
		if (method == ConflictResolverMethod.NoResolution)
			return new NoResolver();
		// default case should never happen
		return new SimpleResolver();
	}

}
