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


import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

public class TemplateTest {

	@Test
	public void noPlaceHolderNoReplacement() {
		String result = render("foo", var -> "NOOP");
		assertEquals("foo", result);
	}
	
	@Test
	public void placeHolderFullReplacement() {
		String result = render("${foo}", var -> "NOOP");
		assertEquals("NOOP", result);
	}
	
	@Test
	public void placeHolderStartReplacement() {
		String result = render("${foo}BAR", var -> "NOOP");
		assertEquals("NOOPBAR", result);
	}
	
	@Test
	public void placeHolderEndReplacement() {
		String result = render("BAR${foo}", var -> "NOOP");
		assertEquals("BARNOOP", result);
	}
	
	@Test
	public void doubleReplacement() {
		String result = render("${foo}${bar}", var -> "["+var+"]");
		assertEquals("[foo][bar]", result);
	}
	
	@Test
	public void multipleReplacementsWithSpace() {
		String result = render("space${foo} ${bar} and more", var -> "["+var+"]");
		assertEquals("space[foo] [bar] and more", result);
	}

	@Test
	public void multipleReplacementsWithSpaceWorksWithAllPatterns() {
		for (ReplacementPattern replacementPattern : ReplacementPattern.values()) {
			String templateContent = "space"+asVariable("foo", replacementPattern)+" "+asVariable("bar", replacementPattern)+" and more";
			String result = Template.render(Template.of(templateContent, replacementPattern), var -> "[" + var + "]");
			assertThat(result)
				.describedAs("rendered with replacementPattern %s", replacementPattern)
				.isEqualTo("space[foo] [bar] and more");
		}
	}

	private static String asVariable(String name, ReplacementPattern replacementPattern) {
		switch (replacementPattern) {
			case DEFAULT: return "${"+name+"}";
			case DOUBLE_CURLY: return "{{"+name+"}}";
			default:
				throw new IllegalArgumentException("not implemented: "+replacementPattern);
		}
	}

	@Test
	public void mapReplacementsWithCrazyName() {
		String key = "abc091.-:_2123ya23";
		Map<String, String> map=new LinkedHashMap<>();
		map.put(key, "DONE");
		
		String result = Template.render(Template.of( ">>${" + key + "}<<"), map);
		assertEquals(">>DONE<<", result);
	}

	private static String render(String source, Function<String, String> variableLookUp) {
		return Template.render(Template.of(source), variableLookUp);
	}
}
