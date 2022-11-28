package jextension;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Example of an annotation that a user might create that composes the
 * SimpleAnnInherited annotation (potentially with others).
 * SimpleAnnInherited is marked as Inherited, but this user annotation
 * is not.  The result is that none of the Annotation.findAnnotation methods
 * will find the SimpleAnnInherited annotation.
 */
@Target({ TYPE, METHOD, ANNOTATION_TYPE })
@Retention(RUNTIME)
@SimpleAnnInherited
public @interface ComposedFromSimpleAnnInherited { }
