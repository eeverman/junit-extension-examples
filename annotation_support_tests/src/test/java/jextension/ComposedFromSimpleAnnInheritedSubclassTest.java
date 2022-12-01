package jextension;

import jextension.misc.ExtensionContextParamResolver;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.platform.commons.support.AnnotationSupport;
import org.junit.platform.commons.support.SearchOption;
import org.junitpioneer.internal.PioneerAnnotationUtils;
import org.junitpioneer.jupiter.ExpectedToFail;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

// Superclass is annotated w/ @UserAnn
@ExtendWith(ExtensionContextParamResolver.class)
public class ComposedFromSimpleAnnInheritedSubclassTest extends ComposedFromSimpleAnnInheritedTestSuperclass {

	@Test  // Works, so JUnit finds and uses the annotation
	public void phaserSetToStunViaSuperClassAnnotation(ExtensionContext context) {
		assertEquals("stun", System.getProperty("phaser"));
	}

	@Test	// Works - JUnit can find inherited annotations through non-Inherited composed ann.
	public void findAnnotation1ShouldFindParentAnnotation(ExtensionContext context) {
		assertTrue(AnnotationSupport.findAnnotation(
				context.getRequiredTestClass(), SimpleAnnInherited.class).isPresent());
	}

	@Test		// Works - JUnit can find inherited annotations
	public void findAnnotation2ShouldFindParentAnnotation(ExtensionContext context) {
		assertTrue(AnnotationSupport.findAnnotation(
				context.getRequiredTestClass(), SimpleAnnInherited.class, SearchOption.INCLUDE_ENCLOSING_CLASSES).isPresent());
	}

	@Test		//NEW PROPOSED METHOD - Works!
	public void findAnnotationForExtensionShouldFindParentAnnotation(ExtensionContext context) {
		assertTrue(ExtensionUtil.findAnnotationForExtension(
				context, SimpleAnnInherited.class).isPresent());
	}

	@Test		//Works - junit-pioneer can find inherited annotations
	public void findClosestEnclosingAnnotationShouldFindParentAnnotation(ExtensionContext context) {
		assertTrue(PioneerAnnotationUtils.findClosestEnclosingAnnotation(context, SimpleAnnInherited.class).isPresent());
	}

	@Nested
	class Nested1 {
		@Test  // Still works, so JUnit finds and uses the annotation
		public void phaserSetToStunViaSuperClassAnnotation(ExtensionContext context) {
			assertEquals("stun", System.getProperty("phaser"));
		}

		@Test	// FAILS!!! Can't handle nesting
		@ExpectedToFail("junit findAnnotation1 can't handle nesting")
		public void findAnnotation1ShouldFindParentAnnotation(ExtensionContext context) {
			assertTrue(AnnotationSupport.findAnnotation(
					context.getRequiredTestClass(), SimpleAnnInherited.class).isPresent());
		}

		@Test		// Works b/c its inherited and this method supports nesting
		public void findAnnotation2ShouldFindParentAnnotation(ExtensionContext context) {
			assertTrue(AnnotationSupport.findAnnotation(
					context.getRequiredTestClass(), SimpleAnnInherited.class, SearchOption.INCLUDE_ENCLOSING_CLASSES).isPresent());
		}

		@Test		//NEW PROPOSED METHOD - Works!
		public void findAnnotationForExtensionShouldFindParentAnnotation(ExtensionContext context) {
			assertTrue(ExtensionUtil.findAnnotationForExtension(
					context, SimpleAnnInherited.class).isPresent());
		}

		@Test		//junit-pioneer
		public void findClosestEnclosingAnnotationShouldFindParentAnnotation(ExtensionContext context) {
			assertTrue(PioneerAnnotationUtils.findClosestEnclosingAnnotation(context, SimpleAnnInherited.class).isPresent());
		}

	}
}
