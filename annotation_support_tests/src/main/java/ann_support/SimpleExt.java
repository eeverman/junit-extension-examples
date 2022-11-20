package ann_support;

import org.junit.jupiter.api.extension.*;

import java.io.IOException;
import java.util.Properties;

public class SimpleExt implements BeforeEachCallback, AfterEachCallback {

	@Override
	public void beforeEach(final ExtensionContext context) throws IOException {
		Properties props = new Properties();
		props.load(getClass().getResourceAsStream("/MyFile.props"));
		System.setProperties(props);
	}

	@Override
	public void afterEach(final ExtensionContext context) throws IOException {
		// reset the sys props ...
		Properties props = new Properties();
		props.load(getClass().getResourceAsStream("/MyFile.props"));

		props.keySet().stream().forEach(k -> System.getProperties().remove(k));
	}
}
