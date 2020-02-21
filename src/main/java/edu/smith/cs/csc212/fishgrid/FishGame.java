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
	
	// Number of steps before lose fish
	final int stepsNum = 20;
	
	// To see if stepsTaken is evenly divided over stepsNum
	int stepsDiv;	

	/**
	 * Score!
	 */
	int score;

	// Rocks that exist
	List<Rock> existRocks;

	// Number of rocks
	final int NUM_ROCKS = 15;
	
	// Number of hearts
	final int NUM_HEARTS = 5;

	// Fish at home
	List<Fish> homeList;
	
	// Hearts not collected
	List<Heart> available;
	
	// Hearts collected
	List<Heart> collected;
	
			
	/**
	 * Create a FishGame of a particular size.
	 * 
	 * @param w how wide is the grid?
	 * @param h how tall is the grid?
	 */
	public FishGame(int w, int h) {
		world = new World(w, h);
		
		// List for fish that haven't yet been found
		missing = new ArrayList<Fish>();
		// List for fish that have been found
		found = new ArrayList<Fish>();
		// List for rocks that exist
		existRocks = new ArrayList<Rock>();
		// List for fish that have gone home
		homeList = new ArrayList<Fish>();

		// Add a home!
		home = world.insertFishHome();

		// Insert rocks randomly into list "existRocks"
		for (int i = 0; i < NUM_ROCKS; i++) {
			Rock rocks = world.insertRockRandomly();
			existRocks.add(rocks);
		}

		// Insert snail
		world.insertSnailRandomly();
		
		// Insert hearts randomly
		for (int i = 0; i < NUM_HEARTS; i++) {
			world.insertHeartRandomly();
		}
		

		// Make the player out of the 0th fish color.
		player = new Fish(0, world);
		// Start the player at "home".
		player.setPosition(home.getX(), home.getY());
		player.markAsPlayer();
		world.register(player);	

		// Generate fish of all the colors but the first into the "missing" List.
		for (int ft = 1; ft < Fish.COLORS.length-1; ft++) {
			Fish friend = world.insertFishRandomly(ft);
			missing.add(friend);
			}
		
		// PinkFish is special fish
		PinkFish special = new PinkFish(world);
		missing.add(special);
		world.insertRandomly(special);
		
		// Bubble
		Bubble bubble = new Bubble(world);
		// (got stuck on making bubble)
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
		// All fish are home/in homeList
		return allHome();	
	}
	
	// Are all fish in homeList?
	public boolean allHome() {
		if (homeList.size() == Fish.COLORS.length) {
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * Update positions of everything (the user has just pressed a button).
	 */
	public void step() {
		// Keep track of how long the game has run.
		this.stepsTaken += 1;
		
		this.stepsDiv = this.stepsTaken/this.stepsNum;
		
		// If number of steps taken is evenly divisible by 20, 20 steps have been walked
		// https://stackoverflow.com/questions/12558206/how-can-i-check-if-a-value-is-of-type-integer
		if (this.stepsDiv == (int)this.stepsDiv) { 
			// last fish in line gets lost (index to remove only last fish in line)
		}
		

		// Make sure missing fish *do* something.
		wanderMissingFish();

		// These are all the objects in the world in the same cell as the player.
		List<WorldObject> overlap = this.player.findSameCell();

		// The player is there, too, let's skip them.
		overlap.remove(this.player);
		
		// List for hearts not collected
		available = new ArrayList<Heart>();
		// List for hearts collected
		collected = new ArrayList<Heart>();

		// If we find a fish, remove it from missing.
		for (WorldObject thing : overlap) {
			if (thing == this.player) {
				continue;
			}

			// Fish is missing if it's in our missing list.
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
				found.add(justFound);
								
				// Increase score when you find a fish! (PinkFish is worth more points)
				if (thing instanceof PinkFish) {
					score += 20;
				} else {
					score += 10;
				}	
			
			// Heart is missing if it's in our available list.
			// (got stuck on removing hearts and adding to score)
			} else if (available.contains(thing)) {
				if (!(thing instanceof Heart)) {
					throw new AssertionError("wo must be a Heart since it was in available!");
				}

				// Convince Java it's a Heart (we know it is!)
				Heart collection = (Heart) thing;

				// Remove this heart from this available list.
				available.remove(collection);
				this.world.remove(collection);

				// Add to collected! 
				// Note that thing is a heart in this case
				collected.add(collection);
				
				score += 5;
				
				}	
			}
				
		// When fish get added to "found" they will follow the player around.
		World.objectsFollow(player, found);
		// Step any world-objects that run themselves.
		world.stepAll();
		
		// If player fish is at home, bring fish home (move to homeList)
		if (player.inSameSpot(home)) {
			bringHome();
		}
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
		// If fish has been stepped on, fish is found
		for (int i = 0; i < atPoint.size(); i++) {
			WorldObject steppedOn = atPoint.get(i);
			if (missing.contains(steppedOn)) {
				found.add((Fish) steppedOn);
			} else if (available.contains(steppedOn)) {
				collected.add((Heart) steppedOn);
			}
		}
	}

	public void bringHome() {
		// In found list, add fish to homeList and remove from world
		for (Fish fish : found) {
			homeList.add(fish);
			this.world.remove(fish);
		}
		// Clear found list
		found.clear();		
	}

	
	// Click to remove rock
	public void click(int x, int y) {
		// List of things at this point
		List<WorldObject> atPoint = world.find(x, y);

		// Run for loop when i < how many things at this point
		for (int i = 0; i < atPoint.size(); i++) {
			// Clicked point
			WorldObject clicked = atPoint.get(i);
			// If clicked point in list of existing rocks,
			if (existRocks.contains(clicked)) {
				// Remove rock from this list and from world
				this.existRocks.remove((Rock) clicked);
				this.world.remove((Rock) clicked);
			}
		}
	}
}
