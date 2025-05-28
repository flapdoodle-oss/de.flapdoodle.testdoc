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

import org.immutables.value.Value;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiFunction;

@Value.Immutable
public interface Recordings {
	TemplateReference templateReference();

	List<String> linesOfCode();

	List<HasLine> lines();

	Map<String, CalledMethod> methodsCalled();

	Map<String, String> classes();

	Map<String, String> resources();

	Map<String, String> output();

	Optional<BiFunction<String, Set<String>, String>> replacementNotFoundFallback();

	static ImmutableRecordings.Builder builder() {
		return ImmutableRecordings.builder();
	}
}
