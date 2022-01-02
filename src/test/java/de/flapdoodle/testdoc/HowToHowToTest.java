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

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.platform.launcher.Launcher;
import org.junit.platform.launcher.LauncherDiscoveryRequest;
import org.junit.platform.launcher.LauncherSession;
import org.junit.platform.launcher.TestPlan;
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder;
import org.junit.platform.launcher.core.LauncherFactory;
import org.junit.platform.launcher.listeners.SummaryGeneratingListener;
import org.junit.platform.launcher.listeners.TestExecutionSummary;

import java.io.PrintWriter;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClass;

public class HowToHowToTest {

	@RegisterExtension
	public static Recording recording=Recorder.with("howto-howto.md", TabSize.spaces(2))
		.sourceCodeOf("howToTest", HowToTest.class, Includes.WithoutPackage, Includes.WithoutImports, Includes.Trim)
		.resource("howToTest.md", HowToTest.class, "howto.md"/*, ResourceFilter.indent("\t")*/)
//		.replacementNotFoundFallback((key, keys) -> "${"+key+"->not found in "+keys+"}")
		;

	@Test
	public void includeResources() {
		recording.resource(getClass(), "howto-howto-pom.part"/*, ResourceFilter.indent("\t")*/);
	}
	
	@Test
//	@Ignore
	public void runTest() {
		AtomicReference<String> renderedOutput=new AtomicReference<String>();
		
		// redirect output for the following recording
		Recording.runWithTemplateConsumer((name, content) -> {
			renderedOutput.set(content);
		}).accept(() -> {
			
			// run junit
			LauncherDiscoveryRequest request = LauncherDiscoveryRequestBuilder.request()
				.selectors(
					selectClass(HowToTest.class)
				)
				.build();

			SummaryGeneratingListener listener = new SummaryGeneratingListener();

			try (LauncherSession session = LauncherFactory.openSession()) {
				Launcher launcher = session.getLauncher();
				// Register a listener of your choice
				launcher.registerTestExecutionListeners(listener);
				// Discover tests and build a test plan
				TestPlan testPlan = launcher.discover(request);

				// recover current recording instance from test class
				Recording oldRecording = HowToTest.recording;

				// recreate static recording test instance
				HowToTest.recording=Recorder.with(HowToTest.class, "howto.md", TabSize.spaces(2))
					.sourceCodeOf("fooClass", FooClass.class);


				// Execute test plan
				launcher.execute(testPlan);
//				// Alternatively, execute the request directly
//				launcher.execute(request);

				
				// restore recording instance
				HowToTest.recording=oldRecording;
			}

			TestExecutionSummary summary = listener.getSummary();
			//summary.printFailuresTo(new PrintWriter(System.out), 10);

			assertEquals(0, summary.getFailures().size());
			
		});
		
		// extract recorded content
		String content = renderedOutput.get();
		assertNotNull(content, "renderedTemplate");
		recording.output("renderResult", content /*ResourceFilter.indent("\t").apply(content)*/);
	}
}
