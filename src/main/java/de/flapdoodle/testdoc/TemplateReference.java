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

import java.util.Optional;

@Value.Immutable
public interface TemplateReference {
	@Value.Parameter
	Class<?> clazz();
	@Value.Parameter
	String templateName();

	@Value.Lazy
	default Optional<String> readContent() {
		try {
			return Optional.of(readContent(clazz(), templateName()));
		} catch (RuntimeException rx) {
			return Optional.empty();
		}
	}

	static TemplateReference of(Class<?> clazz, String templateName) {
		return ImmutableTemplateReference.of(clazz, templateName);
	}

	static String readContent(Class<?> clazz, String template) {
		return Resources.read(() -> Preconditions.checkNotNull(clazz.getResourceAsStream(template),"could not get %s for %s",template, clazz));
	}
}
