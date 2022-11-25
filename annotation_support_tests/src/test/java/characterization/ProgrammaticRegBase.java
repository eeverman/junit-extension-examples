package characterization;

import characterization.SimpleExt;
import org.junit.jupiter.api.extension.RegisterExtension;

public class ProgrammaticRegBase {

	@RegisterExtension
	public static SimpleExt extBaseStatic = new SimpleExt("Super Static");

	@RegisterExtension
	public SimpleExt extBaseInstance = new SimpleExt("Super Instance");
}
