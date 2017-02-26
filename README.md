# Organisation Flapdoodle OSS
[![Maven Central](https://img.shields.io/maven-central/v/de.flapdoodle.testdoc/de.flapdoodle.testdoc.svg)](https://maven-badges.herokuapp.com/maven-central/de.flapdoodle.testdoc/de.flapdoodle.testdoc)

We are a github organisation. You are invited to participate.

## de.flapdoodle.testdoc

simple doc generator based on unit tests

### Maven

Stable (Maven Central Repository, Released: 26.02.2017 - wait 24hrs for [maven central](http://repo1.maven.org/maven2/de/flapdoodle/guava/de.flapdoodle.testdoc/maven-metadata.xml))

	<dependency>
		<groupId>de.flapdoodle.testdoc</groupId>
		<artifactId>de.flapdoodle.testdoc</artifactId>
		<version>1.1.0</version>
	</dependency>

Snapshots (Repository http://oss.sonatype.org/content/repositories/snapshots)

	<dependency>
		<groupId>de.flapdoodle.testdoc</groupId>
		<artifactId>de.flapdoodle.testdoc</artifactId>
		<version>1.1.1-SNAPSHOT</version>
	</dependency>

### Usage

Use Recording as `ClassRule` like this:

```
public class HowToTest {

	@ClassRule
	public static Recording recording=Recorder.generateMarkDown("howto.md");

	@Test
	public void theMethodNameIsTheKey() {
		// everything after this marker ...
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

	# How To

	some text

	## Simple Sample 

	```
	${theMethodNameIsTheKey}
	```

	## Sample with more than one block

	```
	${multipleCodeBlocks}
	```

... add a property in your `pom.xml` :

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

... and you will get this:

	# How To

	some text

	## Simple Sample 

	```

	boolean sampleVar = true;
	assertTrue(sampleVar);

	```

	## Sample with more than one block

	```
	// first block
	...

	// second block
	```
