package jextension;

import org.junit.jupiter.api.extension.*;

import java.io.IOException;
import java.util.Properties;

public class ConfigExt implements BeforeEachCallback, AfterEachCallback {

	public String findPath(final ExtensionContext context) {
		ConfigAnn ann = ExtensionUtil.findAnnotationForExtension(
				context, ConfigAnn.class).get();
		return ann.filepath();
	}

	@Override
	public void beforeEach(final ExtensionContext context) throws IOException {
		Properties props = new Properties();
		props.load(getClass().getResourceAsStream(findPath(context)));
		System.setProperties(props);
		System.out.println("ConfigExt Before");
	}

	@Override
	public void afterEach(final ExtensionContext context) throws IOException {
		// reset the sys props ...
		Properties props = new Properties();
		props.load(getClass().getResourceAsStream(findPath(context)));

		props.keySet().stream().forEach(k -> System.getProperties().remove(k));
	}
}
