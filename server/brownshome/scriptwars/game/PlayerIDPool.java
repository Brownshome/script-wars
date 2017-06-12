package brownshome.scriptwars.game;

import java.util.Arrays;

/** A class to be used for allocating and de-allocating player IDs. IDs are either active, free or requested.
 * Every operation is O(1) Effort is made to only re-use to oldest IDs */
public class PlayerIDPool extends IDPool {
	/* Request array is a sliding window from [start to end) % capacity. 
	 * If start is greater than capacity subtract capacity from both. 
	 * 
	 * To remove an item, move the item at the start to the hole and increment the start.
	 * To add an item increment end and place the item into the array.
	 * To remove the oldest item remove the item at start
	 */
	private final int[] requestedIDs;
	private int start = 0; //start < capacity
	private int end = 0; //end >= start, end <= start + capacity
	private final int[] positionOfRequested; //-1 <= x < capacity
	
	/** Creates an ID pool with capacity IDs, from 0 to capacity -1 */
	public PlayerIDPool(int capacity) {
		super(capacity);
		
		requestedIDs = new int[capacity];
		positionOfRequested = new int[capacity];
		Arrays.fill(positionOfRequested, -1);
	}
	
	/**
	 * Gets an ID from the list of IDs, this method checks the free IDs before it returns an old requested ID.
	 */
	@Override
	public int request() throws OutOfIDsException {
		int ID;
		
		if(hasFreeIDs()) {
			ID = super.request();	
		} else {

			//We now grab the a requested ID.
			if(start == end)
				throw new OutOfIDsException();

			//Grab the first.
			ID = requestedIDs[start];

			removeRequestedAt(start);
		}
		
		requestedIDs[end % poolSize()] = ID;
		positionOfRequested[ID] = end % poolSize();
		end++;
		
		return ID;
	}
	
	/** Makes no checks on index validity */
	private void removeRequestedAt(int index) {
		assert positionOfRequested[requestedIDs[index]] != -1 && start != end;

		positionOfRequested[requestedIDs[index]] = -1;
		
		if(index == start) {
			start++;
		} else if(index == (end - 1) % poolSize()) {
			end--;
			return; //No need to check start invariant
		} else {
			//Move the item at the start to the vacancy.
			int filler = requestedIDs[start];
			requestedIDs[index] = filler;
			positionOfRequested[filler] = index;
			start++;
		}
		
		//Ensure start < capacity
		if(start >= poolSize()) {
			start -= poolSize();
			end -= poolSize();
		}
	}

	/**
	 * Checks if an ID was previously returned by request() and not made active. This should be used if it is unknown if this is true
	 * prior to calling makeActive(id). This method does make bounds checks.
	 */
	public boolean isRequested(int id) {
		return id >= 0 && id < poolSize() && positionOfRequested[id] != -1;
	}
	
	/**
	 * Makes an ID active, this ID must be an ID returned from request().
	 */
	public void makeActive(int id) {
		int index = positionOfRequested[id];
		removeRequestedAt(index);
	}
	
	/**
	 * Frees an ID. This must be an ID previously made active.
	 **/
	@Override
	public void free(int id) {
		super.free(id);
	}
}
