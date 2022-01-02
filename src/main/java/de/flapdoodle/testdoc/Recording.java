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
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;

import de.flapdoodle.checks.Preconditions;
import de.flapdoodle.testdoc.ImmutableReplacements.Builder;
import de.flapdoodle.testdoc.Stacktraces.Scope;
import org.junit.jupiter.api.extension.ExtensionContext;

public class Recording implements AfterAllCallback {

	private static final String DEST_DIR_PROPERTY = "de.flapdoodle.testdoc.destination";
	private static final ThreadLocal<BiConsumer<String, String>> templateConsumer=new ThreadLocal<>();
	
	private final String templateName;
	private final String templateContent;
	private final List<String> testSourceCode;
	private final List<HasLine> lines=new ArrayList<>(); 
	private final Map<String, String> classes=new LinkedHashMap<>();
	private final Map<String, String> resources=new LinkedHashMap<>();
	private final Map<String, String> output=new LinkedHashMap<>();
	private final TabSize tabSize;
	private Optional<BiFunction<String, Set<String>, String>> replacementNotFoundFallback=Optional.empty();
	
	public Recording(String templateName, String templateContent, List<String> testSourceCode, TabSize tabSize) {
		this.tabSize = tabSize;
		this.templateName = Preconditions.checkNotNull(templateName, "template name is null");
		this.templateContent = Preconditions.checkNotNull(templateContent, "template content is null");
		this.testSourceCode = new ArrayList<>(Preconditions.checkNotNull(testSourceCode, "linesOfCode is null"));
	}
	
	public Recording sourceCodeOf(String label, Class<?> clazz, Includes ...includeOptions) {
		Optional<List<String>> sourceCode = Resources.sourceCodeOf(clazz, tabSize, includeOptions);
		Preconditions.checkArgument(sourceCode.isPresent(), "could not find sourceCode of %s",clazz);
		String old = classes.put(label, Resources.joinedWithNewLine(sourceCode.get()));
		Preconditions.checkArgument(old==null, "sourceCodeOf with label %s was already set to %s",label,old);
		return this;
	}

	public Recording resource(String label, Class<?> clazz, String resourceName, ResourceFilter ... filters) {
		Optional<String> resource = Resources.resource(clazz, resourceName);
		Preconditions.checkArgument(resource.isPresent(), "could not find resource of %s:%s",clazz,resourceName);
		String old = resources.put(label, resource.map(ResourceFilter.join(filters)).get());
		Preconditions.checkArgument(old==null, "resource with label %s was already set to %s",label,old);
		return this;
	}
	
	public Recording replacementNotFoundFallback(BiFunction<String, Set<String>, String> fallback) {
		Preconditions.checkArgument(!replacementNotFoundFallback.isPresent(), "already set to: %s", replacementNotFoundFallback);
		this.replacementNotFoundFallback = Optional.of(fallback);
		return this;
	}

	@Override public void afterAll(ExtensionContext extensionContext) throws Exception {
		String renderedTemplate = renderTemplate(templateName, templateContent, testSourceCode, lines, classes, resources, output, replacementNotFoundFallback);
		writeResult(templateName, renderedTemplate);
	}

	protected static void writeResult(String templateName, String renderedTemplate) {
		if (templateConsumer.get()!=null) {
			templateConsumer.get().accept(templateName, renderedTemplate);
		} else {
			String destination = System.getProperty(DEST_DIR_PROPERTY);
			if (destination!=null) {
				Path output = Paths.get(destination).resolve(templateName);
				try {
					Files.write(output, renderedTemplate.getBytes(Charset.forName("UTF-8")), StandardOpenOption.WRITE,StandardOpenOption.CREATE,StandardOpenOption.TRUNCATE_EXISTING);
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
		}
	}

	protected static String renderTemplate(String templateName, String templateContent, List<String> linesOfCode, List<HasLine> lines, Map<String, String> classes, Map<String, String> resources, Map<String, String> output, Optional<BiFunction<String, Set<String>, String>> replacementNotFoundFallback) {
		Map<String, List<HasLine>> usedFilenames = lines.stream()
			.collect(Collectors.groupingBy((HasLine l) -> l.line().fileName()));
		
		Preconditions.checkArgument(usedFilenames.size()<=1, "more than one used filename: ",usedFilenames.keySet());
		
		Map<String, List<HasLine>> methodNames = lines.stream()
			.collect(Collectors.groupingBy((HasLine l) -> l.line().methodName()));
		
		Map<String, List<String>> recordingsByMethod = recordingsByMethod(methodNames, linesOfCode);
		
		return render(templateContent, recordingsByMethod, classes, resources, output, replacementNotFoundFallback);
	}

	private static String render(String templateContent, Map<String, List<String>> recordingsByMethod, Map<String, String> classes, Map<String, String> resources, Map<String, String> output, Optional<BiFunction<String, Set<String>, String>> replacementNotFoundFallback) {
		Map<String, String> joinedMap = merge(recordingsByMethod, classes, resources, output);
		return replacementNotFoundFallback.isPresent() 
				? Template.render(templateContent, joinedMap, replacementNotFoundFallback.get()) 
				: Template.render(templateContent, joinedMap);
	}

	private static Map<String, String> merge(Map<String, List<String>> recordingsByMethod, Map<String, String> classes, Map<String, String> resources, Map<String, String> output) {
		Set<String> usedKeys=new LinkedHashSet<>();
		
		Builder builder = Template.Replacements.builder();
		recordingsByMethod.forEach((method, blocks) -> {
			builder.putReplacement(method, formatBlocks(blocks));
			usedKeys.add(method);
			
			AtomicInteger counter=new AtomicInteger(0);
			for (String block : blocks) {
				String blockLabel = method+"."+counter.incrementAndGet();
				builder.putReplacement(blockLabel, block);
				usedKeys.add(blockLabel);
			}
		});
		
		Function<String, BiConsumer<? super String, ? super String>> checkAndAddToBuilderFactory=scope -> (key, value) -> {
			Preconditions.checkArgument(!usedKeys.contains(key), scope+": already set: %s",key);
			builder.putReplacement(key, value);
			usedKeys.add(key);
		}; 
		
		classes.forEach(checkAndAddToBuilderFactory.apply("classes"));
		resources.forEach(checkAndAddToBuilderFactory.apply("resources"));
		output.forEach(checkAndAddToBuilderFactory.apply("output"));
		return builder.build().replacement();
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
	
	public void resource(Class<?> clazz, String resourceName, ResourceFilter... filters) {
		Line currentLine = Stacktraces.currentLine(Scope.CallerOfCaller);
		String label=currentLine.methodName()+"."+clazz.getSimpleName()+":"+resourceName;
		resource(label, clazz, resourceName, filters);
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
	
	private static BiConsumer<String, String> setTemplateConsumerForInternalUse(BiConsumer<String, String> consumer) {
		BiConsumer<String, String> old = templateConsumer.get();
		templateConsumer.set(consumer);
		return old;
	}
	
	protected static Consumer<Runnable> runWithTemplateConsumer(BiConsumer<String, String> consumer) {
		return runnable -> {
			BiConsumer<String, String> old = setTemplateConsumerForInternalUse(consumer);
			try {
				runnable.run();
			} finally {
				setTemplateConsumerForInternalUse(old);
			}
		};
	}
}