/**
 * Copyright (C) 2016
 *   Michael Mosmann <michael@mosmann.de>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.flapdoodle.testdoc;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import de.flapdoodle.testdoc.Stacktraces.Scope;

public class Recording implements TestRule {

	private static final String DEST_DIR_PROPERTY = "de.flapdoodle.testdoc.destination";
	private final String templateName;
	private final String templateContent;
	private final List<String> testSourceCode;
	private final List<HasLine> lines=new ArrayList<>(); 
	private final Map<String, String> classes=new LinkedHashMap<>();
	private final Map<String, String> output=new LinkedHashMap<>();
	private final TabSize tabSize;

	public Recording(String templateName, String templateContent, List<String> testSourceCode, TabSize tabSize) {
		this.tabSize = tabSize;
		this.templateName = Preconditions.checkNotNull(templateName, "template name is null");
		this.templateContent = Preconditions.checkNotNull(templateContent, "template content is null");
		this.testSourceCode = new ArrayList<>(Preconditions.checkNotNull(testSourceCode, "linesOfCode is null"));
	}
	
	public Recording sourceCodeOf(String label, Class<?> clazz, Includes ...includeOptions) {
		Optional<List<String>> sourceCode = Resources.sourceCodeOf(clazz, tabSize, includeOptions);
		if (!sourceCode.isPresent()) {
			throw new IllegalArgumentException("could not find sourceCode of "+clazz);
		}
		String old = classes.put(label, Resources.joinedWithNewLine(sourceCode.get()));
		Preconditions.checkArgument(old==null, "sourceCodeOf with label %s was already set to %s",label,old);
		return this;
	}

	@Override
	public Statement apply(Statement base, Description description) {
		return new Statement() {
			
			@Override
			public void evaluate() throws Throwable {
//				System.out.println("before "+base+" -> "+description);
				base.evaluate();
//				System.out.println("after "+base+" -> "+description);
				
				String renderedTemplate = renderTemplate(templateName, templateContent, testSourceCode, lines, classes, output);
				writeResult(templateName, renderedTemplate);
			}
		};
	}
	
	protected static void writeResult(String templateName, String renderedTemplate) {
		String destination = System.getProperty(DEST_DIR_PROPERTY);
		if (destination!=null) {
			Path output = Paths.get(destination).resolve(templateName);
			try {
				Files.write(output, renderedTemplate.getBytes(Charset.forName("UTF-8")), StandardOpenOption.WRITE,StandardOpenOption.CREATE);
			} catch (IOException iox) {
				throw new RuntimeException("could not write "+output,iox);
			}
		} else {
			System.out.println(DEST_DIR_PROPERTY+" not set");
			System.out.println("---------------------------");
			System.out.println("should write "+templateName);
			System.out.println("---------------------------");
			System.out.println(renderedTemplate);
			System.out.println("---------------------------");
		}
//		System.getProperties().forEach((key, val) -> {
//			System.out.println(key+"="+val);
//		});
	}

	protected static String renderTemplate(String templateName, String templateContent, List<String> linesOfCode, List<HasLine> lines, Map<String, String> classes, Map<String, String> output) {
		Map<String, List<HasLine>> usedFilenames = lines.stream()
			.collect(Collectors.groupingBy((HasLine l) -> l.line().fileName()));
		
		Preconditions.checkArgument(usedFilenames.size()<=1, "more than one used filename: ",usedFilenames.keySet());
		
		Map<String, List<HasLine>> methodNames = lines.stream()
			.collect(Collectors.groupingBy((HasLine l) -> l.line().methodName()));
		
		Map<String, List<String>> recordingsByMethod = recordingsByMethod(methodNames, linesOfCode);
		
		return render(templateContent, recordingsByMethod, classes, output);
	}

	private static String render(String templateContent, Map<String, List<String>> recordingsByMethod, Map<String, String> classes, Map<String, String> output) {
		String rendered=templateContent;
		Set<String> usedLabels=new LinkedHashSet<>();
		usedLabels.addAll(recordingsByMethod.keySet());
		
		for (String method : recordingsByMethod.keySet()) {
			List<String> blocks = recordingsByMethod.get(method);
			String formatedBlocks=formatBlocks(blocks);
//			System.out.println(method+"=\n-------"+formatedBlocks+"\n-------");
			rendered=rendered.replace("${"+method+"}", formatedBlocks);
			AtomicInteger counter=new AtomicInteger(0);
			for (String block : blocks) {
				String blockLabel = method+"."+counter.incrementAndGet();
				rendered=rendered.replace("${"+blockLabel+"}", block);
				usedLabels.add(blockLabel);
			}
//			System.out.println("~~~~~~~~~~~~~");
//			System.out.println(rendered);
//			System.out.println("~~~~~~~~~~~~~");
		};
		for (String label : classes.keySet()) {
			Preconditions.checkArgument(!usedLabels.contains(label), "rendering failed, %s already used: %s", label, usedLabels);
			String content=classes.get(label);
			rendered=rendered.replace("${"+label+"}", content);
		}
		for (String label : output.keySet()) {
			Preconditions.checkArgument(!usedLabels.contains(label), "rendering failed, %s already used: %s", label, usedLabels);
			String content=output.get(label);
			rendered=rendered.replace("${"+label+"}", content);
		}
		return rendered;
	}

	private static String formatBlocks(List<String> blocks) {
		return blocks.stream().collect(Collectors.joining("\n...\n\n"));
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
		
//		System.out.println("sorted: "+sortedLineNumbers);
		
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
		
//		System.out.println("ret: "+ret);
		
		return ret;
	}

	private static String blockOf(List<String> linesOfCode, int startLineNumber, int endLineNumber) {
		return shiftLeft(linesOfCode.subList(startLineNumber, endLineNumber-1))
				.stream()
				.collect(Collectors.joining("\n"));
	}

	private static Pattern WHITESPACES=Pattern.compile("\\s*");
	
	private static List<String> shiftLeft(List<String> subList) {
//		System.out.println("shiftLeft: "+subList);
		
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

	public void include(Class<?> clazz, Includes ...includeOptions) {
		Line currentLine = Stacktraces.currentLine(Scope.CallerOfCaller);
		String label=currentLine.methodName()+"."+clazz.getSimpleName();
		sourceCodeOf(label, clazz, includeOptions);
	}
	
	public void output(String label, String content) {
		Line currentLine = Stacktraces.currentLine(Scope.CallerOfCaller);
		String scopedLabel=currentLine.methodName()+"."+label;
		String old = output.put(scopedLabel, content);
		Preconditions.checkArgument(old==null, "%s already set to %s",label, old);
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