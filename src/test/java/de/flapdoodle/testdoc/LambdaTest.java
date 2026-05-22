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

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class LambdaTest {
	@RegisterExtension
	public static Recording recording=Recorder.with("lambda.md");

	@Test
	public void legacyCall() {
		call(() -> {
			recording.begin("call");

			assertEquals("first","first");

			recording.end();
		});
	}

	@Test
	public void firstMethod() {
		call(() -> {
			recording.beginInLambda("call");

			assertEquals("first","first");

			recording.endInLambda();
		});
	}

	static void call(Runnable r) {
		r.run();
	}

}
