package com.example.mini;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.xteam.cs.runtime.DefaultErrorReporter;
import org.xteam.cs.runtime.IErrorReporter;
import org.xteam.cs.runtime.ILexer;
import org.xteam.cs.runtime.IStatedLexer;
import org.xteam.cs.runtime.LRParser;

import com.example.mini.ast.Function;
import com.example.mini.ast.Ident;
import com.example.mini.ast.MiniFile;
import com.example.mini.cmodel.CExpr;
import com.example.mini.cmodel.CFunction;
import com.example.mini.cmodel.MiniType;
import com.example.mini.lexer.MiniLexerFactory;
import com.example.mini.lexer.MiniSyntaxHelper;
import com.example.mini.parser.MiniParserFactory;
import com.example.mini.parser.MiniRuleReducer;

public class MiniCompiler {
	public static void main(String[] args) throws IOException {
		String filename = args[0];
		new MiniCompiler().compile(filename);
	}

	private HashMap<String, CFunction> cfuntions;
	private HashMap<String, CFunction> primitives;
	private HashMap<CFunction, Function> funcMap;
	private DefaultErrorReporter reporter;
	
	public void compile(String filename) throws IOException {
		reporter = new DefaultErrorReporter(System.out);
		ILexer lexer = MiniLexerFactory.createLexer(null);
		lexer.setErrorReporter(reporter);
		lexer.setInput(new FileReader(filename));
		LRParser parser = MiniParserFactory.createParser(lexer,
				new MiniRuleReducer((IStatedLexer)lexer),
				new MiniSyntaxHelper(), reporter);
		try {
			MiniFile file = (MiniFile) parser.parse();
			if (analyse(file) && ! reporter.hasErrors()) {
				generate(filename);
			}
			if (reporter.hasErrors())
				reporter.printDiagnostic(filename);
		} catch (IOException e) {
			reporter.printDiagnostic(filename);
		}
	}
	
	private boolean analyse(MiniFile file) {
		primitives = new HashMap<String, CFunction>();
		primitives.put("print", new Primitive("print", null, MiniType.STRING));
		primitives.put("i_to_s", new Primitive("i_to_s", MiniType.STRING, MiniType.INTEGER));
		primitives.put("str_concat", new Primitive("str_concat", MiniType.STRING, MiniType.STRING, MiniType.STRING));
		
		cfuntions = new HashMap<String, CFunction>();
		funcMap = new HashMap<CFunction, Function>();
		for (Function func : file.getFunctions()) {
			String name = func.getName().getName();
			if (cfuntions.containsKey(name)) {
				reporter.reportError(IErrorReporter.ERROR, func.getName().span(),
						"function '"+name+"' already defined");
			} else {
				CFunction cfunc = new CFunction(name);
				funcMap.put(cfunc, func);
				cfuntions.put(name, cfunc);
			}
		}
		if (! cfuntions.containsKey("main")) {
			System.out.println("error: must contain 'main' function");
			return false;
		}
		CFunction mainFunc = cfuntions.get("main");
		inferFrom(mainFunc, null);
		for (CFunction cfunc : cfuntions.values()) {
			if (! cfunc.isUsed()) {
				Function func = funcMap.get(cfunc);
				reporter.reportError(IErrorReporter.WARNING, func.getName().span(), "unused function");
				cfuntions.remove(cfunc);
			}
		}
		return true; 
	}

	public boolean inferFrom(CFunction cfunc, List<CExpr> args) {
		cfunc.markUsed();
		Function func = funcMap.get(cfunc);
		if (args == null) {
			for (Ident arg : func.getArguments()) {
				cfunc.addArgument(arg.getName(), MiniType.STRING);
			}
		} else {
			if (args.size() != func.getArguments().size())
				return false;
			int index = 0;
			for (Ident arg : func.getArguments()) {
				cfunc.addArgument(arg.getName(), args.get(index).getType());
				++index;
			}
		}
		cfunc.markResolved();
		func.visit(new TypeChecker(this, cfunc, reporter));
		return true;
	}

	public CFunction getFunction(String name) {
		return cfuntions.get(name);
	}

	public CFunction getPrimitive(String name) {
		return primitives.get(name);
	}
	
	private void generate(String filename) {
		Properties prop = new Properties();
		prop.setProperty("resource.loader", "defaultLoader");
		prop.setProperty("defaultLoader.resource.loader.class",
				"org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
		prop.setProperty("foreach.provide.scope.control", "true");
		try {
			Velocity.init(prop);

			File currentFile = makeFilename(filename);

			VelocityContext context = new VelocityContext();
			context.put("cGen", new CGenModel(cfuntions, primitives));
			
			Template nodeTemplate = Velocity
					.getTemplate("/resources/c-code.vm");
			BufferedWriter writer = new BufferedWriter(new FileWriter(
					currentFile));
			nodeTemplate.merge(context, writer);
			writer.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private File makeFilename(String filename) {
		String base = filename;
		int pos = filename.lastIndexOf('.');
		if (pos >= 0)
			base = filename.substring(0, pos);
		return new File(base + ".c");
	}

}
