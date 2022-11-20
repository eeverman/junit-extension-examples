package ext.simple;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@SimpleAnn
public class SimpleAnnTest {

	@Test
	public void phaserShouldBeSetToStun() {
		assertEquals("stun", System.getProperty("phaser"));
	}

}