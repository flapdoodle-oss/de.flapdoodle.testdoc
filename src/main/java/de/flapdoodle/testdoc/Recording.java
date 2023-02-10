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

import de.flapdoodle.testdoc.Stacktraces.Scope;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;

public class Recording implements AfterAllCallback {

	private static final String DEST_DIR_PROPERTY = "de.flapdoodle.testdoc.destination";
	private static final ThreadLocal<BiConsumer<String, String>> templateConsumer = new ThreadLocal<>();

	private final TemplateReference templateReference;
	private final List<String> testSourceCode;
	private final List<HasLine> lines = new ArrayList<>();
	private final Map<String, CalledMethod> calledMethod = new LinkedHashMap<>();
	private final Map<String, String> classes = new LinkedHashMap<>();
	private final Map<String, String> resources = new LinkedHashMap<>();
	private final Map<String, String> output = new LinkedHashMap<>();
	private final TabSize tabSize;
	private Optional<BiFunction<String, Set<String>, String>> replacementNotFoundFallback = Optional.empty();

	protected Recording(TemplateReference templateReference, List<String> testSourceCode, TabSize tabSize) {
		this.tabSize = tabSize;
		this.templateReference = Preconditions.checkNotNull(templateReference, "template name is null");
		this.testSourceCode = new ArrayList<>(Preconditions.checkNotNull(testSourceCode, "linesOfCode is null"));
	}

	public Recording sourceCodeOf(String label, Class<?> clazz, Includes... includeOptions) {
		Optional<List<String>> sourceCode = Resources.sourceCodeOf(clazz, tabSize, includeOptions);
		Preconditions.checkArgument(sourceCode.isPresent(), "could not find sourceCode of %s", clazz);
		String old = classes.put(label, Resources.joinedWithNewLine(sourceCode.get()));
		Preconditions.checkArgument(old == null, "sourceCodeOf with label %s was already set to %s", label, old);
		return this;
	}

	/**
	 * use with care, it is not stable, it can happen that
	 * the wrong fragment is used
	 * @param label name for this segment
	 */
	@Deprecated
	public Recording thisMethod(String label) {
		Line currentLine = Stacktraces.currentLine(Scope.CallerOfCaller);
		CalledMethod old = calledMethod.put(label, CalledMethod.of(currentLine));
		Preconditions.checkArgument(old == null, "method with label %s was already set to %s", label, old);
		return this;
	}

	public Recording resource(String label, Class<?> clazz, String resourceName, ResourceFilter... filters) {
		Optional<String> resource = Resources.resource(clazz, resourceName);
		Preconditions.checkArgument(resource.isPresent(), "could not find resource of %s:%s", clazz, resourceName);
		String old = resources.put(label, resource.map(ResourceFilter.join(filters)).get());
		Preconditions.checkArgument(old == null, "resource with label %s was already set to %s", label, old);
		return this;
	}

	public Recording replacementNotFoundFallback(BiFunction<String, Set<String>, String> fallback) {
		Preconditions.checkArgument(!replacementNotFoundFallback.isPresent(), "already set to: %s", replacementNotFoundFallback);
		this.replacementNotFoundFallback = Optional.of(fallback);
		return this;
	}

	@Override
	public void afterAll(ExtensionContext extensionContext) throws Exception {
		String renderedTemplate = Renderer.renderTemplate(Recordings.builder()
			.templateReference(templateReference)
			.linesOfCode(testSourceCode)
			.lines(lines)
			.methodsCalled(calledMethod)
			.classes(classes)
			.resources(resources)
			.output(output)
			.replacementNotFoundFallback(replacementNotFoundFallback)
			.build());

		writeResult(templateReference.templateName(), renderedTemplate);
	}

	protected static void writeResult(String templateName, String renderedTemplate) {
		if (templateConsumer.get() != null) {
			templateConsumer.get().accept(templateName, renderedTemplate);
		} else {
			String destination = System.getProperty(DEST_DIR_PROPERTY);
			if (destination != null) {
				Path output = Paths.get(destination).resolve(templateName);
				try {
					Files.write(output, renderedTemplate.getBytes(Charset.forName("UTF-8")), StandardOpenOption.WRITE, StandardOpenOption.CREATE,
						StandardOpenOption.TRUNCATE_EXISTING);
				}
				catch (IOException iox) {
					throw new RuntimeException("could not write " + output, iox);
				}
			} else {
				System.out.println(DEST_DIR_PROPERTY + " not set");
				System.out.println("---------------------------");
				System.out.println("should write " + templateName);
				System.out.println("---------------------------");
				System.out.println(renderedTemplate);
				System.out.println("---------------------------");
			}
		}
	}

	public void include(Class<?> clazz, Includes... includeOptions) {
		Line currentLine = Stacktraces.currentLine(Scope.CallerOfCaller);
		String label = currentLine.methodName() + "." + clazz.getSimpleName();
		sourceCodeOf(label, clazz, includeOptions);
	}

	public void resource(Class<?> clazz, String resourceName, ResourceFilter... filters) {
		Line currentLine = Stacktraces.currentLine(Scope.CallerOfCaller);
		String label = currentLine.methodName() + "." + clazz.getSimpleName() + ":" + resourceName;
		resource(label, clazz, resourceName, filters);
	}

	public void output(String label, String content) {
		Line currentLine = Stacktraces.currentLine(Scope.CallerOfCaller);
		String scopedLabel = currentLine.methodName() + "." + label;
		String old = output.put(scopedLabel, content);
		Preconditions.checkArgument(old == null, "%s already set to %s", label, old);
	}


	public void begin() {
		Line currentLine = Stacktraces.currentLine(Scope.CallerOfCaller);
		lines.add(Start.of(currentLine));
	}

	public void begin(String label) {
		Line currentLine = Stacktraces.currentLine(Scope.CallerOfCaller);
		lines.add(Start.of(label, currentLine));
	}

	public void end() {
		Line currentLine = Stacktraces.currentLine(Scope.CallerOfCaller);
		lines.add(End.of(currentLine));
	}

	private static BiConsumer<String, String> setTemplateConsumerForInternalUse(BiConsumer<String, String> consumer) {
		BiConsumer<String, String> old = templateConsumer.get();
		templateConsumer.set(consumer);
		return old;
	}

	protected static Consumer<Runnable> runWithTemplateConsumer(BiConsumer<String, String> consumer) {
		return runnable -> {
			BiConsumer<String, String> old = setTemplateConsumerForInternalUse(consumer);
			try {
				runnable.run();
			}
			finally {
				setTemplateConsumerForInternalUse(old);
			}
		};
	}
}