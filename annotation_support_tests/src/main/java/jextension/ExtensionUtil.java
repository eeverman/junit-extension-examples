package jextension;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.platform.commons.support.AnnotationSupport;
import org.junit.platform.commons.util.Preconditions;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Modifier;
import java.util.*;

public class ExtensionUtil {

	private ExtensionUtil() {
		/* NO OP - no instances */
	}


	public static <A extends Annotation> Optional<A> findAnnotationForExtension(ExtensionContext context, Class<A> annotationType) {

		Optional<A> annInstance = Optional.empty();

		// Don't know if this annotation is on a method or class - it may even be on both!
		// Searching the method first:  Most annotation users would consider a test method ann. to
		// naturally 'override' a class annotation, even if the JUnit extension system doesn't work that way.

		// Look for the annotation on the method, if present.
		if (context.getTestMethod().isPresent()) {
			annInstance = AnnotationSupport.findAnnotation(Optional.of(context.getRequiredTestMethod()), annotationType);
		}

		// Look for the annotation on the class, if present.
		if (! annInstance.isPresent() && context.getTestClass().isPresent()) {

			// Use AnnotationSupport.findAnnotation to check for the simple cases (directly present annotations)
			annInstance = AnnotationSupport.findAnnotation(Optional.of(context.getRequiredTestClass()), annotationType);

			if (! annInstance.isPresent()) {
				// Now try searching inherited and nested annotations on classes

				annInstance = ExtensionUtil.findAnnotation(context.getRequiredTestClass(), annotationType);

			}
		}

		return annInstance;
	}


	/**
	 * Copied from JUnit AnnotationUtils
	 *
	 * ref:  AnnotationUtils.findAnnotation(Class<?> clazz, Class<A> annotationType,
	 * 			boolean searchEnclosingClasses)
	 * @param clazz
	 * @param annotationType
	 * @return
	 * @param <A>
	 */
	public static <A extends Annotation> Optional<A> findAnnotation(Class<?> clazz, Class<A> annotationType) {

		Class<?> candidate = clazz;
		while (candidate != null) {
			Optional<A> annotation = findAnnotationAssumeInheritence(candidate, annotationType);
			if (annotation.isPresent()) {
				return annotation;
			}

			// EE:  getEnclosingClass() returns null if top-level, so the logic is unneeded.
			candidate = (isInnerClass(candidate) ? candidate.getEnclosingClass() : null);
		}
		return Optional.empty();
	}

	/**
	 * From JUnit AnnotationUtils.findAnnotation(AnnotatedElement element, Class<A> annotationType).
	 * Modified to assume inheritence, which is how JUnit behaves WRT the effect of annotations.
	 * @param element
	 * @param annotationType
	 * @return
	 * @param <A>
	 */
	public static <A extends Annotation> Optional<A> findAnnotationAssumeInheritence(AnnotatedElement element, Class<A> annotationType) {
		Preconditions.notNull(annotationType, "annotationType must not be null");
		return findAnnotation(element, annotationType, true, new HashSet<>());
	}


	/**
	 * From JUnit AnnotationUtils.findAnnotation(AnnotatedElement element, Class<A> annotationType,
	 * 			boolean inherited, Set<Annotation> visited)
	 * @param element
	 * @param annotationType
	 * @param inherited
	 * @param visited
	 * @return
	 * @param <A>
	 */
	private static <A extends Annotation> Optional<A> findAnnotation(AnnotatedElement element, Class<A> annotationType,
			boolean inherited, Set<Annotation> visited) {

		Preconditions.notNull(annotationType, "annotationType must not be null");

		if (element == null) {
			return Optional.empty();
		}

		// Directly present?
		A annotation = element.getDeclaredAnnotation(annotationType);
		if (annotation != null) {
			return Optional.of(annotation);
		}

		// Meta-present on directly present annotations?
		Optional<A> directMetaAnnotation = findMetaAnnotation(annotationType, element.getDeclaredAnnotations(),
				inherited, visited);
		if (directMetaAnnotation.isPresent()) {
			return directMetaAnnotation;
		}

		if (element instanceof Class) {
			Class<?> clazz = (Class<?>) element;

			// Search on interfaces
			for (Class<?> ifc : clazz.getInterfaces()) {
				if (ifc != Annotation.class) {
					Optional<A> annotationOnInterface = findAnnotation(ifc, annotationType, inherited, visited);
					if (annotationOnInterface.isPresent()) {
						return annotationOnInterface;
					}
				}
			}

			// Indirectly present?
			// Search in class hierarchy
			if (inherited) {
				Class<?> superclass = clazz.getSuperclass();
				if (superclass != null && superclass != Object.class) {
					Optional<A> annotationOnSuperclass = findAnnotation(superclass, annotationType, inherited, visited);
					if (annotationOnSuperclass.isPresent()) {
						return annotationOnSuperclass;
					}
				}
			}
		}

		// Meta-present on indirectly present annotations?
		return findMetaAnnotation(annotationType, element.getAnnotations(), inherited, visited);
	}

	/**
	 * From JUnit AnnotationUtils.findAnnotation(AnnotatedElement element, Class<A> annotationType,
	 * 			boolean inherited, Set<Annotation> visited)
	 * @param annotationType
	 * @param candidates
	 * @param inherited
	 * @param visited
	 * @return
	 * @param <A>
	 */
	private static <A extends Annotation> Optional<A> findMetaAnnotation(Class<A> annotationType,
			Annotation[] candidates, boolean inherited, Set<Annotation> visited) {

		for (Annotation candidateAnnotation : candidates) {
			Class<? extends Annotation> candidateAnnotationType = candidateAnnotation.annotationType();
			if (!isInJavaLangAnnotationPackage(candidateAnnotationType) && visited.add(candidateAnnotation)) {
				Optional<A> metaAnnotation = findAnnotation(candidateAnnotationType, annotationType, inherited,
						visited);
				if (metaAnnotation.isPresent()) {
					return metaAnnotation;
				}
			}
		}
		return Optional.empty();
	}

	private static boolean isInJavaLangAnnotationPackage(Class<? extends Annotation> annotationType) {
		return (annotationType != null && annotationType.getName().startsWith("java.lang.annotation"));
	}

	/**
	 * Is this a non-static innerClass?
	 *
	 * @param clazz
	 * @return
	 */
	public static boolean isInnerClass(Class<?> clazz) {
		return !(Modifier.isStatic(clazz.getModifiers())) && clazz.isMemberClass();
	}


}
