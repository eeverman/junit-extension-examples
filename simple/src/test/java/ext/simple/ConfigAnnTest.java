package ext.simple;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ConfigAnn(filepath = "/other.props")
public class ConfigAnnTest {

	@Test
	public void phaserShouldBeSetToEntertain() {
		assertEquals("entertain", System.getProperty("phaser"));
	}

}