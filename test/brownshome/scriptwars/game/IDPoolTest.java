package brownshome.scriptwars.game;

import static org.hamcrest.CoreMatchers.hasItems;
import static org.junit.Assert.*;

import java.util.*;

import org.junit.*;

public class IDPoolTest {
	private static final int POOL_SIZE = 10;
	IDPool idPool;
	
	@Before
	public void setup() {
		idPool = new IDPool(POOL_SIZE);
	}
	
	@Test
	public void testRequest() throws OutOfIDsException {
		List<Integer> result = Arrays.asList(new Integer[POOL_SIZE]);
		
		for(int i = 0; i < idPool.poolSize(); i++)
			result.set(i, idPool.request());
		
		assertThat(result, hasItems(0, 1, 2, 3, 4, 5, 6, 7, 8, 9));
		
		try {
			idPool.request();
			fail();
		} catch(OutOfIDsException e) {
			//Expected
		}
	}

	@Test
	public void testHasFreeIDs() throws OutOfIDsException {
		for(int i = 0; i < idPool.poolSize(); i++) {
			assertTrue(idPool.hasFreeIDs());
			idPool.request();
		}
		
		assertFalse(idPool.hasFreeIDs());
	}

	@Test
	public void testPoolSize() {
		assertEquals(POOL_SIZE, idPool.poolSize());
	}

	@Test
	public void testFree() throws OutOfIDsException {
		for(int i = 0; i < idPool.poolSize(); i++) {
			idPool.request();
		}
		
		idPool.free(5);
		assertEquals(5, idPool.request());
	}
}
