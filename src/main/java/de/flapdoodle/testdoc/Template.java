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

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.immutables.value.Value;

import de.flapdoodle.checks.Preconditions;

public abstract class Template {

	private static Pattern VAR_PATTERN=Pattern.compile("(?<all>\\$\\{(?<label>[a-zA-Z0-9\\-_:\\.]+)\\})");
	
	public static String render(String template, Map<String, String> replacements) {
		return render(template, key -> {
			return Preconditions.checkNotNull(replacements.get(key),"could not resolve %s in %s",key, replacements.keySet());
		});
	}
	
	public static String render(String template, Map<String, String> replacements, BiFunction<String, Set<String>, String> fallback) {
		return render(template, key -> {
			String replacement = replacements.get(key);
			if (replacement==null) {
				replacement=Preconditions.checkNotNull(fallback.apply(key, replacements.keySet()),"fallback return null for %s", key);
			}
			return replacement;
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
		
		static ImmutableReplacements.Builder builder() {
			return ImmutableReplacements.builder();
		}
	}
}
