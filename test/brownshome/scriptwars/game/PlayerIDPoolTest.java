package brownshome.scriptwars.game;

import static org.junit.Assert.*;

import java.util.*;

import static org.hamcrest.CoreMatchers.*;

import org.junit.*;

public class PlayerIDPoolTest {
	private static final int POOL_SIZE = 10;
	PlayerIDPool idPool;
	
	@Before
	public void setup() {
		idPool = new PlayerIDPool(POOL_SIZE);
	}
	
	@Test
	public void testRequest() throws OutOfIDsException {
		for(int i = 0; i < idPool.poolSize() * 5; i++) {
			idPool.request();
		}
		
		for(int i = 1; i < idPool.poolSize(); i++) {
			idPool.makeActive(i);
		}
		
		assertEquals(0, idPool.request());
		
		idPool.makeActive(0);
		
		try {
			idPool.request();
			fail();
		} catch (OutOfIDsException e) {
			//Expected
		}
	}

	@Test
	public void testIsRequested() throws OutOfIDsException {
		int requested = idPool.request();
		assertTrue(idPool.isRequested(requested));
	}

	@Test
	public void testMakeActive() throws OutOfIDsException {
		int requested = idPool.request();
		idPool.makeActive(requested);
		assertFalse(idPool.isRequested(requested));
	}
}
