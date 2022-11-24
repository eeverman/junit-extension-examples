# JUnit Extension Registration via Composed Annotation - Almost (but not quite) workable

JUnit's extension system includes a natural way to register extensions via
[composed annotations](https://junit.org/junit5/docs/current/user-guide/#writing-tests-meta-annotations).
The JUnit docs include just two examples of Composed Annotation Extension Registration (***CAER***) in the
[Declarative Extension Registration](https://junit.org/junit5/docs/current/user-guide/#extensions-registration-declarative)
section, so there is not a lot of detail out there for developers.
In *CAER* makes it easy and natural to use extensions.
However, while *CAER* works well for simple extensions, support is lacking for extensions that
require configuration - It's possible, but it's ugly.

## Basics of Composed Annotation Extension Registration (CAER)

If you are not familiar with ***CAER***, here is a quick example.
Below is a simple extension that loads key-value pairs from the ***MyFile.props***
file into `System.properties`:

```java
public class SimpleExt implements BeforeEachCallback, AfterEachCallback {

	public void beforeEach(final ExtensionContext context) { 
		Properties props = new Properties();  
		InputStream is = getClass().getResourceAsStream("/MyFile.props");  
		props.load(is);  
		System.setProperties(props);
	}  
  
	public void afterEach(final ExtensionContext context) {  
		// reset the sys props ...
	}
}
```

There are multiple ways to register the extension, but the most elegant is via *CAER*.
The `@SimpleAnn` composed annotation, below, registers the extension via the `@ExtendWith` annotation:
```java
@Target({ TYPE, METHOD, ANNOTATION_TYPE })  @Retention(RUNTIME)  
@ExtendWith(SimpleExt.class)  // Here just one extension is registered, but it could be several
public @interface SimpleAnn {  }
```

Users can then annotate test classes or methods with `@SimpleAnn` and the extension
is automatically registered.

```java
@SimpleAnn
public class MyTestClass { /* SimpleExt in use in this test class */ }
```

*CAER* makes it easy for users of an extension, but what if the extension needs configuration?
For instance, ***what if we wanted to configure which file is loaded in the `SimpleExt` example?***

## Adding Configuration to an extension registered via *CAER*

JUnit creates the extension instance for us, so there is no opportunity to pass arguments.
The solution is to pass the arguments to the *annotation*, then find the annotation and its arguments
in the extension.

Let's extend our example to configure which file is loaded.
Here is what that could look like if a `classpathFile` property was added to `@SimpleAnn`:

 ```java
@SimpleAnn(classpathFile = "/MyFile.props")
public class MyTestClass {  }
```

The annotation just needs a single line added for the classpathFile property:

```java
@Target({ TYPE, METHOD, ANNOTATION_TYPE })  @Retention(RUNTIME)  
@ExtendWith(SimpleExt.class)  
public @interface SimpleAnn {
	String classpathFile();		// Added
}
```

The extension will need to find the annotation to grab the value, but how?
JUnit includes two different `AnnotationSupport.findAnnotation()` methods that seem to be designed
for the task.  If they worked for this purpose, the extension could look like this:

```java
public class SimpleExt implements BeforeEachCallback, AfterEachCallback {
	
	// Trivial method to grab the configured value from the annotation... but it doesn't work
	public String findPath(final ExtensionContext context) {  
		SimpleAnn ann = AnnotationSupport.findAnnotation(  
			context.getElement(), SimpleAnn.class).get();  
		return ann.classpathFile();  
	}  
  
	@Override  
	public void beforeEach(final ExtensionContext context) throws IOException {
		String findPath(context);
		// load the file...
	}
}
```

There are several reasons why `findAnnotation` may not find the annotation, but
the key issue is that effective scope of JUnit extensions is different than the scope of
Java annotations, and the `findAnnotation` methods tend to follow the Java model.
The scope of a Junit extensions follow these rules:
- An extension registered on a superclass applies to its subclass
- An extension registered on a parent class applies to all `@Nested` test classes

By contrast, annotations in Java follow these rules:
- Annotations on a superclass are only applicable to a subclass if the annotation is marked as `@Inherited`
- Nested inner classes do not inherit the parent class's annotations

## The two `AnnotationSupport.findAnnotation` methods

### `AnnotationSupport.findAnnotation(Optional<AnnotatedElement>, Class<A>)` AKA ***Method 1*** {#Method1}

***Method 1*** ([source code](https://github.com/junit-team/junit5/blob/732a5400f80c8f446daa8b43eaa4b41b3da929be/junit-platform-commons/src/main/java/org/junit/platform/commons/support/AnnotationSupport.java#L103))
finds an annotation of type `Class<A>` on the `AnnotatedElement`.
However, it will not search parent classes of `@Nested` tests,
and it will only search superclasses if an annotation is marked as `@Inherited`.

### `AnnotationSupport.findAnnotation(Class<?>, Class<A>, SearchOption)` AKA ***Method 2*** {#Method2}
***Method 2*** ([source code](https://github.com/junit-team/junit5/blob/732a5400f80c8f446daa8b43eaa4b41b3da929be/junit-platform-commons/src/main/java/org/junit/platform/commons/support/AnnotationSupport.java#L158))
finds an annotation of type `Class<A>` on the class in the 1st argument.
My guess is the method was created to address the shortcomings of ***Method 1***:
This method will find annotations on parent classes of `@Nested` tests if the `INCLUDE_ENCLOSING_CLASSES`
`SearchOption` is passed.
Similar to ***Method 1***, however, it only searches superclasses if the annotation is `@Inherited`.
An unfortunate aspect of this method:  It was only introduced in JUnit 5.8.0 and is ***EXPERIMENTAL***.

Here is a summary of these two methods:

| Method         | Finds superclass ann. if marked as inherited | Finds superclass ann. if NOT marked as inherited | Finds ann. on parent of @Nested class | Is well supported                  |     |
|----------------|----------------------------------------------|--------------------------------------------------|---------------------------------------|------------------------------------|-----|
| ***Method 1*** | Yes                                          | No                                               | No                                    | Yes (MAINTAINED status)            |     |
| ***Method 2*** | Yes                                          | No                                               | Optionally                            | No (EXPERIMENTAL status) since 5.8 |     |
*Note:  There is a [third method](https://github.com/junit-team/junit5/blob/732a5400f80c8f446daa8b43eaa4b41b3da929be/junit-platform-commons/src/main/java/org/junit/platform/commons/support/AnnotationSupport.java#L126),
but it is trivially different from ***method 1****

At first, the situation doesn't seem so bad:  Just mark your annotations as `@Inherited` and
use ***Method 2***.  That will work for your own projects, but it's a problem if you distribute your extensions.

There is the (not so) minor issue of requiring a relatively recent version of JUnit
(5.8 is just a year old) and using an EXPERIMENTAL API.
More significantly, while *you* can mark your annotations as `@Inherited`, your users can re-compose
them into their own annotations and may forget the `@Inherited` marker.
In fact, users may need to compose your annotation into an annotation that *cannot* be inherited.
If your extension breaks in this situation while others don't
(because they don't need configuration) it will be seen as a bug in your extension.

## Was the annotation on a method or class?

Another complication is that extensions implementing `BeforeEachCallback` and/or `AfterEachCallback`
are equally applicable to class or method level registration, thus, their associated annotation
could be marked as `@Target({ TYPE, METHOD })`.
When the extension's `beforeEach` and `afterEach` methods are called,
there is nothing to distinguish the two types of registrations,
so the extension code must search for a method annotation, check for null,
then try searching for a class annotation.

Its just one more challenge for extensions developers to potentially forget or get wrong.
In the `findPath` method example above, this is the reason the method will fail:
`context.getElement()` returns the method, not the class, even though the annotation was on the class.

## Determining which class was the annotated class

In the configurable usage example, e.g. `@SimpleAnn(classpathFile = "/MyFile.props")`,
the path used is a short, absolute path.
It would be useful to accept relative paths to make it easy to, for instance,
load a file named 'config.props' in the same package as the annotated class:

 ```java
package com.bigcorp.bigproject;
        
@SimpleAnn(classpathFile = "config.props") //results in file /com/bigcorp/bigproject/config.props
public class MyTestClass { /*  */ }
```

But how can an extension determine that?  As an extension developer, you would need to reimplement
and extend the existing `findAnnotation` methods to return the class on which the annotations were
found.  Yikes!

## Possible Solutions for Developers using *CAER*

### Option 1:  Use [Method 1](#Method1) + @Inherited {#Option1}
#### The Pros

- Easy w/ minimal code
- Works for many use cases
- Doesn't use an experimental API and likely works for all JUnit 5.X releases

#### The Cons
- Won't work at all for `@Nested` tests, which is a standard feature of Junit
- Users of your extension-annotation set will get errors if they re-compose your annotation and do
  not mark their annotation as `@Inherited`.  Your code could help users a bit:
  If the extension cannot find its annotation, the error message could include this as a possible cause.
- If the extension needs to find the actual annotated class (for relative classpath references),
  you will still need to reimplement and modify the AnnotationSupport code.

### Option 2:  Use the [Method 2](#Method2) + @Inherited {#Option2}
#### The Pros

- Easy w/ minimal code
- Works for many use cases including `@Nested` tests

#### The Cons
- Users will get compiler errors for pre-5.8.0 JUnit releases
- If [Method 2](#Method2) is removed in the release after 5.9.1, there would be compiler errors
  for newer versions as well (that is potentially a narrow band of known support).
- Like [Option 1](#Option1), re-composing the annotation without `@Inherited` will cause errors.
- Like [Option 1](#Option1), finding the annotated class will require added code.

### Option 3:  Reimplement the needed `findAnnotation` methods as part of your distributable
#### The Pros

- Can be made to work for all uses (`@Nested` tests as well as non-`@Inherited` annotations)
- Doesn't use an experimental API and can easily work for all JUnit 5.X releases
- It's easy to add the ability to find the annotated class, rather than just the annotation

#### The Cons
- It's [a lot of code](https://github.com/eeverman/junit-extension-examples/blob/main/annotation_support_tests/src/main/java/jextension/ExtensionUtil.java)
  to manage, test and distribute.

## Other ideas?

I'm open to suggestions and maybe even creating a separate library to provide this functionality.
Contact me (@eeverman) in the [JUnit gitter discussion](https://gitter.im/junit-team/junit5) channel.








```java
// In the properties file: 'other.props' file:
// phaser: stun

	@SimpleAnn(classpathFile = "/other.props")  
	public class SimpleExtTest {  
	  
	@Test  
	public void phaserShouldBeSetToStun() {  
		assertEquals("stun", System.getProperty("phaser"));  
	}  
}
```

However, things get difficult when the annotation is on a superclass:

```java

@SimpleAnn(classpathFile = "/other.props")  
public class InheritedTestBase {  /* Empty */ }

//

public class InheritedTest extends InheritedTestBase {  


	// FAILS WITH AN ERROR!!
	@Test  
	public void phaserShouldBeSetToStun() {  
		assertEquals("stun", System.getProperty("phaser"));  
	}  
}
```

It turns out that none of the `AnnotationSupport.findAnnotation` support method will find the annotation on the super class.  There is another possibility:  The annotation could be on a containing class, like this:

```java
@SimpleAnn  
public class NestedTest {  
  
  
	@Nested  
	class Nested1 {  
	@Test  
	public void phaserSetToStunViaOuterClassAnnotation(ExtensionContext context) {  
	// JUnit finds and applies the annotation, thus, the system property is set  
	assertEquals("stun", System.getProperty("phaser"));  
	}  
	
	
	}  
  
}
```

First, lets see a simple example of how the extension and annotation mechanism works:
```java
public class MyExtension implements BeforeEachCallback { ... }


@Target({ TYPE, METHOD, ANNOTATION_TYPE })  
@Retention(RUNTIME)  
@ExtendWith(MyExtension.class)  
public @interface MyAnnotation { ... }


@MyAnnotation
public MyTestClass { ... }
```

The example above is typical:
- Create a custom extension that implements a set of callback methods
- Create an annotation that will register that extension (because its easier to use than manual registration)
- Use the annotation, in this case on a test class, but it could be on a test method and/or other things

The example above (with some added details) will work just fine, but things get difficult when the extension takes arguments.  Since the extension is constructed by JUnit, there is no way to pass configuration to it.  The only place configuration can come from is the annotation.  Lets re-imagine the example above as an extension that reads properties from a file and does something with them - perhaps it sets the system properties based on them:
```java
public class ReadPropsExt implements BeforeEachCallback {

	@Override  
	public void beforeEach(final ExtensionContext context) {  
	  String path = findTheClassPathFile(context);
	  ... do something with the path ...
	}


	public void findTheClassPathFile(final ExtensionContext context) {  
	 //how do I find the annotation?? 
	}
}


@Target({ TYPE, METHOD, ANNOTATION_TYPE })  
@Retention(RUNTIME)  
@ExtendWith(MyExtension.class)  
public @interface ReadPropsAnnnotation {
	String path();
}


@ReadPropsAnnnotation(path = "propFile1.props")
public MyTestClass {

	@ReadPropsAnnnotation(path = "propFile2.props")
	@Test
	public void doTest();
}
```



The problems:
- The primary AnnotationSupport.findAnnotation method doesn't find inherited or nested annotation.
- The EXPERIMENTAL findAnnotation method can find nested annotations, but not inherited.
- None of the methods tell you what class the annotation is on
- Its impossible to tell if an extension was registered by an annotation on a method or class.  But perhaps it doesn't matter, since you can search the method first.

So, if you are using an annotation to register an extension ***and the extension needs to find the
annotation because the extension needs to discover its configuration***,
neither of the `findAnnotation()` will work for you.


Notes:
- Including a concept of distance would be helpful to differentiate ambiguous applications
- turn into more of a tutorial
- Does JUnit skip programatic registration of duplicate extensions?
  - How would the instance created programatically determined state if its shared?
    If all dup instances are ignored, it may be impossible to resolve the configured state
    of an instance created prog. vs the config available, say, on a method.
- Add best practices of using optionals to handle direct vs ann. creation.
- Checking method vs class annotation.

There is a key bug in the JUnit docs RE
[Extension Inheritance](https://junit.org/junit5/docs/current/user-guide/#extensions-registration-inheritance)
Is says:
Furthermore, a specific extension implementation can only be registered once for a given extension context and its parent contexts.
Consequently, any attempt to register a duplicate extension implementation will be ignored.
This is not true for Programmatic registration - each created class is registered.
See the ProgrammaticRegTest.