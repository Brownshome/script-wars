package brownshome.scriptwars.server.game.tanks;

import java.util.*;

class MapGenerator {
	
	//STARTUP-PARAMETERS
	private int width;
	private int height;
	//the percentage of the grid that will be empty space.
	private float density;
	//how many routes on average there will be from A to B. There will be at least one regardless of this value
	private float connectivity;
	
	//GENERATED VARIABLES
	private int fillGoal;
	private Random random;
	
	//WORKING VARIABLES
	private boolean[][] map;
	//The number of spaces that have been filled in
	private int fills;
	
	private MapGenerator() {
		width = 15;
		height = 15;
		density = 1.0f;
		connectivity = 0.0f;
	}
	
	static MapGenerator getGenerator() {
		return new MapGenerator();
	}
	
	MapGenerator withSize(int width, int height) {
		assert width > 0 && height > 0;
		
		this.width = width;
		this.height = height;
		return this;
	}
	
	MapGenerator withDensity(float density) {
		assert density >= 0.0f && density <= 1.0f;
		
		this.density = density;
		return this;
	}
	
	MapGenerator withConnectivity(float connectivity) {
		this.connectivity = connectivity;
		return this;
	}

	boolean[][] generate() {
		return generate(System.currentTimeMillis());
	}
	
	boolean[][] generate(long seed) {
		random = new Random(seed);
		
		fills = 0;
		fillGoal = Math.round((width - 1) * (height - 1) * density);
		map = new boolean[height][width];
		fillEdge();
		partitionMap(1, 1, width - 1, height - 1, fillGoal - fills, true);
		
		return map;
	}

	//[xStart, xEnd) and [yStart, yEnd)
	private void partitionMap(int xStart, int yStart, int xEnd, int yEnd, int toFill, boolean vertical) {
		assert xStart != 0 && yStart != 0;
		
		if(xEnd - xStart < 3 && yEnd - yStart < 3)
			return; //Not enough space to partition
		
		if(toFill <= 0)
			return;
		
		//Pick division line.
		//Put a hole if the resulting space is enclosed.
		//Put more holes depending on the connectivity argument.
		
		if(xEnd - xStart < 3)
			vertical = false;
		
		if(yEnd - yStart < 3)
			vertical = true;
		
		if(vertical) {
			int lineX;
			
			int attempt = 0;
			do {
				lineX = xStart + 1 + random.nextInt(xEnd - xStart - 2);
				if(attempt++ > 10)
					return;
			} while(!map[yStart - 1][lineX] || !map[yEnd][lineX]);
			
			for(int y = yStart; y < yEnd; y++) {
				map[y][lineX] = true;
			}
			
			map[random.nextInt(yEnd - yStart) + yStart][lineX] = false;
			
			for(int y = yStart; y < yEnd; y++) {
				map[y][lineX] &= random.nextFloat() * (yEnd - yStart) >= connectivity;
				
				if(map[y][lineX])
					toFill--;
			}
			
			int toFillA = toFill * (lineX - xStart) / (xEnd - xStart);
			int toFillB = toFill - toFillA;
			
			partitionMap(xStart, yStart, lineX, yEnd, toFillA, !vertical);
			partitionMap(lineX + 1, yStart, xEnd, yEnd, toFillB, !vertical);
		} else {
			int lineY;
			
			int attempt = 0;
			do {
				lineY = yStart + 1 + random.nextInt(yEnd - yStart - 2);
				if(attempt++ > 10)
					return;
			} while(!map[lineY][xStart - 1] || !map[lineY][xEnd]);

			for(int x = xStart; x < xEnd; x++) {
				map[lineY][x] = true;
			}
			
			map[lineY][random.nextInt(xEnd - xStart) + xStart] = false;

			for(int x = xStart; x < xEnd; x++) {
				map[lineY][x] &= random.nextFloat() * (xEnd - xStart) >= connectivity;
				
				if(map[lineY][x])
					toFill--;
			}
			
			int toFillA = toFill * (lineY - yStart) / (yEnd - yStart);
			int toFillB = toFill - toFillA;
			
			partitionMap(xStart, yStart, xEnd, lineY, toFillA, !vertical);
			partitionMap(xStart, lineY + 1, xEnd, yEnd, toFillB, !vertical);
		}
	}

	private void printMap() {
		for(int y = 0; y < height; y++) {
			for(int x = 0; x < width; x++) {
				System.out.print(map[y][x] ? '#' : ' ');
			}
			
			System.out.println();
		}
	}

	private void fillEdge() {
		Arrays.fill(map[0], true);
		Arrays.fill(map[height - 1], true);
		
		for(int y = 1; y < height - 1; y++) {
			map[y][0] = true;
			map[y][width - 1] = true;
		}
	}
}
