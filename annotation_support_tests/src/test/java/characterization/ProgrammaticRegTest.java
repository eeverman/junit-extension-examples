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

		List<String> exts = SimpleExt.getBeforeInvocations();
		assertEquals("Super Static", exts.get(0));
		assertEquals("Test Static", exts.get(1));
		assertEquals("Super Instance", exts.get(2));
		assertEquals("Test Instance", exts.get(3));

		assertEquals(4, exts.size());
	}

	@ExtendWith(SimpleExt.class)
	@SimpleAnn(name = "MyName")
	@Test
	public void declarativeAnnotationIsNotAddedAfterProgrammaticRegOfSameClass() {

		List<String> exts = SimpleExt.getBeforeInvocations();
		assertEquals("Super Static", exts.get(0));
		assertEquals("Test Static", exts.get(1));
		assertEquals("Super Instance", exts.get(2));
		assertEquals("Test Instance", exts.get(3));

		assertEquals(4, exts.size());
	}

	@Nested
	class NestedProgrammaticRegistration {
		@RegisterExtension
		public static SimpleExt extNestStatic = new SimpleExt("Nested Static");

		@RegisterExtension
		public SimpleExt extNestInstance = new SimpleExt("Nested Instance");

		@Test
		public void nestedProgrammaticRegistrationIsAddedOn() {

			List<String> exts = SimpleExt.getBeforeInvocations();
			assertEquals("Super Static", exts.get(0));
			assertEquals("Test Static", exts.get(1));
			assertEquals("Nested Static", exts.get(2));
			assertEquals("Super Instance", exts.get(3));
			assertEquals("Test Instance", exts.get(4));
			assertEquals("Nested Instance", exts.get(5));

			assertEquals(6, exts.size());
		}
	}

	@Nested
	class NestedProgrammaticRegistrationWithDuplicateInstances {
		@RegisterExtension
		public static SimpleExt extNestStatic = extTestStatic;	//Register the same INSTANCE AGAIN!!

		@RegisterExtension
		public SimpleExt extNestInstance = extTestInstance;	//Register the same INSTANCE AGAIN!!

		@Test
		public void nestedProgrammaticRegistrationIsAddedOn() {

			List<String> exts = SimpleExt.getBeforeInvocations();
			assertEquals("Super Static", exts.get(0));
			assertEquals("Test Static", exts.get(1));
			assertEquals("Test Static", exts.get(2));	//WHAT!!
			assertEquals("Super Instance", exts.get(3));
			assertEquals("Test Instance", exts.get(4));
			assertEquals("Test Instance", exts.get(5));	//WHAT!!

			assertEquals(6, exts.size());
		}
	}

	@ExtendWith(SimpleExt.class)
	@SimpleAnn(name = "MyName")
	@Nested
	class NestedTestWithDeclarativeRegs {

		@ExtendWith(SimpleExt.class)
		@SimpleAnn(name = "MyName")
		@Test
		public void stillIgnoreDeclarativeRegistrations() {

			List<String> exts = SimpleExt.getBeforeInvocations();
			assertEquals("Super Static", exts.get(0));
			assertEquals("Test Static", exts.get(1));
			assertEquals("Super Instance", exts.get(2));
			assertEquals("Test Instance", exts.get(3));

			assertEquals(4, exts.size());
		}
	}
}
