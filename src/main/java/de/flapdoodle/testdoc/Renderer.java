/*
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

		Map<String, List<Block>> recordingsByMethod = recordingsByMethod(methodNames, recordings.linesOfCode());

		return render(recordings, recordingsByMethod);
	}

	private static String render(Recordings recordings, Map<String, List<Block>> recordingsByMethod) {
		Map<String, String> joinedMap = merge(recordings, recordingsByMethod);

		String templateContent = recordings.templateReference().readContent()
			.orElseGet(() -> templateFrom(recordings.templateReference(), joinedMap));
		ReplacementPattern replacementPattern = recordings.templateReference().replacementPattern();
		
		return recordings.replacementNotFoundFallback().isPresent()
				? Template.render(Template.of(templateContent, replacementPattern), joinedMap, recordings.replacementNotFoundFallback().get())
				: Template.render(Template.of(templateContent, replacementPattern), joinedMap);
	}

	private static String templateFrom(TemplateReference templateReference, Map<String, String> joinedMap) {
		String source = TemplateReference.readContent(Renderer.class, "template-is-missing-fallback.md");
		return Template.render(Template.of(source), Replacements.builder()
				.putReplacement("templateName", templateReference.templateName())
				.putReplacement("templateClass", templateReference.clazz().getName())
				.putReplacement("recordedParts", joinedMap.keySet().stream()
					.map(key -> "* `"+key+"`")
					.collect(Collectors.joining("\n")))
			.build().replacement());
	}

	private static Map<String, String> merge(Recordings recordings, Map<String, List<Block>> recordingsByMethod) {
		Set<String> usedKeys=new LinkedHashSet<>();

		ImmutableReplacements.Builder builder = Replacements.builder();
		recordingsByMethod.forEach((method, blocks) -> {
			builder.putReplacement(method, formatBlocks(blocks));
			usedKeys.add(method);

			AtomicInteger counter=new AtomicInteger(0);
			for (Block block : blocks) {
				String blockLabel = method+"."+counter.incrementAndGet();
				builder.putReplacement(blockLabel, block.content);
				usedKeys.add(blockLabel);

				if (block.label.isPresent()) {
					String secondBlockLabel = method + "." + block.label.get();
					builder.putReplacement(secondBlockLabel, block.content);
					usedKeys.add(secondBlockLabel);
				}
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
		recordings.methodsCalled().forEach((label, calledMethod) -> {
			String methodCode = findSurroundingMethodOf(recordings.linesOfCode(), calledMethod);
			builder.putReplacement(label, methodCode);
		});
		return builder.build().replacement();
	}

	private static final String CLOSING_BRACE_REGEX="\\s*}\\s*";

	private static String findSurroundingMethodOf(List<String> linesOfCode, CalledMethod calledMethod) {
		int lineIndex = calledMethod.line().lineIndex();

		Preconditions.checkArgument(lineIndex <= linesOfCode.size(),"line number(%s) > lines of code(%s)", lineIndex, linesOfCode.size());
//		System.out.println(linesOfCode.size()+" ? "+ lineIndex);
//		System.out.println(linesOfCode.get(lineIndex - 1));

		int start=-1;
		int end=-1;

		for (int i=lineIndex-1; i>=0;i--) {
			String line = linesOfCode.get(i);
			if (line.contains(calledMethod.line().methodName())) {
//				System.out.println("found "+line+" at "+i);
				start=i;
				break;
			}
		}
		Preconditions.checkArgument(start!=-1,"could not find method declaration for "+calledMethod.line().methodName());

		for (int i=lineIndex;i<linesOfCode.size();i++) {
			String lastLine = i >0 ? linesOfCode.get(i-1) : "{}";
			String line = linesOfCode.get(i);

			if (line.trim().isEmpty() && i>lineIndex) {
				if (lastLine.matches(CLOSING_BRACE_REGEX)) {
//					System.out.println("found "+line+" at "+i);
					end = i;
					break;
				}
			} else {
				if (i+1==linesOfCode.size()) {
					if (line.matches(CLOSING_BRACE_REGEX) && lastLine.matches(CLOSING_BRACE_REGEX)) {
//						System.out.println("found EOF '"+line+"' at "+i);
						end = i;
					}
				}
			}
		}

		Preconditions.checkArgument(end!=-1,"could not find end of method declaration for "+calledMethod.line().methodName());

		List<String> lines = new ArrayList<>();
		lines.addAll(linesOfCode.subList(start, lineIndex));
		lines.addAll(linesOfCode.subList(lineIndex+1,end+1));
		
		return blockOf(lines, 0, lines.size());
	}

	private static String formatBlocks(List<Block> blocks) {
		return blocks.stream().map(block -> block.content).collect(Collectors.joining("\n...\n\n"));
	}

	private static Map<String, List<Block>> recordingsByMethod(Map<String, List<HasLine>> methodNames, List<String> linesOfCode) {
		Map<String, List<Block>> ret=new LinkedHashMap<>();
		for (String key : methodNames.keySet()) {
			ret.put(key, recordings(methodNames.get(key), linesOfCode));
		}
		return ret;
	}

	private static List<Block> recordings(List<HasLine> list, List<String> linesOfCode) {
		List<Block> ret=new ArrayList<>();

		List<HasLine> sortedLineNumbers = list.stream()
			.sorted(Comparator.comparingInt(a -> a.line().lineNumber()))
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
					ret.add(new Block(
						blockOf(linesOfCode, lastStart.line().lineNumber(), line.line().lineNumber()),
						lastStart.label()
					));
					lastStart=null;
				} else {
					Preconditions.checkArgument(false, "hmm... should not happen: %s",line);
				}
			}
		}

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

	private static class Block {
		final String content;
		final Optional<String> label;

		Block(String content, Optional<String> label) {
			this.content = content;
			this.label = label;
		}
	}
}
