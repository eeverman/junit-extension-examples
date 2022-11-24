package characterization;

import jextension.SimpleExt;
import jextension.misc.ExtensionContextParamResolver;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.*;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(ExtensionContextParamResolver.class)
public class ProgrammaticRegTest extends ProgrammaticRegBase {

	@RegisterExtension
	public static SimpleExt extTestStatic = new SimpleExt();

	@RegisterExtension
	public SimpleExt extTestInstance = new SimpleExt();

	@Test
	public void howManySimpleExtsAreRegistered(ExtensionContext context) {
		assertNotNull(extBaseStatic);
		assertNotNull(extBaseInstance);
		assertNotNull(extTestStatic);
		assertNotNull(extTestInstance);
	}

	@Test
	public void howManySimpleExtsAreRegistered2(ExtensionContext context) {
		assertNotNull(extBaseStatic);
		assertNotNull(extBaseInstance);
		assertNotNull(extTestStatic);
		assertNotNull(extTestInstance);
	}
}
