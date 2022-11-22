# JUnit Extension Registration via Composed Annotation - Almost (but not quite) workable

JUnit's extension system includes a natural way to register extensions via annotations (See JUnit docs
[Declarative Extension Registration / composed annotations section](https://junit.org/junit5/docs/current/user-guide/#extensions-registration-declarative)].
While this system works well for simple extensions, it is not possible to use it with
extensions that require configuration without resorting to some hacky solutions.

## Basics of Composed Annotation Extension Registration

If you are not familiar with composed annotation extension registration, here is a quick example.
Below is a simple extension that loads key-value pairs from the ***MyFile.props***
properties file into `System.properties`:

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

There are multiple ways to register the extension, but the most elegant is via a composed annotation:
```java
@Target({ TYPE, METHOD, ANNOTATION_TYPE })  
@Retention(RUNTIME)  
@ExtendWith(SimpleExt.class)  // Causes SimpleExt to be registered wherever @SimpleAnn is used
public @interface SimpleAnn {  }
```

Then, you can easily use that annotation on a test class or method:
```java
@SimpleAnn
public class MyTestClass { /* SimpleExt in use in this test class */ }
```

## Adding Configuration to an annotation registered extension

The example above is elegant and easy, but...
***How can we configure which file is loaded, rather than hard-code the file??***

Since JUnit creates the extension instance for us, there is no opportunity to pass arguments.
The solution is to pass the arguments to the annotation, then find the annotation and its arguments
in the extension.  This makes it easy for the users of the extension, and usage looks natural:
 ```java
@SimpleAnn(classpathFile = "/MyFile.props")
public class MyTestClass { /*  */ }
```

To do that, the annotation just needs a single extra line to declare a classpathFile property:
```java
@Target({ TYPE, METHOD, ANNOTATION_TYPE })  
@Retention(RUNTIME)  
@ExtendWith(SimpleExt.class)  
public @interface SimpleAnn {
	String classpathFile();		// Added
}
```

The extension will need to find the annotation to grab the value, but how?
Junit includes two different `AnnotationSupport.findAnnotation()` methods that seem to be designed
for the task.  If they worked for this purpose, our extension could look like this:
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

The source of the issue is that effective scope of JUnit extensions is different than the scope of
Java annotations, and the `findAnnotation()` methods tend to follow the Java model.
The scope of a Junit extension follows these rules:
- An extension registered on a superclass applies to its subclass
- An extension registered on a parent class applies to all `@Nested` test classes


By contrast in Java:
- Annotations on a superclass are only applicable to a subclass if the annotation is marked as `@Inherited`.
- Nested inner classes don't really have a concept of inheriting the parent class's annotations.

So, if you are using an annotation to register an extension ***and the extension needs to find the
annotation because the extension needs to discover its configuration***, 
neither of the `findAnnotation()` will work for you.

## The two `AnnotationSupport.findAnnotation` methods

### `AnnotationSupport.findAnnotation(AnnotatedElement, Class<A>)` AKA ***Method 1*** {#Method1}
This method finds an annotation of type `Class<A>` on the `AnnotatedElement`,
but does not search parent classes of `@Nested` tests.
It will search superclasses, but only for annotations marked `@Inherited`.

### `AnnotationSupport.findAnnotation(Class<?>, Class<A>, SearchOption)` AKA ***Method 2*** {#Method2}
This method finds an annotation of type `Class<A>` on the class in the 1st argument.
My guess is that this method was created to address the shortcomings of ***Method 1***:
This method will find annotations on parents of `@Nested` tests if the `INCLUDE_ENCLOSING_CLASSES`
`SearchOption` is passed.
Similar to method 1, however, it only searches superclasses if the annotation is `@Inherited`.
Another unfortunate aspect of this method:  It was only introduced in
JUnit 5.8.0 and is marked as ***EXPERIMENTAL***.

Here is a summary of these two methods:

| Method         | Finds superclass ann. if marked as inherited | Finds superclass ann. if NOT marked as inherited | Finds ann. on parent of @Nested class | Is well supported                  |     |
|----------------|----------------------------------------------|--------------------------------------------------|---------------------------------------|------------------------------------|-----|
| ***Method 1*** | Yes                                          | No                                               | No                                    | Yes (MAINTAINED status)            |     |
| ***Method 2*** | Yes                                          | No                                               | Optionally                            | No (EXPERIMENTAL status) since 5.8 |     |
*Note:  There is a third method, but it is effectively the same as **method 1***


At first glance, the situation doesn't seem so bad:  Just mark your annotations as `@Inherited` and
use ***Method 2***.  But this is a problem if you intend to distribute your extensions.
There is the issue of requiring a relatively recent version of JUnit (5.8 was released just a year ago)
and the use of an ***EXPERIMENTAL*** API.

More importantly, while *you* can mark your annotations as `@Inherited`, your users can re-compose
them into their own annotations and likely would not include the `@Inherited` marker.
It is possible that users will want to compose your annotation into a new one that *cannot* be inherited.
That fact that your extensions break in this situation while others don't
(because they don't need configuration) would be seen as a bug with your extensions.

## Which class was the annotated class?

Let's go back to the example of an extension that takes a `classpathFile` configuration. 
The example conveniently used a short, absolute path, `/MyFile.props`.
A reasonable and useful feature would be to accept paths relative to the annotation file.
For instance, to load a file named 'config.props' in the same package as the class:

 ```java
@SimpleAnn(classpathFile = "config.props")
public class MyTestClass { /*  */ }
```

But how can an extension determine that?  As an extension developer, you would need to reimplement
and extend the existing `findAnnotation` methods to return the class on which the annotations were
found.  Yikes!

## Was the annotation on a method or class?

Extensions that implement the `BeforeEachCallback` and `AfterEachCallback` are equally applicable
to a class and method level registration, thus, their associated annotation could be marked
as `@Target({ TYPE, METHOD })`.  When the extension's `beforeEach` and `afterEach` methods are called,
there is nothing to distinguish the two types of registrations,
so the extension code must search for a method annotation, check for null,
then try searching for a class annotation.
Its just one more challenge for extensions developers to potentially forget or get wrong.

## Possible Solutions for Developers creating Extensions registered with Annotations
- To support extension registration in nested test classes, you could use the ***EXPERIMENTAL***
  ***Method 1*** AnnotationSupport.findAnnotation(Class, Class, SearchOption), but that is a questionable choice.
  The JUnit docs have a lot of warnings about using such methods,
  and it locks your users into using JUnit 5.8.0 or newer.
- To support inherited extension registration from superclasses, you could mark all of your
  annotations with `@Inherited`. 
  Its not a terrible solution, but it is not technically correct and implies that the annotation
  is on the test class, which it is not.  This could cause issues in situations where there might
  need to be a relative classpath reference from the class the annotation was placed on.
  And annotations can be bundled together if they are marked as `@Target({ TYPE, ANNOTATION_TYPE })`,
  and its possible that other annotations may not function correctly if forced to be @Inherited.
- Alternatively, selectively re-implement the needed code from JUnit's `AnnotationSupport` and
  `AnnotationUtils` to create your own annotation method that always assumes inheritence.








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

Notes:
- Good to add the detail that its not possible to know if beforeEach is annotated on the method or class.
- Including a concept of distance would be helpful to differentiate ambiguous applications

The problems:
- The primary AnnotationSupport.findAnnotation method doesn't find inherited or nested annotation.
- The EXPERIMENTAL findAnnotation method can find nested annotations, but not inherited.
- None of the methods tell you what class the annotation is on
- Its impossible to tell if an extension was registered by an annotation on a method or class.  But perhaps it doesn't matter, since you can search the method first.

