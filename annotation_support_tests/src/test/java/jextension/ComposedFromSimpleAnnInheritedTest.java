package jextension;

import jextension.misc.ExtensionContextParamResolver;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.platform.commons.support.AnnotationSupport;
import org.junit.platform.commons.support.SearchOption;
import org.junitpioneer.internal.PioneerAnnotationUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

// Superclass is annotated w/ @UserAnn
@ExtendWith(ExtensionContextParamResolver.class)
public class ComposedFromSimpleAnnInheritedTest extends ComposedFromSimpleAnnInheritedTestSuperclass {

	@Test  // Works, so JUnit finds and uses the annotation
	public void phaserSetToStunViaSuperClassAnnotation(ExtensionContext context) {
		assertEquals("stun", System.getProperty("phaser"));
	}

	@Test	// FAILS!! Because even those SimpleAnnInherited is inherited, UserAnn is not.
	public void findAnnotation1ShouldFindParentAnnotation(ExtensionContext context) {
		assertTrue(AnnotationSupport.findAnnotation(
				context.getRequiredTestClass(), ComposedFromSimpleAnnInherited.class).isPresent());
	}

	@Test		// FAILS!! Because even those SimpleAnnInherited is inherited, UserAnn is not.
	public void findAnnotation2ShouldFindParentAnnotation(ExtensionContext context) {
		assertTrue(AnnotationSupport.findAnnotation(
				context.getRequiredTestClass(), ComposedFromSimpleAnnInherited.class, SearchOption.INCLUDE_ENCLOSING_CLASSES).isPresent());
	}

	@Test		//NEW PROPOSED METHOD - Works!
	public void findAnnotationForExtensionShouldFindParentAnnotation(ExtensionContext context) {
		assertTrue(ExtensionUtil.findAnnotationForExtension(
				context, ComposedFromSimpleAnnInherited.class).isPresent());
	}

	@Test		//junit-pioneer
	public void findClosestEnclosingAnnotationShouldFindParentAnnotation(ExtensionContext context) {
		assertTrue(PioneerAnnotationUtils.findClosestEnclosingAnnotation(context, ComposedFromSimpleAnnInherited.class).isPresent());
	}




	@Nested
	class Nested1 {
		@Test  // Still works, so JUnit finds and uses the annotation
		public void phaserSetToStunViaSuperClassAnnotation(ExtensionContext context) {
			assertEquals("stun", System.getProperty("phaser"));
		}

		@Test	// FAILS!!! Can't handle nesting or non-inherited annotations
		public void findAnnotation1ShouldFindParentAnnotation(ExtensionContext context) {
			assertTrue(AnnotationSupport.findAnnotation(
					context.getRequiredTestClass(), ComposedFromSimpleAnnInherited.class).isPresent());
		}

		@Test		// FAILS!!! Can't handle non-inherited annotations
		public void findAnnotation2ShouldFindParentAnnotation(ExtensionContext context) {
			assertTrue(AnnotationSupport.findAnnotation(
					context.getRequiredTestClass(), ComposedFromSimpleAnnInherited.class, SearchOption.INCLUDE_ENCLOSING_CLASSES).isPresent());
		}

		@Test		//NEW PROPOSED METHOD - Works!
		public void findAnnotationForExtensionShouldFindParentAnnotation(ExtensionContext context) {
			assertTrue(ExtensionUtil.findAnnotationForExtension(
					context, ComposedFromSimpleAnnInherited.class).isPresent());
		}

		@Test		//junit-pioneer
		public void findClosestEnclosingAnnotationShouldFindParentAnnotation(ExtensionContext context) {
			assertTrue(PioneerAnnotationUtils.findClosestEnclosingAnnotation(context, ComposedFromSimpleAnnInherited.class).isPresent());
		}

	}
}
