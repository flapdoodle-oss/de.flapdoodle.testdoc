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

import de.flapdoodle.checks.Preconditions;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public abstract class Renderer {

	private static Pattern WHITESPACES=Pattern.compile("\\s*");

	protected static String renderTemplate(Recordings recordings) {
		Map<String, List<HasLine>> usedFilenames = recordings.lines().stream()
			.collect(Collectors.groupingBy((HasLine l) -> l.line().fileName()));

		Preconditions.checkArgument(usedFilenames.size()<=1, "more than one used filename: ",usedFilenames.keySet());

		Map<String, List<HasLine>> methodNames = recordings.lines().stream()
			.collect(Collectors.groupingBy((HasLine l) -> l.line().methodName()));

		Map<String, List<String>> recordingsByMethod = recordingsByMethod(methodNames, recordings.linesOfCode());

		return render(recordings, recordingsByMethod);
	}

	private static String render(Recordings recordings, Map<String, List<String>> recordingsByMethod) {
		Map<String, String> joinedMap = merge(recordings, recordingsByMethod);

		String templateContent = recordings.templateReference().readContent()
			.orElseGet(() -> templateFrom(recordings.templateReference(), joinedMap));

		return recordings.replacementNotFoundFallback().isPresent()
				? Template.render(templateContent, joinedMap, recordings.replacementNotFoundFallback().get())
				: Template.render(templateContent, joinedMap);
	}
	private static String templateFrom(TemplateReference templateReference, Map<String, String> joinedMap) {
		StringBuilder sb=new StringBuilder();
		sb.append("# document from generated template\n\n");
		sb.append("as you did not create a matching template with the name `")
			.append(templateReference.templateName()).append("`\n")
			.append("in the same package as `")
			.append(templateReference.clazz().toString()).append("`\n")
			.append("the content of this file is generated from the recordings of your test class.")
			.append("\n\n")
			.append("In your test following parts were recorded:\n\n");

		joinedMap.forEach((key,value) -> {
			sb.append("* `").append(key).append("`\n");
		});

		sb.append("\n")
			.append("to insert the content of a part into the generated document you must embed a name\n")
			.append("from this list between a starting `${` and `}`\n");
		
		return sb.toString();
	}

	private static Map<String, String> merge(Recordings recordings, Map<String, List<String>> recordingsByMethod) {
		Set<String> usedKeys=new LinkedHashSet<>();

		ImmutableReplacements.Builder builder = Template.Replacements.builder();
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

		recordings.classes().forEach(checkAndAddToBuilderFactory.apply("classes"));
		recordings.resources().forEach(checkAndAddToBuilderFactory.apply("resources"));
		recordings.output().forEach(checkAndAddToBuilderFactory.apply("output"));
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
}
