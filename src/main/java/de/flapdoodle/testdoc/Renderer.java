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

	protected static String renderTemplate(String templateName, Recordings recordings) {
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
		
		return recordings.replacementNotFoundFallback().isPresent()
				? Template.render(recordings.templateContent(), joinedMap, recordings.replacementNotFoundFallback().get())
				: Template.render(recordings.templateContent(), joinedMap);
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
