package ann_support;

import ann_support.misc.ExtensionContextParamResolver;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.platform.commons.support.AnnotationSupport;
import org.junit.platform.commons.support.SearchOption;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(ExtensionContextParamResolver.class)
@SimpleAnn
public class NestedTest {

	@Nested
	class Nested1 {
		@Test		// This works, so JUnit properly applies the nested annotation
		public void phaserSetToStunViaOuterClassAnnotation(ExtensionContext context) {
			assertEquals("stun", System.getProperty("phaser"));
		}

		@Test	// FAILS!!  This method cannot handle nesting
		public void findAnnotation1ShouldFindParentAnnotation(ExtensionContext context) {
			assertTrue(AnnotationSupport.findAnnotation(
					context.getRequiredTestClass(), SimpleAnn.class).isPresent());
		}

		@Test	//Only method to work, unfortunately its EXPERIMENTAL
		public void findAnnotation2ShouldFindParentAnnotation(ExtensionContext context) {
			assertTrue(AnnotationSupport.findAnnotation(
					context.getRequiredTestClass(), SimpleAnn.class, SearchOption.INCLUDE_ENCLOSING_CLASSES).isPresent());
		}

	}

}
