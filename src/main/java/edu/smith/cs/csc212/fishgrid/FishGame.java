package edu.smith.cs.csc212.fishgrid;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import me.jjfoley.gfx.IntPoint;

// https://moodle.smith.edu/pluginfile.php/801626/mod_resource/content/0/06_Java_Maps.pdf

/**
 * This class manages our model of gameplay: missing and found fish, etc.
 * 
 * @author jfoley
 *
 */
public class FishGame {
	/**
	 * This is the world in which the fish are missing. (It's mostly a List!).
	 */
	World world;
	/**
	 * The player (a Fish.COLORS[0]-colored fish) goes seeking their friends.
	 */
	Fish player;
	/**
	 * The home location.
	 */
	FishHome home;
	/**
	 * These are the missing fish!
	 */
	List<Fish> missing;

	/**
	 * These are fish we've found!
	 */
	List<Fish> found;

	/**
	 * Number of steps!
	 */
	int stepsTaken;

	/**
	 * Score!
	 */
	int score;

	// Rocks that exist
	List<Rock> existRocks;

	// Number of rocks
	final int NUM_ROCKS = 15;

	// Bring fish home
	List<WorldObject> homeList;
		
	List<Fish> homeFish;
	
	/**
	 * Create a FishGame of a particular size.
	 * 
	 * @param w how wide is the grid?
	 * @param h how tall is the grid?
	 */
	public FishGame(int w, int h) {
		world = new World(w, h);

		missing = new ArrayList<Fish>();
		found = new ArrayList<Fish>();
		existRocks = new ArrayList<Rock>();

		// Add a home!
		home = world.insertFishHome();

		// Insert rocks randomly into list "existRocks"
		for (int i = 0; i < NUM_ROCKS; i++) {
			Rock rocks = world.insertRockRandomly();
			existRocks.add(rocks);
			System.out.println(existRocks);
		}

		// Insert snail
		world.insertSnailRandomly();

		// Make the player out of the 0th fish color.
		player = new Fish(0, world);
		// Start the player at "home".
		player.setPosition(home.getX(), home.getY());
		player.markAsPlayer();
		world.register(player);
		
		// orange list
		//orange = new ArrayList<Fish>();
		

		// Generate fish of all the colors but the first into the "missing" List.
		for (int ft = 1; ft < Fish.COLORS.length-1; ft++) {
			Fish friend = world.insertFishRandomly(ft);
			missing.add(friend);
			}
		
		PinkFish special = new PinkFish(world);
		missing.add(special);
		world.insertRandomly(special);
		}

	/**
	 * How we tell if the game is over: if missingFishLeft() == 0.
	 * 
	 * @return the size of the missing list.
	 */
	public int missingFishLeft() {
		return missing.size();
	}

	/**
	 * This method is how the Main app tells whether we're done.
	 * 
	 * @return true if the player has won (or maybe lost?).
	 */
	public boolean gameOver() {
		// TODO(FishGrid) We want to bring the fish home before we win!
		//bringHome(x, y);
//		if (homeList.size() > 9) {
//			
//		}
		return missing.isEmpty();
	

		// These are all the objects in the world in the same cell as the player.
	}
	
	public void bringHome(int x, int y) {
		// list of fish in home
		homeList = new ArrayList<WorldObject>();
		
		// fish to go home
		List<WorldObject> homeFish = world.find(x, y);
				
		// Run for loop when i < how many things at this point
		for (int i = 0; i < missing.size(); i++) {
			// fish to go home
			WorldObject homie = homeFish.get(i);
			// If clicked point in list of existing rocks,
			if (found.contains(homie)) {
				// Add fish to homeList
				this.homeList.add((Fish) homie);
				// Remove fish from homeFish and from world
				this.homeFish.remove((Fish) homie);
				this.world.remove((Fish) homie);
			}
		}		
	}

	/**
	 * Update positions of everything (the user has just pressed a button).
	 */
	public void step() {
		// Keep track of how long the game has run.
		this.stepsTaken += 1;

		// Make sure missing fish *do* something.
		wanderMissingFish();

		// These are all the objects in the world in the same cell as the player.
		List<WorldObject> overlap = this.player.findSameCell();

		// The player is there, too, let's skip them.
		overlap.remove(this.player);

		// If we find a fish, remove it from missing.
		for (WorldObject thing : overlap) {
			if (thing == this.player) {
				continue;
			}

			// It is missing if it's in our missing list.
			if (missing.contains(thing)) {
				if (!(thing instanceof Fish)) {
					throw new AssertionError("wo must be a Fish since it was in missing!");
				}

				// Convince Java it's a Fish (we know it is!)
				Fish justFound = (Fish) thing;

				// Remove this fish from this missing list.
				missing.remove(justFound);

				// Add to found! (So we see objectsFollow work!)
				// Note that thing is a fish in this case
				found.add((Fish) thing);
				
				System.out.println(found);
				
				if (this.stepsTaken > 20) {
					found.remove((Fish) thing);
				}

				// Increase score when you find a fish!
				// if fish 1 to found list, Fish(1, world)
				// if () {

		// added		
		//if (found.add((Fish) thing)); { 
			//for (int i = 0; i<missing.size(); i++) { 
				
				

			
				//WorldObject homeFish = found.get(i);
				// found list has home fish
				//if (found.contains(homeFish)) {
					score += 10; 
					//}
				//}
			}
		}

			// When fish get added to "found" they will follow the player around.
			World.objectsFollow(player, found);
			// Step any world-objects that run themselves.
			world.stepAll();
		
	}

	/**
	 * Call moveRandomly() on all of the missing fish to make them seem alive.
	 */
	private void wanderMissingFish() {
		Random rand = ThreadLocalRandom.current();
		for (Fish lost : missing) {
			// 30% of the time, lost fish move randomly.
			if (rand.nextDouble() < 0.3) {
				lost.moveRandomly();
				// 80% of the time, scared fish move randomly
			} else if (rand.nextDouble() > 0.3 && rand.nextDouble() < 0.8) {
				lost.moveRandomly();
			}
		}
	}

	/**
	 * This gets a click on the grid. We want it to destroy rocks that ruin the
	 * game.
	 * 
	 * @param x - the x-tile.
	 * @param y - the y-tile.
	 * @return
	 */

	public void stepOn(int x, int y) {
		List<WorldObject> atPoint = world.find(x, y);

		for (int i = 0; i < atPoint.size(); i++) {
			WorldObject steppedOn = atPoint.get(i);
			if (missing.contains(steppedOn)) {
				found.add((Fish) steppedOn);
			}
		}
	}

	public void click(int x, int y) {
		// List of things at this point
		List<WorldObject> atPoint = world.find(x, y);

		// Run for loop when i < how many things at this point
		for (int i = 0; i < atPoint.size(); i++) {
			// Clicked point
			WorldObject clicked = atPoint.get(i);
			//System.out.println(clicked); //prints rock coords
			// If clicked point in list of existing rocks,
			if (existRocks.contains(clicked)) {
				// Remove rock from this list and from world
				this.existRocks.remove((Rock) clicked);
				this.world.remove((Rock) clicked);
			}
		}
	}
}
