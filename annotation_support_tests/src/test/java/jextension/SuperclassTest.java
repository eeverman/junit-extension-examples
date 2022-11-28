package jextension;

import jextension.misc.ExtensionContextParamResolver;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.platform.commons.support.AnnotationSupport;
import org.junit.platform.commons.support.SearchOption;
import org.junitpioneer.internal.PioneerAnnotationUtils;

import static org.junit.jupiter.api.Assertions.*;

// The superclass is annotated w/ @SimpleAnn
@ExtendWith(ExtensionContextParamResolver.class)
public class SuperclassTest extends SuperclassTestBase {

	@Test  // This works, so JUnit finds and uses the annotation
	public void phaserSetToStunViaSuperClassAnnotation(ExtensionContext context) {
		assertEquals("stun", System.getProperty("phaser"));
	}

	@Test	// FAILS!!  Cannot find the annotation on the superclass
	public void findAnnotation1ShouldFindParentAnnotation(ExtensionContext context) {
		assertTrue(AnnotationSupport.findAnnotation(
				context.getRequiredTestClass(), SimpleAnn.class).isPresent());
	}

	@Test		// FAILS!! - Not even the EXPERIMENTAL method is enough to find it
	public void findAnnotation2ShouldFindParentAnnotation(ExtensionContext context) {
		assertTrue(AnnotationSupport.findAnnotation(
				context.getRequiredTestClass(), SimpleAnn.class, SearchOption.INCLUDE_ENCLOSING_CLASSES).isPresent());
	}

	@Test		//NEW PROPOSED METHOD - Works!
	public void findAnnotationForExtensionShouldFindParentAnnotation(ExtensionContext context) {
		assertTrue(ExtensionUtil.findAnnotationForExtension(
				context, SimpleAnn.class).isPresent());
	}

	@Test		//junit-pioneer
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
		public void findAnnotation1ShouldFindParentAnnotation(ExtensionContext context) {
			assertTrue(AnnotationSupport.findAnnotation(
					context.getRequiredTestClass(), SimpleAnn.class).isPresent());
		}

		@Test		// FAILS!!! Can't handle non-inherited annotations
		public void findAnnotation2ShouldFindParentAnnotation(ExtensionContext context) {
			assertTrue(AnnotationSupport.findAnnotation(
					context.getRequiredTestClass(), SimpleAnn.class, SearchOption.INCLUDE_ENCLOSING_CLASSES).isPresent());
		}

		@Test		//NEW PROPOSED METHOD - Works!
		public void findAnnotationForExtensionShouldFindParentAnnotation(ExtensionContext context) {
			assertTrue(ExtensionUtil.findAnnotationForExtension(
					context, SimpleAnn.class).isPresent());
		}

		@Test		//junit-pioneer
		public void findClosestEnclosingAnnotationShouldFindParentAnnotation(ExtensionContext context) {
			assertTrue(PioneerAnnotationUtils.findClosestEnclosingAnnotation(context, SimpleAnn.class).isPresent());
		}

	}

}
