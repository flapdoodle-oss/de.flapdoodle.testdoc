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
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClass;

public class HowToHowToTest {

	@RegisterExtension
	public static Recording recording=Recorder.with("howto-howto.md", TabSize.spaces(2))
		.sourceCodeOf("howToTest", HowToTest.class, Includes.WithoutPackage, Includes.WithoutImports, Includes.Trim)
		.resource("howToTest.md", HowToTest.class, "howto.md"/*, ResourceFilter.indent("\t")*/)
		.sourceCodeOf("howToDoubleCurlyTest", HowToDifferentReplacementPatternTest.class, Includes.WithoutPackage, Includes.WithoutImports, Includes.Trim)
		.resource("howToDoubleCurlyTest.md", HowToDifferentReplacementPatternTest.class, "howtoDoubleCurly.md"/*, ResourceFilter.indent("\t")*/)
		.sourceCodeOf("methodSourceTest", MethodSourceTest.class, Includes.WithoutPackage, Includes.WithoutImports, Includes.Trim)
		.resource("methodSourceTest.md", MethodSourceTest.class, "method-source.md"/*, ResourceFilter.indent("\t")*/)
//		.replacementNotFoundFallback((key, keys) -> "${"+key+"->not found in "+keys+"}")
		;

	@Test
	public void includeResources() {
		recording.resource(getClass(), "howto-howto-pom.part"/*, ResourceFilter.indent("\t")*/);
	}

	@Test
	public void runTest() {
		recordTestRun(
			"howtoOutput",
			HowToTest.class,
			() -> HowToTest.recording,
			() -> Recorder.with(HowToTest.class,"howto.md", TabSize.spaces(2))
				.sourceCodeOf("fooClass", FooClass.class),
			r -> HowToTest.recording=r
		);
	}

	@Test
	public void runTestDoubleCurly() {
		recordTestRun(
			"howtoDoubleCurlyOutput",
			HowToDifferentReplacementPatternTest.class,
			() -> HowToDifferentReplacementPatternTest.recording,
			() -> Recorder.with(HowToDifferentReplacementPatternTest.class,"howtoDoubleCurly.md", ReplacementPattern.DOUBLE_CURLY, TabSize.spaces(2))
				.sourceCodeOf("fooClass", FooClass.class),
			r -> HowToDifferentReplacementPatternTest.recording=r
		);
	}

	@Test
	public void runMissingTemplate() {
		recordTestRun(
			"missingTemplateOutput",
			MissingTemplateTest.class,
			() -> MissingTemplateTest.recording,
			() -> Recorder.with(MissingTemplateTest.class,"missingTemplate.md", TabSize.spaces(2)),
			r -> MissingTemplateTest.recording=r
		);
	}

	@Test
	public void runMethodSource() {
		recordTestRun(
			"methodSourceOutput",
			MethodSourceTest.class,
			() -> MethodSourceTest.recording,
			() -> Recorder.with(MethodSourceTest.class,"method-source.md", TabSize.spaces(2)),
			r -> MethodSourceTest.recording=r
		);
	}

	static void recordTestRun(
		String label,
		Class<?> testClass,
		Supplier<Recording> readRecording,
		Supplier<Recording> recordingFactory,
		Consumer<Recording> setRecording
	) {
		AtomicReference<String> renderedOutput=new AtomicReference<String>();

		// redirect output for the following recording
		Recording.runWithTemplateConsumer((name, content) -> {
			renderedOutput.set(content);
		}).accept(() -> {

			// run junit
			LauncherDiscoveryRequest request = LauncherDiscoveryRequestBuilder.request()
				.selectors(
					selectClass(testClass)
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
				Recording oldRecording = readRecording.get();

				// recreate static recording test instance
				setRecording.accept(recordingFactory.get());

				// Execute test plan
				launcher.execute(testPlan);
//				// Alternatively, execute the request directly
//				launcher.execute(request);


				// restore recording instance
				setRecording.accept(oldRecording);
			}

			TestExecutionSummary summary = listener.getSummary();
			//summary.printFailuresTo(new PrintWriter(System.out), 10);

			assertEquals(0, summary.getFailures().size());

		});

		// extract recorded content
		String content = renderedOutput.get();
		assertNotNull(content, "renderedTemplate");
		recording.output(label, content /*ResourceFilter.indent("\t").apply(content)*/);
	}
}
