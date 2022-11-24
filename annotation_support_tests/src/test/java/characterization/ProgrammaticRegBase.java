package characterization;

import jextension.SimpleExt;
import org.junit.jupiter.api.extension.RegisterExtension;

public class ProgrammaticRegBase {

	@RegisterExtension
	public static SimpleExt extBaseStatic = new SimpleExt();

	@RegisterExtension
	public SimpleExt extBaseInstance = new SimpleExt();
}
