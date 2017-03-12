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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.concurrent.atomic.AtomicReference;

import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;

public class HowToHowToTest {

	@ClassRule
	public static Recording recording=Recorder.with("howto-howto.md", TabSize.spaces(2))
		.sourceCodeOf("howToTest", HowToTest.class, Includes.WithoutPackage, Includes.WithoutImports, Includes.Trim)
		.resource("howToTest.md", HowToTest.class, "howto.md", ResourceFilter.indent("\t"))
//		.replacementNotFoundFallback((key, keys) -> "${"+key+"->not found in "+keys+"}")
		;

	@Test
	public void includeResources() {
		recording.resource(getClass(), "howto-howto-pom.part", ResourceFilter.indent("\t"));
	}
	
	@Test
//	@Ignore
	public void runTest() {
		AtomicReference<String> renderedOutput=new AtomicReference<String>();
		
		// redirect output for the following recording
		Recording.runWithTemplateConsumer((name, content) -> {
			renderedOutput.set(content);
		}).accept(() -> {
			
			// recover current recording instance from test class
			Recording oldRecording = HowToTest.recording;
			
			// recreate static recording test instance
			HowToTest.recording=Recorder.with(HowToTest.class, "howto.md", TabSize.spaces(2))
				.sourceCodeOf("fooClass", FooClass.class);
				
			// run junit
			JUnitCore junit = new JUnitCore();
			Result result = junit.run(HowToTest.class);
			assertEquals(0, result.getFailureCount());
			
			// restore recording instance
			HowToTest.recording=oldRecording;
			
		});
		
		// extract recorded content
		String content = renderedOutput.get();
		assertNotNull("renderedTemplate", content);
		recording.output("renderResult", ResourceFilter.indent("\t").apply(content));
	}
}
