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
import org.immutables.value.Value;

import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.regex.Matcher;

@Value.Immutable
public abstract class Template {

	public abstract String source();
	
	@Value.Default
	public ReplacementPattern pattern() {
		return ReplacementPattern.DEFAULT;
	}

	public static Template of(String source) {
		return ImmutableTemplate.builder()
			.source(source)
			.build();
	}

	public static Template of(String source, ReplacementPattern pattern) {
		return ImmutableTemplate.builder()
			.source(source)
			.pattern(pattern)
			.build();
	}

	public static String render(Template template, Map<String, String> replacements) {
		return render(template, key -> Preconditions.checkNotNull(replacements.get(key),"could not resolve %s in %s",key, replacements.keySet()));
	}

	public static String render(Template template, Map<String, String> replacements, BiFunction<String, Set<String>, String> fallback) {
		return render(template, key -> {
			String replacement = replacements.get(key);
			if (replacement==null) {
				replacement=Preconditions.checkNotNull(fallback.apply(key, replacements.keySet()),"fallback return null for %s", key);
			}
			return replacement;
		});
	}

	public static String render(Template template, Function<String, String> variableLookUp) {
		StringBuilder sb=new StringBuilder();
		String source = template.source();

		Matcher matcher = template.pattern().matcher(source);
		int lastEnd=0;
		while (matcher.find()) {
			sb.append(source, lastEnd, matcher.start());
			sb.append(variableLookUp.apply(matcher.group("label")));
			lastEnd=matcher.end();
		}
		sb.append(source.substring(lastEnd));
		return sb.toString();
	}
}
