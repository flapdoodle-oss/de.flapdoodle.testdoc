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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public enum ReplacementPattern {
	/**
	 * pattern is ${name}
	 */
	DEFAULT("\\$\\{", "\\}"),

	/**
	 * pattern is {{name}}
	 */
	DOUBLE_CURLY("\\{\\{", "\\}\\}")

	;

	private final Pattern pattern;

	ReplacementPattern(String start, String end) {
		this.pattern = Pattern.compile("(?<all>" + start + "(?<label>[a-zA-Z0-9\\-_:\\.]+)" + end + ")");
	}
	
	public Matcher matcher(String source) {
		return pattern.matcher(source);
	}
}
