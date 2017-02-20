package de.flapdoodle.testdoc;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import de.flapdoodle.testdoc.Stacktraces.Scope;

public class Recording implements TestRule {

	private final String templateName;
	private final String templateContent;
	private final List<String> linesOfCode;
	private final List<HasLine> lines=new ArrayList<>(); 

	public Recording(String templateName, String templateContent, List<String> linesOfCode) {
		this.templateName = Preconditions.checkNotNull(templateName, "template name is null");
		this.templateContent = Preconditions.checkNotNull(templateContent, "template content is null");
		this.linesOfCode = new ArrayList<>(Preconditions.checkNotNull(linesOfCode, "linesOfCode is null"));
	}

	@Override
	public Statement apply(Statement base, Description description) {
		return new Statement() {
			
			@Override
			public void evaluate() throws Throwable {
//				System.out.println("before "+base+" -> "+description);
				base.evaluate();
//				System.out.println("after "+base+" -> "+description);
				
				String renderedTemplate = renderTemplate(templateName, templateContent, linesOfCode, lines);
				writeResult(templateName, renderedTemplate);
			}
		};
	}
	
	protected static void writeResult(String templateName, String renderedTemplate) {
		System.getProperties().forEach((key, val) -> {
			System.out.println(key+"="+val);
		});
		System.out.println("---------------------------");
		System.out.println("should write "+templateName);
		System.out.println("---------------------------");
		System.out.println(renderedTemplate);
		System.out.println("---------------------------");
	}

	protected static String renderTemplate(String templateName, String templateContent, List<String> linesOfCode, List<HasLine> lines) {
		Map<String, List<HasLine>> usedFilenames = lines.stream()
			.collect(Collectors.groupingBy((HasLine l) -> l.line().fileName()));
		
		Preconditions.checkArgument(usedFilenames.size()==1, "more than one used filename: ",usedFilenames.keySet());
		
		Map<String, List<HasLine>> methodNames = lines.stream()
			.collect(Collectors.groupingBy((HasLine l) -> l.line().methodName()));
		
		Map<String, List<String>> recordingsByMethod = recordingsByMethod(methodNames, linesOfCode);
		
		return render(templateContent, recordingsByMethod);
	}

	private static String render(String templateContent, Map<String, List<String>> recordingsByMethod) {
		String rendered=templateContent;
		for (String method : recordingsByMethod.keySet()) {
			String formatedBlocks=recordingsByMethod.get(method).stream().collect(Collectors.joining("...\n"));
			rendered=rendered.replace("${"+method+"}", formatedBlocks);
		};
		return rendered;
	}

	private static Map<String, List<String>> recordingsByMethod(Map<String, List<HasLine>> methodNames, List<String> linesOfCode) {
		Map<String, List<String>> ret=new LinkedHashMap<>();
		for (String key : methodNames.keySet()) {
			ret.put(key, recordings(methodNames.get(key), linesOfCode));
		}
		return ret;
	}

	private static List<String> recordings(List<HasLine> list, List<String> linesOfCode) {
		List<String> ret=new ArrayList<>();
		
		List<HasLine> sortedLineNumbers = list.stream()
			.sorted((a,b) -> Integer.compare(a.line().lineNumber(),b.line().lineNumber()))
			.collect(Collectors.toList());
		
		Preconditions.checkArgument(sortedLineNumbers.size() % 2 == 0, "odd number of markers: %s", sortedLineNumbers);
		
		Start lastStart=null;
		for (HasLine line : sortedLineNumbers) {
			if (line instanceof Start) {
				Preconditions.checkArgument(lastStart==null, "start after start: %s - %s",lastStart, line);
				lastStart=(Start) line;
			} else {
				if (line instanceof End) {
					Preconditions.checkNotNull(lastStart, "end but no start: %s", line);
					ret.add(blockOf(linesOfCode, lastStart.line().lineNumber(), line.line().lineNumber()));
					lastStart=null;
				} else {
					Preconditions.checkArgument(false, "hmm... should not happen: %s",line);
				}
			}
		}
		return ret;
	}

	private static String blockOf(List<String> linesOfCode, int startLineNumber, int endLineNumber) {
		return shiftLeft(linesOfCode.subList(startLineNumber+1, endLineNumber-1))
				.stream()
				.collect(Collectors.joining("\n"));
	}

	private static Pattern WHITESPACES=Pattern.compile("\\s*");
	
	private static List<String> shiftLeft(List<String> subList) {
		
		Optional<Integer> minWhitespaces = subList.stream()
			.filter(line -> !line.trim().isEmpty())
			.map(line -> WHITESPACES.matcher(line))
			.filter(matcher -> matcher.find())
			.map(matcher -> matcher.end())
			.min(Comparator.naturalOrder());
		
		if (minWhitespaces.isPresent()) {
			int offset=minWhitespaces.get();
			return subList.stream()
					.map(line -> line.length()<offset ? "" : line.substring(offset))
					.collect(Collectors.toList());
		}
		
		return subList;
	}

	public void begin() {
		Line currentLine = Stacktraces.currentLine(Scope.CallerOfCaller);
//		System.out.println("begin -> "+currentLine);
		lines.add(Start.of(currentLine));
	}
	
	public void end() {
		Line currentLine = Stacktraces.currentLine(Scope.CallerOfCaller);
//		System.out.println("end -> "+currentLine);
		lines.add(End.of(currentLine));
	}
}