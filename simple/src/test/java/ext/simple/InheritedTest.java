package ext.simple;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class InheritedTest extends InheritedTestBase {

	@Test
	public void phaserShouldBeSetToEntertain() {
		assertEquals("entertain", System.getProperty("phaser"));
	}

}


