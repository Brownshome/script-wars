package brownshome.scriptwars.client.tanks;

import java.io.IOException;

import brownshome.scriptwars.client.Network;

/**
 * This is an example of reading data from the server. We avoid using the pre-built Tank and
 * Shot classes as they would not be available in languages other than Java. The actual processing
 * of the data is left to the reader. But structs or classes would be useful.
 * 
 * @author James
 */
public class ExampleTankAIBasic {
	private static Network network;
	
	/**
	 * The main method to start the AI and connect to the server.
	 * 
	 * args[0] should contain the game id.
	 * You can request one from: http://script-wars.com/games/Tanks
	 * by clicking the 'Join' button.
	 * 
	 * @param args The input arguments containing the ID allocated by the server
	 * @throws IOException If we failed to connect to the server
	 */
	public static void main(String[] args) throws IOException {
		// args[0] should contain the game id.
		// You can request one from: http://script-wars.com/games/Tanks
		// by clicking the 'Join' button

		int id;
		if(args.length > 0){
			id = Integer.valueOf(args[0]);
		} else {
			System.out.println("Usage: JAVACOMMAND serverid");
			System.exit(1);
			return;
		}

		network = new Network(id, "www.script-wars.com", "John Smith Low Level");
		int direction = 0; //The initial direction, UP
		boolean hasHitWall = false;

		while(network.nextTick()) {
			boolean isAlive = network.getByte() == 1;
			if(!isAlive) {
				network.sendByte(0); //Always send data
				System.out.println("We are dead, not big surprise");
				continue;
			}
			
			int xPos = network.getByte();
			int yPos = network.getByte();
			
			int width = network.getByte();
			int height = network.getByte();
			
			boolean[][] map = new boolean[width][height];
			for(int y = 0; y < height; y++) {
				for(int x = 0; x < width; x++) {
					map[y][x] = network.getBoolean();
				}
			}
			
			int noTanks = network.getByte();
			
			int[][] tanks = new int[noTanks][2]; //An array holding the X and Y positions of each tank
			for(int i = 0; i < noTanks; i++) {
				tanks[i][0] = network.getByte();
				tanks[i][1] = network.getByte();
			}
			
			int noShots = network.getByte();
			
			int[][] shots = new int[noShots][3]; //An array holding the X, Y and direction of each shot
			for(int i = 0; i < noShots; i++) {
				shots[i][0] = network.getByte();
				shots[i][1] = network.getByte();
				shots[i][2] = network.getByte();
			}
			
			//For the actual AI, we shall hug the left wall and keep moving around, should keep things interesting
			//Check the anti-clockwise direction, then the straight, then the clockwise direction, then the reverse
			int antiClock = (direction + 1) % 4; //Cool right : D
			int clockwise = (direction + 3) % 4;
			int reverse = (direction + 2) % 4;
			
			if(isWall(direction, xPos, yPos, map)) {
				hasHitWall = true;
			}
			
			if(hasHitWall) {
				if(!isWall(antiClock, xPos, yPos, map)) {
					direction = antiClock;
				} else if(!isWall(direction, xPos, yPos, map)){
					//Don't change the direction
				} else if(!isWall(clockwise, xPos, yPos, map)) {
					direction = clockwise;
				} else if(!isWall(reverse, xPos, yPos, map)) {
					direction = reverse;
				} else {
					//We are trapped in a box, time to panic, and panic hard
				}
			}
			
			network.sendByte(1); //Move
			network.sendByte(direction);
		}
		
		System.out.println(network.getConnectionStatus()); //This function may not be available on some languages.
	}
	
	/**
	 * Check if there is a wall in the direction we are trying to go.
	 */
	private static boolean isWall(int direction, int xPos, int yPos, boolean[][] map) {
		switch(direction) {
			case 0: //UP
				yPos--;
				break;
			case 1: //LEFT
				xPos--;
				break;
			case 2: //DOWN
				yPos++;
				break;
			case 3: //RIGHT
				xPos++;
				break;
		}
		
		//x and yPos are now the next space.
		return map[yPos][xPos];
	}
}