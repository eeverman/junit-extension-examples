package characterization;

import org.junit.jupiter.api.extension.*;

import java.io.IOException;
import java.util.*;

/**
 * Simple extension that is intended to be registered programmatically and
 * to be identifiable:  It has a static method to ask for an instance list.
 */
public class SimpleExt implements BeforeEachCallback, AfterEachCallback {

	static Queue<SimpleExt> instances = new LinkedList<>();

	private String _name;

	public SimpleExt() {
		_name = "UNKNOWN";
	}

	public SimpleExt(String name) {
		_name = name;
	}

	@Override
	public void beforeEach(final ExtensionContext context) throws IOException {
		instances.add(this);
		System.out.println("SimpleExt BeforeEach:  " + _name);
	}

	@Override
	public void afterEach(final ExtensionContext context) throws IOException {
		instances.remove();
		System.out.println("SimpleExt AfterEach:  " + _name);
	}

	public static List<SimpleExt> getInstances() {
		return instances.stream().toList();
	}

	public String getName() {
		return _name;
	}
}
