package jextension;

import jextension.misc.ExtensionContextParamResolver;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.platform.commons.support.AnnotationSupport;
import org.junit.platform.commons.support.SearchOption;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Uses the @ConfigAnn and ConfigExt (which uses ExtensionUtil) to test which annotation
 * ExtensionUtil finds.  Since the @ConfigAnn takes a filepath config parameter,
 * tests can show which value was loaded.
 */
@ExtendWith(ExtensionContextParamResolver.class)
@ConfigAnn(filepath = "/MyFile.props")
public class ExtensionUtilTest {

	@Test  // 'stun' value found in MyFile.props
	public void classLevelAnnotationShouldSetPhaserToStun(ExtensionContext context) {
		assertEquals("stun", System.getProperty("phaser"));
	}

	@ConfigAnn(filepath = "/other.props")
	@Test		// 'entertain' value in the other.props file
	public void methodLevelAnnotationShouldSetPhaserToEntertain(ExtensionContext context) {
		assertEquals("entertain", System.getProperty("phaser"));
	}

	@Nested
	class Nested1 {
		@Test  // parent class's configuration should be used here
		public void classLevelAnnotationShouldSetPhaserToStun(ExtensionContext context) {
			assertEquals("stun", System.getProperty("phaser"));
		}
	}

	@ConfigAnn(filepath = "/other.props")
	@Nested
	class Nested2 {
		@Test		// 'entertain' value in the other.props file from the Nested2 class
		public void methodLevelAnnotationShouldSetPhaserToEntertain(ExtensionContext context) {
			assertEquals("entertain", System.getProperty("phaser"));
		}

		@ConfigAnn(filepath = "/MyFile.props")
		@Test  // Override at the method level
		public void classLevelAnnotationShouldSetPhaserToStun(ExtensionContext context) {
			assertEquals("stun", System.getProperty("phaser"));
		}
	}
}