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

// Superclass is annotated w/ @ComposedFromSimpleAnn
@ExtendWith(ExtensionContextParamResolver.class)
public class ComposedFromSimpleAnnSubclassTest extends ComposedFromSimpleAnnTestSuperclass {

	@Test  // Works, so JUnit finds and uses the annotation
	public void phaserSetToStunViaSuperClassAnnotation(ExtensionContext context) {
		assertEquals("stun", System.getProperty("phaser"));
	}

	@Test	// FAILS!! Because even those SimpleAnnInherited is inherited, UserAnn is not.
	@ExpectedToFail("junit findAnnotation1 can't find ann. on super b/c it is not @Inherited")
	public void findAnnotation1ShouldFindParentAnnotation(ExtensionContext context) {
		assertTrue(AnnotationSupport.findAnnotation(
				context.getRequiredTestClass(), SimpleAnn.class).isPresent());
	}

	@Test		// FAILS!! Because even those SimpleAnnInherited is inherited, UserAnn is not.
	@ExpectedToFail("junit findAnnotation2 can't find ann. on super b/c it is not @Inherited")
	public void findAnnotation2ShouldFindParentAnnotation(ExtensionContext context) {
		assertTrue(AnnotationSupport.findAnnotation(
				context.getRequiredTestClass(), SimpleAnn.class, SearchOption.INCLUDE_ENCLOSING_CLASSES).isPresent());
	}

	@Test		//NEW PROPOSED METHOD - Works!
	public void findAnnotationForExtensionShouldFindParentAnnotation(ExtensionContext context) {
		assertTrue(ExtensionUtil.findAnnotationForExtension(
				context, SimpleAnn.class).isPresent());
	}

	@Test		//FAILS!! - junit-pioneer - Can't handle non-inherited annotations
	@ExpectedToFail("pioneer findClosestEnclosingAnnotation can't find ann. on super b/c it is not @Inherited")
	public void findClosestEnclosingAnnotationShouldFindParentAnnotation(ExtensionContext context) {
		assertTrue(PioneerAnnotationUtils.findClosestEnclosingAnnotation(context, SimpleAnn.class).isPresent());
	}


	@Nested
	class Nested1 {
		@Test  // Still works, so JUnit finds and uses the annotation
		public void phaserSetToStunViaSuperClassAnnotation(ExtensionContext context) {
			assertEquals("stun", System.getProperty("phaser"));
		}

		@Test	// FAILS!!! Can't handle nesting or non-inherited annotations
		@ExpectedToFail("junit findAnnotation1 can't find ann. on super b/c it is not @Inherited & can't nest")
		public void findAnnotation1ShouldFindParentAnnotation(ExtensionContext context) {
			assertTrue(AnnotationSupport.findAnnotation(
					context.getRequiredTestClass(), SimpleAnn.class).isPresent());
		}

		@Test		// FAILS!!! Can't handle non-inherited annotations
		@ExpectedToFail("junit findAnnotation2 can't find ann. on super b/c it is not @Inherited")
		public void findAnnotation2ShouldFindParentAnnotation(ExtensionContext context) {
			assertTrue(AnnotationSupport.findAnnotation(
					context.getRequiredTestClass(), SimpleAnn.class, SearchOption.INCLUDE_ENCLOSING_CLASSES).isPresent());
		}

		@Test		//NEW PROPOSED METHOD - Works!
		public void findAnnotationForExtensionShouldFindParentAnnotation(ExtensionContext context) {
			assertTrue(ExtensionUtil.findAnnotationForExtension(
					context, SimpleAnn.class).isPresent());
		}

		@Test		//FAILS!! - junit-pioneer - Can't handle non-inherited annotations
		@ExpectedToFail("pioneer findClosestEnclosingAnnotation can't find ann. on super b/c it is not @Inherited")
		public void findClosestEnclosingAnnotationShouldFindParentAnnotation(ExtensionContext context) {
			assertTrue(PioneerAnnotationUtils.findClosestEnclosingAnnotation(context, SimpleAnn.class).isPresent());
		}

	}
}
