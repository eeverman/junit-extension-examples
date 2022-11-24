package jextension;

import org.junit.jupiter.api.extension.*;

import java.io.IOException;
import java.util.Properties;

public class SimpleExt implements BeforeEachCallback, AfterEachCallback {

	@Override
	public void beforeEach(final ExtensionContext context) throws IOException {
		Properties props = new Properties();
		props.load(getClass().getResourceAsStream("/MyFile.props"));
		System.setProperties(props);
		System.out.println("SimpleExt BeforeEach! " + this);
	}

	@Override
	public void afterEach(final ExtensionContext context) throws IOException {
		// reset the sys props ...
		Properties props = new Properties();
		props.load(getClass().getResourceAsStream("/MyFile.props"));
		props.keySet().stream().forEach(k -> System.getProperties().remove(k));
		System.out.println("SimpleExt AfterEach! " + this);
	}
}
