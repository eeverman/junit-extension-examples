package characterization;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Characterization test to understand how JUnit handles registration of the same
 * class and same instance of extensions.
 *
 * See the superclass ProgrammaticRegBase for some additional registrations of the
 * SimpleExt extension.
 */
public class ProgrammaticRegTest extends ProgrammaticRegBase {

	@RegisterExtension
	public static SimpleExt extTestStatic = new SimpleExt("Test Static");

	@RegisterExtension
	public SimpleExt extTestInstance = new SimpleExt("Test Instance");

	/**
	 * Despite what the docs say, each programmatic registration of the SimpleExt
	 * extension happens, resulting in 4 instances of the same extension class in
	 * this test.
	 */
	@Test
	public void eachProgrammaticRegisteredSimpleExtDoesReceiveEvents() {

		List<String> exts = SimpleExt.getBeforeInvocations();
		assertEquals("Super Static", exts.get(0));
		assertEquals("Test Static", exts.get(1));
		assertEquals("Super Instance", exts.get(2));
		assertEquals("Test Instance", exts.get(3));

		assertEquals(4, exts.size());
	}

	/**
	 * Declarative registration of the same class is ignored.
	 */
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

	/**
	 * With nested tests, we can add even more registrations of the same extension
	 * class.
	 */
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

	/**
	 * In this nested test, we add registrations of the SAME INSTANCE.
	 */
	@Nested
	class NestedProgrammaticRegistrationWithDuplicateInstances {
		@RegisterExtension
		public static SimpleExt extNestStatic = extTestStatic;	//Register the same INSTANCE AGAIN!!

		@RegisterExtension
		public SimpleExt extNestInstance = extTestInstance;	//Register the same INSTANCE AGAIN!!

		@RegisterExtension
		public SimpleExt extNestInstance2 = extTestInstance;	//Register the same INSTANCE AGAIN, AGAIN!!

		@Test
		public void possibleToAddSameExtInstanceMultipleTimes() {

			List<String> exts = SimpleExt.getBeforeInvocations();
			assertEquals("Super Static", exts.get(0));
			assertEquals("Test Static", exts.get(1));
			assertEquals("Test Static", exts.get(2));	//WHAT!!
			assertEquals("Super Instance", exts.get(3));
			assertEquals("Test Instance", exts.get(4));
			assertEquals("Test Instance", exts.get(5));	//WHAT!!
			assertEquals("Test Instance", exts.get(6));	//WHAT!!

			assertEquals(7, exts.size());
		}
	}

	/**
	 * Declarative registration is still ignored with nested tests.
	 */
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
