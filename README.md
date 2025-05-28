# Organisation Flapdoodle OSS
[![Maven Central](https://img.shields.io/maven-central/v/de.flapdoodle.testdoc/de.flapdoodle.testdoc.svg)](https://maven-badges.herokuapp.com/maven-central/de.flapdoodle.testdoc/de.flapdoodle.testdoc)

We are a github organisation. You are invited to participate.

## de.flapdoodle.testdoc

simple doc generator based on unit tests

### Maven

Stable (Maven Central Repository, Released: 28.05.2025 - wait 24hrs for [maven central](http://repo1.maven.org/maven2/de/flapdoodle/guava/de.flapdoodle.testdoc/maven-metadata.xml))

	<dependency>
		<groupId>de.flapdoodle.testdoc</groupId>
		<artifactId>de.flapdoodle.testdoc</artifactId>
		<version>1.6.2</version>
	</dependency>

### Usage

Use Recording as `RegisterExtension` like this:

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
    recording.begin("named");
    // second block - named
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

```
${multipleCodeBlocks.named}
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

// second block - named
```

## Sample with more than one block - alt version

```
// first block
```

```
// second block - named
```

```
// second block - named
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

#### Different Replacement Pattern                            

With different replacement pattern as in:                                          

```java
public class HowToDifferentReplacementPatternTest {

  @RegisterExtension
  public static Recording recording=Recorder.with("howtoDoubleCurly.md", ReplacementPattern.DOUBLE_CURLY, TabSize.spaces(2))
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
    recording.begin("named");
    // second block - named
    recording.end();
  }
}
```

.. and a differnt template as TestClass-Resource (same package):

````markdown
# How To

some text

## Simple Sample 

```
{{theMethodNameIsTheKey}}
```

### .. with class include

```
{{theMethodNameIsTheKey.BarClass}}
```


## Sample with more than one block

```
{{multipleCodeBlocks}}
```

## Sample with more than one block - alt version

```
{{multipleCodeBlocks.1}}
```

```
{{multipleCodeBlocks.2}}
```

```
{{multipleCodeBlocks.named}}
```

# Includes

```
{{fooClass}}
```

```
{{theMethodNameIsTheKey.BarClass}}
```
````

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

// second block - named
```

## Sample with more than one block - alt version

```
// first block
```

```
// second block - named
```

```
// second block - named
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

#### Method Called

.. to add a method called inside test class you can

```java
public class MethodSourceTest {

  @RegisterExtension
  public static Recording recording=Recorder.with("method-source.md", TabSize.spaces(2));

  @Test
  public void testMethod() {
    recording.begin();
    otherMethod();
    recording.end();
  }

  public void otherMethod() {
    recording.thisMethod("otherMethod");
    // you should include this

    // and this
  }
}
```

.. and create MarkDown template as TestClass-Resource (same package):

````markdown
# include method source code

```java
${testMethod}
```

calls:

```java
${otherMethod}
```
````
... and you will get this:

````markdown
# include method source code

```java
otherMethod();
```

calls:

```java
public void otherMethod() {
  // you should include this

  // and this
}
```
````

#### Add Files

... to write some files as part of the test

```java
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
```

.. and create MarkDown template as TestClass-Resource (same package):

````markdown
# An other HowTo

this documentation references this [file](${inAndMethod.file})

````
... and you will get this:

````markdown
# An other HowTo

this documentation references this [file](test.txt)

````

with these files generated: test.txt


#### Without Template

If you forgot to create the template, then this content will be rendered into the document:

````markdown
# document from generated template

As you did not create a matching template with the name

> `missingTemplate.md`

in the same package as

> `de.flapdoodle.testdoc.MissingTemplateTest`

the content of this file is generated from the recordings of your test class.

In your test following parts were recorded:

* `firstTest`
* `firstTest.1`

To insert the content of a part into the generated document you must embed a name
from this list between a starting `${` and `}`.
````
