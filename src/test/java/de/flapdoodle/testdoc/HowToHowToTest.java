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

import org.junit.ClassRule;
import org.junit.Test;

public class HowToHowToTest {

	@ClassRule
	public static Recording recording=Recorder.generateMarkDown("howto-howto.md", TabSize.spaces(2))
		.sourceCodeOf("howToTest", HowToTest.class, Includes.WithoutPackage, Includes.WithoutImports, Includes.Trim)
		.resource("howToTest.md", HowToTest.class, "howto.md", ResourceFilter.indent("\t"))
		.replacementNotFoundFallback((key, keys) -> "${"+key+"->not found in "+keys+"}");

	@Test
	public void includeResources() {
		recording.resource(getClass(), "howto-howto-pom.txt", ResourceFilter.indent("\t"));
	}
}
