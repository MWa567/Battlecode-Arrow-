package crossbow;

import battlecode.common.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.ArrayList;
import java.util.List;


/**
 * RobotPlayer is the class that describes your main robot strategy.
 * The run() method inside this class is like your main function: this is what we'll call once your robot
 * is created!
 */
public class RobotPlayer {

    static int turnCount = 0;
    static final Random rng = new Random(6147);
    static MapLocation target;
    static MapLocation originalLocation = null;

    static int towersBuilding = 0;

    static List<MapLocation> ruins = new ArrayList<>();

    /** Array containing all the possible movement directions. */
    static final Direction[] directions = {
        Direction.NORTH,
        Direction.NORTHEAST,
        Direction.EAST,
        Direction.SOUTHEAST,
        Direction.SOUTH,
        Direction.SOUTHWEST,
        Direction.WEST,
        Direction.NORTHWEST,
    };

    /**
     * run() is the method that is called when a robot is instantiated in the Battlecode world.
     * It is like the main function for your robot. If this method returns, the robot dies!
     *
     * @param rc  The RobotController object. You use it to perform actions from this robot, and to get
     *            information on its current status. Essentially your portal to interacting with the world.
     **/
    @SuppressWarnings("unused")
    public static void run(RobotController rc) throws GameActionException {

        System.out.println("I'm alive");

        // You can also use indicators to save debug notes in replays.
        rc.setIndicatorString("Hello world!");

        if (originalLocation == null) {
        	originalLocation = rc.getLocation();
        }

        while (true) {
            // This code runs during the entire lifespan of the robot, which is why it is in an infinite
            // loop. If we ever leave this loop and return from run(), the robot dies! At the end of the
            // loop, we call Clock.yield(), signifying that we've done everything we want to do.

            turnCount += 1;  // We have now been alive for one more turn!

            // Try/catch blocks stop unhandled exceptions, which cause your robot to explode.
            try {
                // The same run() function is called for every robot on your team, even if they are
                // different types. Here, we separate the control depending on the UnitType, so we can
                // use different strategies on different robots. If you wish, you are free to rewrite
                // this into a different control structure!
                switch (rc.getType()){
                    case SOLDIER: runSoldier(rc); break;
                    case SPLASHER: runSplasher(rc); break;
                    default: runTower(rc, turnCount); break;
                    }
             }
             catch (GameActionException e) {
                // Oh no! It looks like we did something illegal in the Battlecode world. You should
                // handle GameActionExceptions judiciously, in case unexpected events occur in the game
                // world. Remember, uncaught exceptions cause your robot to explode!
                System.out.println("GameActionException");
                e.printStackTrace();

            } catch (Exception e) {
                // Oh no! It looks like our code tried to do something bad. This isn't a
                // GameActionException, so it's more likely to be a bug in our code.
                System.out.println("Exception");
                e.printStackTrace();

            } finally {
                // Signify we've done everything we want to do, thereby ending our turn.
                // This will make our code wait until the next turn, and then perform this loop again.
                Clock.yield();
            }
            // End of loop: go back to the top. Clock.yield() has ended, so it's time for another turn!
        }

        // Your code should never reach here (unless it's intentional)! Self-destruction imminent...
    }

    /**
     * Run a single turn for towers.
     * This code is wrapped inside the infinite loop in run(), so it is called once per turn.
     */
    public static void runTower(RobotController rc, int turnCount) throws GameActionException{
        // Pick a direction to build in.
        Direction dir = directions[rng.nextInt(directions.length)];
        MapLocation nextLoc = rc.getLocation().add(dir);

		// if (rc.canBuildRobot(UnitType.SOLDIER, nextLoc)){
        //     rc.buildRobot(UnitType.SOLDIER, nextLoc);
        //     System.out.println("BUILT A SOLDIER");
        // }

        // if (rc.canBuildRobot(UnitType.SPLASHER, nextLoc)){
        //     rc.buildRobot(UnitType.SPLASHER, nextLoc);
        //     System.out.println("BUILT A SPLASHER");
        // }

        if (rc.getRoundNum() <= 500 && rc.canBuildRobot(UnitType.SOLDIER, nextLoc)){
            rc.buildRobot(UnitType.SOLDIER, nextLoc);
            System.out.println("BUILT A SOLDIER");
        }

        else if (rc.getRoundNum() > 500 && rc.canBuildRobot(UnitType.SPLASHER, nextLoc)){
            rc.buildRobot(UnitType.SPLASHER, nextLoc);
            System.out.println("BUILT A SPLASHER");
        }
        /*
        else if (rc.canBuildRobot(UnitType.SPLASHER, nextLoc)){
            // rc.buildRobot(UnitType.SPLASHER, nextLoc);
            // System.out.println("BUILT A SPLASHER");
            rc.setIndicatorString("SPLASHER NOT IMPLEMENTED YET");
        }
        */

        // Read incoming messages
        Message[] messages = rc.readMessages(-1);
        for (Message m : messages) {
            System.out.println("Tower received message: '#" + m.getSenderID() + " " + m.getBytes());
        }

        // TODO: can we attack other bots?
    }


    /**
     * Run a single turn for a Soldier.
     * This code is wrapped inside the infinite loop in run(), so it is called once per turn.
     */
    public static void runSoldier(RobotController rc) throws GameActionException{
    	Util.init(rc);
        Explore.init(rc);
        Pathfinding.init(rc);
        Pathfinding.initTurn();
        target = Explore.getExploreTarget();
    	while (true) {
	    	// Sense information about all visible nearby tiles.
	        MapInfo[] nearbyTiles = rc.senseNearbyMapInfos();
	        // Search for a nearby ruin to complete.

	        //If we can see a ruin, move towards it
	        MapInfo curRuin = null;
	        int curDist = 999999;
	        for (MapInfo tile : nearbyTiles){
	            if (tile.hasRuin() && rc.senseRobotAtLocation(tile.getMapLocation()) == null){
	            	int dist = tile.getMapLocation().distanceSquaredTo(rc.getLocation());
	            	if (dist < curDist) {
		                curRuin = tile;
		                curDist = dist;
		                MapLocation targetLoc = curRuin.getMapLocation();

		                // Complete the ruin if we can.
		            	if (rc.canCompleteTowerPattern(UnitType.LEVEL_ONE_PAINT_TOWER, targetLoc)){
		                    rc.completeTowerPattern(UnitType.LEVEL_ONE_PAINT_TOWER, targetLoc);
		                    rc.setTimelineMarker("Tower built", 0, 255, 0);
		                    System.out.println("Built a paint tower at " + targetLoc + "!");
		                }
		            	else if (rc.canCompleteTowerPattern(UnitType.LEVEL_ONE_MONEY_TOWER, targetLoc)){
		                    rc.completeTowerPattern(UnitType.LEVEL_ONE_MONEY_TOWER, targetLoc);
		                    rc.setTimelineMarker("Tower built", 0, 255, 0);
		                    System.out.println("Built a money tower at " + targetLoc + "!");
		                }
	            	}
	            }
	        }
	        if (curRuin != null){
	            MapLocation targetLoc = curRuin.getMapLocation();
	            Direction dir = rc.getLocation().directionTo(targetLoc);
	            if (rc.canMove(dir))
	            	rc.move(dir);
	            int towerCount = towersBuilding % 6;
	            if (towerCount == 0 || towerCount == 2 || towerCount == 5) {
	            	// Mark the pattern we need to draw to build a tower here if we haven't already.
	                MapLocation shouldBeMarked = curRuin.getMapLocation().subtract(dir);
	                towersBuilding += 1;
	                if (rc.senseMapInfo(shouldBeMarked).getMark() == PaintType.EMPTY && rc.canMarkTowerPattern(UnitType.LEVEL_ONE_PAINT_TOWER, targetLoc)){
	                    rc.markTowerPattern(UnitType.LEVEL_ONE_PAINT_TOWER, targetLoc);
	                    System.out.println("Trying to build a tower at " + targetLoc);
	                }
	                // Fill in any spots in the pattern with the appropriate paint.
	                for (MapInfo patternTile : rc.senseNearbyMapInfos(targetLoc, 8)){
	                    if (patternTile.getMark() != patternTile.getPaint() && patternTile.getMark() != PaintType.EMPTY){
	                        boolean useSecondaryColor = patternTile.getMark() == PaintType.ALLY_SECONDARY;
	                        if (rc.canAttack(patternTile.getMapLocation()))
	                            rc.attack(patternTile.getMapLocation(), useSecondaryColor);
	                    }
	                }
	            }
	            else {
	            	// Mark the pattern we need to draw to build a tower here if we haven't already.
	                MapLocation shouldBeMarked = curRuin.getMapLocation().subtract(dir);
	                towersBuilding += 1;
	                if (rc.senseMapInfo(shouldBeMarked).getMark() == PaintType.EMPTY && rc.canMarkTowerPattern(UnitType.LEVEL_ONE_MONEY_TOWER, targetLoc)){
	                    rc.markTowerPattern(UnitType.LEVEL_ONE_MONEY_TOWER, targetLoc);
	                    System.out.println("Trying to build a tower at " + targetLoc);
	                }
	                // Fill in any spots in the pattern with the appropriate paint.
	                for (MapInfo patternTile : rc.senseNearbyMapInfos(targetLoc, 8)){
	                    if (patternTile.getMark() != patternTile.getPaint() && patternTile.getMark() != PaintType.EMPTY){
	                        boolean useSecondaryColor = patternTile.getMark() == PaintType.ALLY_SECONDARY;
	                        if (rc.canAttack(patternTile.getMapLocation()))
	                            rc.attack(patternTile.getMapLocation(), useSecondaryColor);
	                    }
	                }
	            }
	            // Complete the ruin if we can.
            	if (rc.canCompleteTowerPattern(UnitType.LEVEL_ONE_PAINT_TOWER, targetLoc)){
                    rc.completeTowerPattern(UnitType.LEVEL_ONE_PAINT_TOWER, targetLoc);
                    rc.setTimelineMarker("Tower built", 0, 255, 0);
                    System.out.println("Built a paint tower at " + targetLoc + "!");
                }
            	else if (rc.canCompleteTowerPattern(UnitType.LEVEL_ONE_MONEY_TOWER, targetLoc)){
                    rc.completeTowerPattern(UnitType.LEVEL_ONE_MONEY_TOWER, targetLoc);
                    rc.setTimelineMarker("Tower built", 0, 255, 0);
                    System.out.println("Built a money tower at " + targetLoc + "!");
                }
	        }

	        if (rc.getLocation().x == 0 || rc.getLocation().x == rc.getMapWidth() || rc.getLocation().y == 0 || rc.getLocation().y == rc.getMapHeight()) {
        	   try {
	            	Pathfinding.move(target);
					// MapInfo[] nearby = rc.senseNearbyMapInfos();
					// int num = 0;

					// for (int i=0; i<nearby.length; i++){
					// 	num++;
					// 	MapInfo cur = nearby[i];
					// 	MapLocation nextLoc = cur.getMapLocation();

					// 	if (cur.isPassable() && (cur.getPaint()==PaintType.EMPTY || cur.getPaint().isEnemy())){
					// 		System.out.println("Paintcheck" + num + "  " + nextLoc + rc.getPaint());
					// 		if (rc.canAttack(nextLoc)){
					// 			rc.attack(nextLoc);
					// 			// rc.move(ourLoc.directionTo(nextLoc));
					// 			// Clock.yield();
					// 		}
					// 	}
					// }

	            } catch (Exception e) {
	                System.out.println(rc.getType() + " Exception");
	                e.printStackTrace();
	            }
           }

        	try {
        		Pathfinding.move(target);

				// MapInfo[] nearby = rc.senseNearbyMapInfos();
				// int num = 0;

				// for (int i=0; i<nearby.length; i++){
				// 	num++;
				// 	MapInfo cur = nearby[i];
				// 	MapLocation nextLoc = cur.getMapLocation();

				// 	if (cur.isPassable() && (cur.getPaint()==PaintType.EMPTY || cur.getPaint().isEnemy())){
				// 		System.out.println("Paintcheck" + num + "  " + nextLoc + rc.getPaint());
				// 		if (rc.canAttack(nextLoc)){
				// 			rc.attack(nextLoc);
				// 			// rc.move(ourLoc.directionTo(nextLoc));
				// 			// Clock.yield();
				// 		}
				// 	}
				// }

        		} catch (Exception e) {
        			System.out.println(rc.getType() + " Exception");
           }
        }
    }

    /**
     * Run a single turn for a Splasher.
     * This code is wrapped inside the infinite loop in run(), so it is called once per turn.
     */



    public static void runSplasher(RobotController rc) throws GameActionException{

    	int coord_x = originalLocation.x;
    	int coord_y = originalLocation.y;

		MapLocation ourLoc = rc.getLocation();

		// Enemy Locations!!!
		MapLocation[] results;
		int map_height = rc.getMapHeight();
		int map_width = rc.getMapWidth();
		// RobotInfo[] enemyRobots = rc.senseNearbyRobots(-1, rc.getTeam().opponent());
		// if (enemyRobots.length >=3){
		// 	rc.setIndicatorString("There are nearby enemy robots! Scary!");
		// 	MapLocation[] enemyLocations = new MapLocation[enemyRobots.length];
		// 	for (int j = 0; j< enemyRobots.length; j++){
		// 		enemyLocations[j] = enemyRobots[j].getLocation();
		// 		if (rc.canAttack(enemyLocations[j])){
		// 			rc.attack(enemyLocations[j]);
		// 		}

		// 	}
		// Clock.yield();
		// }

		if (coord_x < coord_y) {
	    	if (coord_x < map_width / 2) {
	            Random rand = new Random();
	            int randomInd = rand.nextInt(map_height);
	            MapLocation my_target = new MapLocation(map_width, randomInd);

	            Pathfinding.init(rc);
	            Pathfinding.initTurn();
	            Pathfinding.move(my_target);
	    	}
	    	else {
	    		Random rand = new Random();
	            int randomInd = rand.nextInt(map_height);
	            MapLocation my_target = new MapLocation(0, randomInd);

	            Pathfinding.init(rc);
	            Pathfinding.initTurn();
	            Pathfinding.move(my_target);
	    	}

			//UPDATE
			MapInfo[] nearby = rc.senseNearbyMapInfos();
			int num = 0;

			for (int i=0; i<nearby.length; i++){
				num++;
				MapInfo cur = nearby[i];
				MapLocation nextLoc = cur.getMapLocation();

				if (cur.isPassable() && (cur.getPaint()==PaintType.EMPTY || cur.getPaint().isEnemy())){
					System.out.println("Paintcheck" + num + "  " + nextLoc + rc.getPaint());
					if (rc.canAttack(nextLoc)){
						rc.attack(nextLoc);
						// rc.move(ourLoc.directionTo(nextLoc));
						// Clock.yield();
					}
				}
			}
		 	// We can also move our code into different methods or classes to better organize it!
	        updateEnemyRobots(rc);
    	}
    	else {
    		if (coord_y < map_height / 2) {
	            Random rand = new Random();
	            int randomInd = rand.nextInt(map_width);
	            MapLocation my_target = new MapLocation(randomInd, map_height);

	            Pathfinding.init(rc);
	            Pathfinding.initTurn();
	            Pathfinding.move(my_target);
	    	}
	    	else {
	    		Random rand = new Random();
	    		int randomInd = rand.nextInt(map_width);
	            MapLocation my_target = new MapLocation(randomInd, 0);

	            Pathfinding.init(rc);
	            Pathfinding.initTurn();
	            Pathfinding.move(my_target);
	    	}

			// UPDATE
			MapInfo[] nearby = rc.senseNearbyMapInfos();
			int num = 0;


			for (int i=0; i<nearby.length; i++){
				num++;
				MapInfo cur = nearby[i];
				MapLocation nextLoc = cur.getMapLocation();

				if (cur.isPassable() && (cur.getPaint()==PaintType.EMPTY || cur.getPaint().isEnemy())){
					System.out.println("Paintcheck" + num + "  " + nextLoc + rc.getPaint());
					if (rc.canAttack(nextLoc)){
						rc.attack(nextLoc);
						// rc.move(ourLoc.directionTo(nextLoc));
						// Clock.yield();
					}
				}
			}
		 	// We can also move our code into different methods or classes to better organize it!
	        updateEnemyRobots(rc);

    	}



		// MapLocation verticalFlip = new MapLocation(ourLoc.x, height - ourLoc.y - 1);
		// MapLocation horizontalFlip = new MapLocation(width - ourLoc.x - 1, ourLoc.y);
		// MapLocation rotation = new MapLocation(width - ourLoc.x - 1, height - ourLoc.y - 1);
		// results = new MapLocation[] { verticalFlip, horizontalFlip, rotation };

		// int randomIndex = rng.nextInt(results.length);
		// MapLocation my_target = results[randomIndex];
		// Pathfinding.init(rc);
		// Pathfinding.initTurn();
		// Pathfinding.move(my_target);

		// Direction dir = directions[rng.nextInt(directions.length)];
		// MapLocation nextLoc = rc.getLocation().add(dir);
		// if (rc.canAttack(nextLoc)){
		// 	rc.attack(nextLoc);
		// }

		// // We can also move our code into different methods or classes to better organize it!
		// updateEnemyRobots(rc);

		// RobotInfo[] enemyRobots = rc.senseNearbyRobots(-1, rc.getTeam().opponent());
        // if (enemyRobots.length != 0){
        //     rc.setIndicatorString("There are nearby enemy robots! Scary!");
        //     // Save an array of locations with enemy robots in them for possible future use.
        //     MapLocation[] enemyLocations = new MapLocation[enemyRobots.length];
        //     for (int i = 0; i < enemyRobots.length; i++){
        //         enemyLocations[i] = enemyRobots[i].getLocation();
		// 		if (rc.canAttack(nextLoc)){
		// 			rc.attack(nextLoc);
		// 		}
		// 	}
		// }

    }

	public static void updateEnemyRobots(RobotController rc) throws GameActionException{
        // Sensing methods can be passed in a radius of -1 to automatically
        // use the largest possible value.
        RobotInfo[] enemyRobots = rc.senseNearbyRobots(-1, rc.getTeam().opponent());
        if (enemyRobots.length != 0){
            rc.setIndicatorString("There are nearby enemy robots! Scary!");
            // Save an array of locations with enemy robots in them for possible future use.
            MapLocation[] enemyLocations = new MapLocation[enemyRobots.length];
            for (int i = 0; i < enemyRobots.length; i++){
                enemyLocations[i] = enemyRobots[i].getLocation();
            }
            RobotInfo[] allyRobots = rc.senseNearbyRobots(-1, rc.getTeam());
            // Occasionally try to tell nearby allies how many enemy robots we see.
            if (rc.getRoundNum() % 20 == 0){
                for (RobotInfo ally : allyRobots){
                    if (rc.canSendMessage(ally.location, enemyRobots.length)){
                        rc.sendMessage(ally.location, enemyRobots.length);
                    }
                }
            }
        }
    }





}
