# A first pass at a JUnit article
JUnit's [extension system](https://junit.org/junit5/docs/current/user-guide/#extensions) allows a lot of customization and it is best used via annotations.  JUnit's support linking annotations to the extensions they register, is however, lacking.

Here is an example of a JUnit extension that loads values from the ***MyFile.props*** properties file into the System.properties:

```java
public class SysPropExt implements BeforeEachCallback, AfterEachCallback {

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

There are a few different ways to register that extension for a test, but the most elegant is to create an annotation to do it:
```java
@Target({ TYPE, METHOD, ANNOTATION_TYPE })  
@Retention(RUNTIME)  
@ExtendWith(SysPropExt.class)  
public @interface SysPropAnn {  }

```

Then, you can easily use that annotation on a test class or method:
```java
@SysPropAnn
public MyTestClass { ... }
```

Good so far, but...
***How can we configure which file is loaded by the extension??***

Since JUnit creates the extension instance, there is no opportunity to pass arguments.  The solution is to pass the arguments to the annotation, then find them in the extension.  Using that looks natural, like this:
 ```java
@SysPropAnn(filepath = "/MyFile.props")
public MyTestClass { ... }
```

To wire that up, the annotation and extension have to change a bit.  We add a property to the annotation:
```java
@Target({ TYPE, METHOD, ANNOTATION_TYPE })  
@Retention(RUNTIME)  
@ExtendWith(SysPropExt.class)  
public @interface SysPropAnn {
	String filepath();
}
```

The extension needs to fish the value out of the annotation.  To do that, JUnit has a set of `AnnotationSupport.findAnnotation` methods that seem to be designed for it, as shown with the `findPath` method:

```java
public class SysPropExt implements BeforeEachCallback, AfterEachCallback {


	public String findPath(final ExtensionContext context) {  
		SysPropAnn ann = AnnotationSupport.findAnnotation(  
			context.getRequiredTestClass(), SysPropAnn.class).get();  
		return ann.filepath();  
	}  
  
	@Override  
	public void beforeEach(final ExtensionContext context) throws IOException {  
		Properties props = new Properties();  
		props.load(getClass().getResourceAsStream(findPath(context)));  
		System.setProperties(props);  
	} 
  
	public void afterEach(final ExtensionContext context) {  
		// reset the sys props ...
	}
}

```

This approach works well for basic tests, for instance, this test works:

```java
// In the properties file: 'other.props' file:
// phaser: stun

	@SysPropAnn(filepath = "/other.props")  
	public class SysPropExtTest {  
	  
	@Test  
	public void phaserShouldBeSetToStun() {  
		assertEquals("stun", System.getProperty("phaser"));  
	}  
}
```

However, things get difficult when the annotation is on a superclass:

```java

@SysPropAnn(filepath = "/other.props")  
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
@SysPropAnn  
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
	  String path = findTheFilePath(context);
	  ... do something with the path ...
	}


	public void findTheFilePath(final ExtensionContext context) {  
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

Note:  Good to add the detail that its not possible to know if beforeEach is annotated on the method or class.

The problems:
- The primary AnnotationSupport.findAnnotation method doesn't find inherited or nested annotation.
- The EXPERIMENTAL findAnnotation method can find nested annotations, but not inherited.
- None of the methods tell you what class the annotation is on
- Its impossible to tell if an extension was registered by an annotation on a method or class.  But perhaps it doesn't matter, since you can search the method first.

Possible Solutions for Developers creating Extensions registered with Annotations:
- To support extension registration in nested test classes, you could use the EXPERIMENTAL  AnnotationSupport.findAnnotation(Class, Class, SearchOption), but that is a questionable choice.  The JUnit docs have a lot of warnings about using such methods, and it locks your users into using JUnit 5.8.0 or newer (to get the introduction of junit-platform-commons 1.8 and the new findAnnotation method)
- To support inherited extension registration from superclasses, just mark all of your annotations with @Inherited.  Its not a terrible solution, but it is not technically correct and implies that the annotation is on the test class, which it is not.  This could cause issues in situations where there might need to be a relative classpath reference from the class the annotation was placed on.  And annotations can be bundled together if they are marked as @Target({ TYPE, ANNOTATION_TYPE }), and its possible that other annotations may not function correctly if forced to be @Inherited.
- Alternatively, selectively re-implement the needed code from AnnotationSupport and AnnotationUtils to create your own annotation method that always assumes inheritence.

