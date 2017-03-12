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

import org.junit.ClassRule;
import org.junit.Test;

public class SampleTest {
	
	@ClassRule
	public static Recording recording=Recorder.with("sample.md");

	@Test
	public void firstMethod() {
		recording.begin();
		
		// content

		assertEquals("first","first");
		
		recording.end();
	}
	
	@Test
	public void secondMethod() {
		recording.begin();
		
		// content
		             
		assertEquals("second","second");
		
		recording.end();
		
		recording.begin();
		// second block
		recording.end();
	}
}
