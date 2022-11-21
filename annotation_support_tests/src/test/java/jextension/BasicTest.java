package jextension;

import jextension.misc.ExtensionContextParamResolver;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.platform.commons.support.AnnotationSupport;
import org.junit.platform.commons.support.SearchOption;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(ExtensionContextParamResolver.class)
@SimpleAnn
public class BasicTest {

	@Test  // This works as expected
	public void phaserSetToStunViaSuperClassAnnotation(ExtensionContext context) {
		assertEquals("stun", System.getProperty("phaser"));
	}

	@Test	// Works as intended
	public void findAnnotation1ShouldFindParentAnnotation(ExtensionContext context) {
		assertTrue(AnnotationSupport.findAnnotation(
				context.getRequiredTestClass(), SimpleAnn.class).isPresent());
	}

	@Test		// Works as intended
	public void findAnnotation2ShouldFindParentAnnotation(ExtensionContext context) {
		assertTrue(AnnotationSupport.findAnnotation(
				context.getRequiredTestClass(), SimpleAnn.class, SearchOption.INCLUDE_ENCLOSING_CLASSES).isPresent());
	}

	@Test		//NEW PROPOSED METHOD - Works!
	public void findAnnotationForExtensionShouldFindParentAnnotation(ExtensionContext context) {
		assertTrue(ExtensionUtil.findAnnotationForExtension(
				context, SimpleAnn.class).isPresent());
	}
}