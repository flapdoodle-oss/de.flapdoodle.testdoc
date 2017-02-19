package de.flapdoodle.testdoc;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import de.flapdoodle.testdoc.Stacktraces.Scope;

public class Recording implements TestRule {

	private final String templateName;
	private final String templateContent;
	private final String sourceCode;
	private final List<HasLine> lines=new ArrayList<>(); 

	public Recording(String templateName, String templateContent, String sourceCode) {
		this.templateName = Preconditions.checkNotNull(templateName, "template name is null");
		this.templateContent = Preconditions.checkNotNull(templateContent, "template content is null");
		this.sourceCode = Preconditions.checkNotNull(sourceCode, "sourceCode is null");
	}

	@Override
	public Statement apply(Statement base, Description description) {
		return new Statement() {
			
			@Override
			public void evaluate() throws Throwable {
				System.out.println("before "+base+" -> "+description);
				base.evaluate();
				System.out.println("after "+base+" -> "+description);
				
				String renderedTemplate = renderTemplate(templateName, templateContent, sourceCode, lines);
				writeResult(templateName, renderedTemplate);
			}
		};
	}
	
	protected static void writeResult(String templateName, String renderedTemplate) {
		System.out.println("should write "+templateName);
		System.out.println("---------------------------");
		System.out.println(renderedTemplate);
		System.out.println("---------------------------");
	}

	protected static String renderTemplate(String templateName, String templateContent, String sourceCode, List<HasLine> lines) {
		Map<String, List<HasLine>> usedFilenames = lines.stream()
			.collect(Collectors.groupingBy((HasLine l) -> l.line().fileName()));
		
		Preconditions.checkArgument(usedFilenames.size()==1, "more than one used filename: ",usedFilenames.keySet());
		
		Map<String, List<HasLine>> methodNames = lines.stream()
			.collect(Collectors.groupingBy((HasLine l) -> l.line().methodName()));
		
		Map<String, List<String>> recordingsByMethod = recordingsByMethod(methodNames, sourceCode);
		return "";
	}

	private static Map<String, List<String>> recordingsByMethod(Map<String, List<HasLine>> methodNames, String src) {
		Map<String, List<String>> ret=new LinkedHashMap<>();
		for (String key : methodNames.keySet()) {
			ret.put(key, recordings(methodNames.get(key), src));
		}
		return ret;
	}

	private static List<String> recordings(List<HasLine> list, String src) {
		List<String> ret=new ArrayList<>();
		
		List<HasLine> sortedLineNumbers = list.stream()
			.sorted((a,b) -> Integer.compare(a.line().lineNumber(),b.line().lineNumber()))
			.collect(Collectors.toList());
		
		//Preconditions.checkArgument(sortedLineNumbers.size() % 2 == 0, "recordedLineNumbers");
		return ret;
	}

	public void begin() {
		Line currentLine = Stacktraces.currentLine(Scope.CallerOfCaller);
		System.out.println("begin -> "+currentLine);
		lines.add(Start.of(currentLine));
	}
	
	public void end() {
		Line currentLine = Stacktraces.currentLine(Scope.CallerOfCaller);
		System.out.println("end -> "+currentLine);
		lines.add(End.of(currentLine));
	}
}