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

import org.immutables.value.Value.Immutable;
import org.immutables.value.Value.Lazy;
import org.immutables.value.Value.Parameter;

@Immutable
public interface TabSize {
	@Parameter
	int spaces();
	
	@Lazy 
	default String asSpaces() {
		char[] buffer = new char[spaces()];
		for (int i=0;i<buffer.length;i++) {
			buffer[i]=' ';
		}
		return String.copyValueOf(buffer);
	}
	
	public static TabSize spaces(int nr) {
		return ImmutableTabSize.of(nr);
	}
}
