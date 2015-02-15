package org.xteam.cs.lex.build;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.List;
import java.util.Properties;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.xteam.cs.generator.AbstractBuilder;
import org.xteam.cs.lex.LexParser;
import org.xteam.cs.lex.LexSemantic;
import org.xteam.cs.lex.LexerFile;
import org.xteam.cs.lex.LexerProperties;
import org.xteam.cs.lex.ast.LexerFileAst;
import org.xteam.cs.lex.model.LexerModel;
import org.xteam.cs.lex.model.LexicalState;
import org.xteam.cs.model.BuilderErrorReporter;
import org.xteam.cs.model.ErrorMark;
import org.xteam.cs.model.ParseError;
import org.xteam.cs.model.ProjectProperties;
import org.xteam.cs.model.SubProgressMonitor;

public class LexerBuilder extends AbstractBuilder {

	@Override
	public void build() {
		for (LexerFile file : project.getResources(LexerFile.class)) {
			build(file);
		}
	}

	private void build(LexerFile file) {
		file.clearMark(ErrorMark.class);
		LexParser parser = new LexParser();
		BuilderErrorReporter reporter = new BuilderErrorReporter(file);
		parser.setErrorReporter(reporter);
		parser.setInput(new StringReader(file.getContents()));
		try {
			file.setBuildLexer(null);
			monitor.beginTask("Compiling " + file, 3);
			monitor.subTask("Parsing");
			LexerFileAst ast = parser.parse();
			monitor.worked(1);
			monitor.subTask("Analysing");
			LexSemantic semantic = new LexSemantic(reporter);
			LexerModel model = semantic.analyse(ast);
			monitor.worked(1);
			if (reporter.hasErrors())
				return;
			LexerTableGenerator generator = new LexerTableGenerator();
			LexerBuild build = generator.run(model, (LexerProperties)file.getProperties(),
					new SubProgressMonitor(monitor));
			monitor.done();
			file.setBuildLexer(build);
		} catch (ParseError e) {
		} catch (IOException e) {
		}
	}

	@Override
	public void generate() {
		for (LexerFile file : project.getResources(LexerFile.class)) {
			generate(file);
		}
	}

	private void generate(LexerFile file) {
		LexerBuild build = file.getBuild();
		ProjectProperties projectProperties = (ProjectProperties)project.getProperties();
		LexerProperties lexerProperties = (LexerProperties) file.getProperties();
		if (build == null)
			return;
		
		int amountOfWork = 4;
		boolean generateConvertCode = false;
		if (build.getMapping().getConvertCodes().size() > 0) {
			amountOfWork++;
			generateConvertCode = true;
		}
		File currentFile = makeFilename(projectProperties, lexerProperties.lexerPackage, ".", false);
		currentFile.mkdirs();
		
		currentFile = makeFilename(projectProperties, lexerProperties.lexerPackage,
				build.getName() + ".ltb", true);
		currentFile.getParentFile().mkdirs();
		monitor.beginTask("Generating for " + file, amountOfWork);
		
		monitor.subTask("Generating " + currentFile.getName());
		LexerTableFileEmiter emiter = new LexerTableFileEmiter();
		emiter.emit(build.getTables(), build.getMapping().getStateCount(), currentFile);
		monitor.worked(1);
		
		try {
			Properties prop = new Properties();
			prop.setProperty("resource.loader", "defaultLoader");
			prop.setProperty("defaultLoader.resource.loader.class",
							"org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
			Velocity.init(prop);
			
			VelocityContext context = new VelocityContext();
			context.put("lexGen", new LexGenModel(build, projectProperties, lexerProperties));
			
			currentFile = makeFilename(projectProperties, lexerProperties.lexerPackage,
					build.getName() + "LexerFactory.java", false);
			generate(context, currentFile, "/resources/templates/lex/lex-factory.vm");
			
			currentFile = makeFilename(projectProperties, lexerProperties.lexerPackage,
					build.getName() + "SyntaxHelper.java", false);
			generate(context, currentFile, "/resources/templates/lex/lex-syntax-helper.vm");
			
			currentFile = makeFilename(projectProperties, lexerProperties.lexerPackage,
					"I" + build.getName() + "Tokens.java", false);
			generate(context, currentFile, "/resources/templates/lex/lex-tokens.vm");
			
			currentFile = makeFilename(projectProperties, lexerProperties.lexerPackage,
					build.getName() + "StateNames.java", false);
			generate(context, currentFile, "/resources/templates/lex/lex-state-names.vm");
			
			if (generateConvertCode) {
				currentFile = makeFilename(projectProperties, lexerProperties.lexerPackage,
						build.getName() + "ConvertCodes.java", false);
				generate(context, currentFile, "/resources/templates/lex/lex-convert-code.vm");
			}
			
		} catch (Exception e) {
		}
	}
	
	public static class LexGenModel {

		private ProjectProperties projectProperties;
		private LexerProperties lexerProperties;
		private LexerBuild model;

		public LexGenModel(LexerBuild build,
				ProjectProperties projectProperties,
				LexerProperties lexerProperties) {
			this.model = build;
			this.projectProperties = projectProperties;
			this.lexerProperties = lexerProperties;
		}
		
		public String getPackage() {
			return projectProperties.mainPackage + "." + lexerProperties.lexerPackage;
		}
		
		public LexerBuild getModel() {
			return model;
		}
		
		public List<Integer> getTokens() {
			return model.getMapping().getTokenValues();
		}
		
		public String getTokenName(int token) {
			return model.getMapping().getToken(token).replace('$', '_');
		}
		
		public String getTokenDisplayName(int t) {
			return model.getMapping().getTokenName(t).replace("\n", "\\n").replace("\"", "\\\"");
		}
		
		public List<String> getConvertCodes() {
			return model.getMapping().getConvertCodes();
		}
		
		public int getConvertCodeValue(String code) {
			return model.getMapping().getConvertCode(code);
		}
		
		public int getEofValue() {
			return model.getMapping().getTokenNumber("$EOF$");
		}
		
		public List<LexicalState> getStates() {
			return model.getMapping().getStates();
		}
		
		public int getStateValue(LexicalState state) {
			return model.getMapping().getStateNumber(state);
		}
		
	}
	
}
