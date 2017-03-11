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

import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public interface ResourceFilter extends Function<String, String>{
	
	static final Pattern NEW_LINE= Pattern.compile("(?<newline>\\n\\r?)");

	public static ResourceFilter join(ResourceFilter ...filters) {
		return src -> {
			String ret=src;
			for (ResourceFilter f : filters) {
				ret=f.apply(ret);
			}
			return ret;
		};
	}
	
	public static ResourceFilter indent(String beforeEachLine) {
		return src -> {
			Matcher matcher = NEW_LINE.matcher(src);
			int lastStart=0;
			StringBuilder sb=new StringBuilder();
			while (matcher.find()) {
				sb.append(beforeEachLine);
				sb.append(src.substring(lastStart, matcher.start()));
				sb.append(matcher.group("newline"));
				lastStart=matcher.end();
			}
			if (lastStart<src.length()) {
				sb.append(beforeEachLine);
				sb.append(src.substring(lastStart));
			}
			return sb.toString();
		};
	}
}
