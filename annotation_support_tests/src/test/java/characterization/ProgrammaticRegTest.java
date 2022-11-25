package characterization;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class ProgrammaticRegTest extends ProgrammaticRegBase {

	@RegisterExtension
	public static SimpleExt extTestStatic = new SimpleExt("Test Static");

	@RegisterExtension
	public SimpleExt extTestInstance = new SimpleExt("Test Instance");

	@Test
	public void eachProgrammaticRegisteredSimpleExtDoesReceiveEvents() {

		List<SimpleExt> exts = SimpleExt.getInstances();
		assertEquals("Super Static", exts.get(0).getName());
		assertEquals("Test Static", exts.get(1).getName());
		assertEquals("Super Instance", exts.get(2).getName());
		assertEquals("Test Instance", exts.get(3).getName());

		assertEquals(4, exts.size());
	}

	@ExtendWith(SimpleExt.class)
	@SimpleAnn(name = "MyName")
	@Test
	public void declarativeAnnotationIsNotAddedAfterProgrammaticRegOfSameClass() {

		List<SimpleExt> exts = SimpleExt.getInstances();
		assertEquals("Super Static", exts.get(0).getName());
		assertEquals("Test Static", exts.get(1).getName());
		assertEquals("Super Instance", exts.get(2).getName());
		assertEquals("Test Instance", exts.get(3).getName());

		assertEquals(4, exts.size());
	}

	@Nested
	class NestedTest {
		@RegisterExtension
		public static SimpleExt extNestStatic = new SimpleExt("Nested Static");

		@RegisterExtension
		public SimpleExt extNestInstance = new SimpleExt("Nested Instance");

		@Test
		public void eachProgrammaticRegisteredSimpleExtDoesReceiveEvents() {

			List<SimpleExt> exts = SimpleExt.getInstances();
			assertEquals("Super Static", exts.get(0).getName());
			assertEquals("Test Static", exts.get(1).getName());
			assertEquals("Nested Static", exts.get(2).getName());
			assertEquals("Super Instance", exts.get(3).getName());
			assertEquals("Test Instance", exts.get(4).getName());
			assertEquals("Nested Instance", exts.get(5).getName());

			assertEquals(6, exts.size());
		}
	}

	@ExtendWith(SimpleExt.class)
	@SimpleAnn(name = "MyName")
	@Nested
	class NestedTest2 {

		@ExtendWith(SimpleExt.class)
		@SimpleAnn(name = "MyName")
		@Test
		public void eachProgrammaticRegisteredSimpleExtDoesReceiveEvents() {

			List<SimpleExt> exts = SimpleExt.getInstances();
			assertEquals("Super Static", exts.get(0).getName());
			assertEquals("Test Static", exts.get(1).getName());
			assertEquals("Super Instance", exts.get(2).getName());
			assertEquals("Test Instance", exts.get(3).getName());

			assertEquals(4, exts.size());
		}
	}
}
