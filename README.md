# Organisation Flapdoodle OSS
[![Maven Central](https://img.shields.io/maven-central/v/de.flapdoodle.testdoc/de.flapdoodle.testdoc.svg)](https://maven-badges.herokuapp.com/maven-central/de.flapdoodle.testdoc/de.flapdoodle.testdoc)

We are a github organisation. You are invited to participate.

## de.flapdoodle.testdoc

simple doc generator based on unit tests

### Maven

Stable (Maven Central Repository, Released: 09.04.2017 - wait 24hrs for [maven central](http://repo1.maven.org/maven2/de/flapdoodle/guava/de.flapdoodle.testdoc/maven-metadata.xml))

	<dependency>
		<groupId>de.flapdoodle.testdoc</groupId>
		<artifactId>de.flapdoodle.testdoc</artifactId>
		<version>1.2.1</version>
	</dependency>

Snapshots (Repository http://oss.sonatype.org/content/repositories/snapshots)

	<dependency>
		<groupId>de.flapdoodle.testdoc</groupId>
		<artifactId>de.flapdoodle.testdoc</artifactId>
		<version>1.2.2-SNAPSHOT</version>
	</dependency>

### Usage

Use Recording as `ClassRule` like this:

```java
public class HowToTest {

  @RegisterExtension
  public static Recording recording=Recorder.with("howto.md", TabSize.spaces(2))
    .sourceCodeOf("fooClass", FooClass.class);

  @Test
  public void theMethodNameIsTheKey() {
    // everything after this marker ...
    recording.include(BarClass.class, Includes.WithoutPackage, Includes.Trim, Includes.WithoutImports);
    recording.begin();
    
    boolean sampleVar = true;
    assertTrue(sampleVar);
    
    recording.end();
    // nothing after this marker
  }
  
  @Test
  public void multipleCodeBlocks() {
    recording.begin();
    // first block
    recording.end();
    recording.begin();
    // second block
    recording.end();
  }
}
```

.. and create MarkDown template as TestClass-Resource (same package):

````markdown
# How To

some text

## Simple Sample 

```
${theMethodNameIsTheKey}
```

### .. with class include

```
${theMethodNameIsTheKey.BarClass}
```


## Sample with more than one block

```
${multipleCodeBlocks}
```

## Sample with more than one block - alt version

```
${multipleCodeBlocks.1}
```

```
${multipleCodeBlocks.2}
```

# Includes

```
${fooClass}
```

```
${theMethodNameIsTheKey.BarClass}
```
````

... add a property in your `pom.xml` :

```xml
<plugin>
	<groupId>org.apache.maven.plugins</groupId>
	<artifactId>maven-surefire-plugin</artifactId>
	<version>2.19.1</version>
	<configuration>
		<systemPropertyVariables>
			<de.flapdoodle.testdoc.destination>${project.build.directory}</de.flapdoodle.testdoc.destination>
	  </systemPropertyVariables>
	</configuration>
</plugin>
```

... and you will get this:

````markdown
# How To

some text

## Simple Sample 

```

boolean sampleVar = true;
assertTrue(sampleVar);

```

### .. with class include

```
public class BarClass {
  Map<String, Object> map;
}
```


## Sample with more than one block

```
// first block
...

// second block
```

## Sample with more than one block - alt version

```
// first block
```

```
// second block
```

# Includes

```
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

public class FooClass {
  // nothing here
}
```

```
public class BarClass {
  Map<String, Object> map;
}
```
````
