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

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class HowToAddFilesTest {

	@RegisterExtension
	public static Recording recording=Recorder.with("HowToAddFiles.md", TabSize.spaces(2))
		.renderTo("howto-add-files.md");

	@Test
	public void inAndMethod() {
		recording.file("file", "test.txt", "could be any content".getBytes(StandardCharsets.UTF_8));
		recording.begin();
		// ..
		recording.end();
	}
}
