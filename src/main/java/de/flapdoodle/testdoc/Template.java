package de.flapdoodle.testdoc;

import java.util.Map;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.immutables.value.Value;

public abstract class Template {

	private static Pattern VAR_PATTERN=Pattern.compile("(?<all>\\$\\{(?<label>[a-zA-Z0-9\\-_:\\.]+)\\})");
	
	public static String render(String template, Map<String, String> replacements) {
		return render(template, key -> {
			return Preconditions.checkNotNull(replacements.get(key),"could not resolve %s in %s",key, replacements);
		});
	}
	
	public static String render(String template, Function<String, String> variableLookUp) {
		StringBuilder sb=new StringBuilder();
		Matcher matcher = VAR_PATTERN.matcher(template);
		int lastEnd=0;
		while (matcher.find()) {
			sb.append(template.substring(lastEnd, matcher.start()));
			sb.append(variableLookUp.apply(matcher.group("label")));
			lastEnd=matcher.end();
		}
		sb.append(template.substring(lastEnd));
		return sb.toString();
	}
	
	@Value.Immutable
	public interface Replacements {
		Map<String, String> replacement();
		
		public static ImmutableReplacements.Builder builder() {
			return ImmutableReplacements.builder();
		}
	}
}
