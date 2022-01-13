### Usage

Use Recording as `RegisterExtension` like this:

```java
${howToTest}
```

.. and create MarkDown template as TestClass-Resource (same package):

````markdown
${howToTest.md}
````

... add a property in your `pom.xml` :

```xml
${includeResources.HowToHowToTest:howto-howto-pom.part}
```

... and you will get this:

````markdown
${recordTestRun.howtoOutput}
````

If you forgot to create the template, then this content will be rendered into the document:

````markdown
${recordTestRun.missingTemplateOutput}
````
