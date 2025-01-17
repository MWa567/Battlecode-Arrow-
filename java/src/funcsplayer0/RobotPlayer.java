package funcsplayer0;

import battlecode.common.*;

import java.util.Random;


/**
 * RobotPlayer is the class that describes your main robot strategy.
 * The run() method inside this class is like your main function: this is what we'll call once your robot
 * is created!
 */
public class RobotPlayer {
    /**
     * We will use this variable to count the number of turns this robot has been alive.
     * You can use static variables like this to save any information you want. Keep in mind that even though
     * these variables are static, in Battlecode they aren't actually shared between your robots.
     */
    static int turnCount = 0;

    /**
     * A random number generator.
     * We will use this RNG to make some random moves. The Random class is provided by the java.util.Random
     * import at the top of this file. Here, we *seed* the RNG with a constant number (6147); this makes sure
     * we get the same sequence of numbers every time this code is run. This is very useful for debugging!
     */
    static final Random rng = new Random(6147);
    static MapLocation target;
    static MapLocation originalLocation = null;
    static MapLocation nearestTower;
    
    static int map_height;
    static int map_width;
    
    static boolean[][] paintTowerPattern = null;
    static boolean[][] moneyTowerPattern = null;
    static boolean[][] defenseTowerPattern = null;
    
    static MapLocation paintingRuinLoc = null;
    static UnitType paintingTowerType = null;

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
        // Hello world! Standard output is very useful for debugging.
        // Everything you say here will be directly viewable in your terminal when you run a match!
        System.out.println("I'm alive");
        
        if (originalLocation == null) {
        	originalLocation = rc.getLocation();
        }
        
        paintTowerPattern = rc.getTowerPattern(UnitType.LEVEL_ONE_PAINT_TOWER);
        moneyTowerPattern = rc.getTowerPattern(UnitType.LEVEL_ONE_MONEY_TOWER);
        defenseTowerPattern = rc.getTowerPattern(UnitType.LEVEL_ONE_DEFENSE_TOWER);
        
        map_height = rc.getMapHeight();
        map_width = rc.getMapWidth();
        
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
                    case MOPPER: runMopper(rc); break;
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

        if (rc.getRoundNum() <= 300 && rc.getNumberTowers() < 20 && rc.getChips() > 500 && rc.canBuildRobot(UnitType.SOLDIER, nextLoc)){
            rc.buildRobot(UnitType.SOLDIER, nextLoc);
            System.out.println("BUILT A SOLDIER");
        }
        
        else if (rc.getRoundNum() > 300 && rc.getChips() > 500 && rc.canBuildRobot(UnitType.SPLASHER, nextLoc)){
            rc.buildRobot(UnitType.SPLASHER, nextLoc);
            System.out.println("BUILT A SPLASHER");
        }
        
        //sarah is really cool
        
        // Read incoming messages
        Message[] messages = rc.readMessages(-1);
        for (Message m : messages) {
            System.out.println("Tower received message: '#" + m.getSenderID() + " " + m.getBytes());
        }
        
        // TODO: attack other bots
        MapInfo[] nearbyTiles = rc.senseNearbyMapInfos();
        for (MapInfo tile : nearbyTiles) {
        	if ((rc.senseRobotAtLocation(tile.getMapLocation()) != null) && (rc.senseRobotAtLocation(tile.getMapLocation()).getTeam() != rc.getTeam())) {
        		MapLocation enemy = tile.getMapLocation();
        		if (rc.canAttack(enemy)) {
        			rc.attack(enemy);
        		}
        	}
        }
    }
    
    public static boolean isWithinPattern(MapLocation ruinLoc, MapLocation paintLoc) {
    	return Math.abs(paintLoc.x - ruinLoc.x) <= 2 && Math.abs(paintLoc.y - ruinLoc.y) <= 2 && !ruinLoc.equals(paintLoc);
    }
    
    public static UnitType getNewTowerType(RobotController rc) {
    	Random random = new Random();
        int randomNumber = random.nextInt(7);

        boolean inMiddleX = 0.3 * map_width < rc.getLocation().x && rc.getLocation().x < 0.7 * map_width;
        boolean inMiddleY = 0.3 * map_height < rc.getLocation().y && rc.getLocation().y < 0.7 * map_height;
        if (inMiddleX && inMiddleY) {
    		return UnitType.LEVEL_ONE_DEFENSE_TOWER;
    	}
        else if (rc.getNumberTowers() < 4) {
        	return UnitType.LEVEL_ONE_MONEY_TOWER;
        }
    	else if (randomNumber == 0 || randomNumber == 1) {
	    	return UnitType.LEVEL_ONE_PAINT_TOWER;
    	}
	    else {
	    	return UnitType.LEVEL_ONE_MONEY_TOWER;
	    }
    }
    
    public static boolean getIsSecondary(MapLocation ruinLoc, MapLocation paintLoc, UnitType towerType) {
    	if (!isWithinPattern(ruinLoc, paintLoc)) return false;
    	int col = paintLoc.x - ruinLoc.x + 2;
    	int row = paintLoc.y - ruinLoc.y + 2;
    	if (towerType == UnitType.LEVEL_ONE_PAINT_TOWER) {
    		return paintTowerPattern[row][col];
    	}
    	else if (towerType == UnitType.LEVEL_ONE_MONEY_TOWER) {
    		return moneyTowerPattern[row][col];
    	}
    	else {
    		return defenseTowerPattern[row][col];
    	}
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
		    int ruinDist = 999999;
		    int towerDist = 999999;
		    for (MapInfo tile : nearbyTiles){
	    		if (tile.hasRuin() && rc.senseRobotAtLocation(tile.getMapLocation()) == null){
	            	int dist = tile.getMapLocation().distanceSquaredTo(rc.getLocation());
	            	if (dist < ruinDist) {
		                curRuin = tile;
		                ruinDist = dist;
	            	}
	            }
	    		else if (tile.hasRuin() && rc.senseRobotAtLocation(tile.getMapLocation()) != null) {
	    			int dist = tile.getMapLocation().distanceSquaredTo(rc.getLocation());
	    			if (dist < towerDist) {
	    				nearestTower = tile.getMapLocation();
	    				towerDist = dist;
	    			}
	    			if (rc.getPaint() < 30) {
	    				if (rc.canTransferPaint(tile.getMapLocation(), -100)) {
	    					rc.transferPaint(tile.getMapLocation(), -100);
	    				}
	    			}
	    			if (rc.canUpgradeTower(tile.getMapLocation())) {
	    				rc.upgradeTower(tile.getMapLocation());
	    				System.out.println("Tower was upgraded!");
	    			}
	    		}
	    	}
	    
		    if (curRuin != null){
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
	        	else if (rc.canCompleteTowerPattern(UnitType.LEVEL_ONE_DEFENSE_TOWER, targetLoc)){
	                rc.completeTowerPattern(UnitType.LEVEL_ONE_DEFENSE_TOWER, targetLoc);
	                rc.setTimelineMarker("Tower built", 0, 255, 0);
	                System.out.println("Built a defense tower at " + targetLoc + "!");
	                }
	        	
	            Direction dir = rc.getLocation().directionTo(targetLoc);
	            
	            if (rc.getMovementCooldownTurns() > 10) {
	            	// Do nothing
	            }
	            else if (rc.canMove(dir)) {
	            	rc.move(dir);
	            }
	            else if (rc.canMove(dir.rotateRight())){
	            	rc.move(dir.rotateRight());
	            }
	            
	            UnitType randomTower = getNewTowerType(rc);
	            
	            if (randomTower == UnitType.LEVEL_ONE_PAINT_TOWER) {
	            	// Mark the pattern for paint tower
	                MapLocation shouldBeMarked = curRuin.getMapLocation().subtract(dir);
	                if (rc.senseMapInfo(shouldBeMarked).getMark() == PaintType.EMPTY && rc.canMarkTowerPattern(UnitType.LEVEL_ONE_PAINT_TOWER, targetLoc)){
	                	rc.markTowerPattern(UnitType.LEVEL_ONE_PAINT_TOWER, targetLoc);
	                    System.out.println("Trying to build a tower at " + targetLoc);
	                }
	            }
	            else if (randomTower == UnitType.LEVEL_ONE_MONEY_TOWER) {
	            	// Mark the pattern for money tower
	                MapLocation shouldBeMarked = curRuin.getMapLocation().subtract(dir);
	                if (rc.senseMapInfo(shouldBeMarked).getMark() == PaintType.EMPTY && rc.canMarkTowerPattern(UnitType.LEVEL_ONE_MONEY_TOWER, targetLoc)){
	                	rc.markTowerPattern(UnitType.LEVEL_ONE_MONEY_TOWER, targetLoc);
	                    System.out.println("Trying to build a tower at " + targetLoc);
	                }
	            }
	            else {
	            	// Mark the pattern for defense tower
	                MapLocation shouldBeMarked = curRuin.getMapLocation().subtract(dir);
	                if (rc.senseMapInfo(shouldBeMarked).getMark() == PaintType.EMPTY && rc.canMarkTowerPattern(UnitType.LEVEL_ONE_DEFENSE_TOWER, targetLoc)){
	                	rc.markTowerPattern(UnitType.LEVEL_ONE_DEFENSE_TOWER, targetLoc);
	                    System.out.println("Trying to build a tower at " + targetLoc);
	                }
	            }
	            
	            // Fill in any spots in the pattern with the appropriate paint.
	            for (MapInfo patternTile : rc.senseNearbyMapInfos(targetLoc, -1)){
	                if (patternTile.getMark() != patternTile.getPaint() && patternTile.getMark() != PaintType.EMPTY){
	                    boolean useSecondaryColor = patternTile.getMark() == PaintType.ALLY_SECONDARY;
	                    if (rc.canAttack(patternTile.getMapLocation()))
	                        rc.attack(patternTile.getMapLocation(), useSecondaryColor);
	                }
	            }
	        }
		    
		    if (rc.getPaint() < 50) {
	        	System.out.println("GOING BACK FOR MORE PAINT");
		    	Pathfinding.setTarget(nearestTower);
		    	try {
		    		Pathfinding.move(nearestTower, false);
		    		} catch (Exception e) {
		    			System.out.println(rc.getType() + " Exception");
		    		}
		    }
        
	        if (rc.getMovementCooldownTurns() > 10) {
	        	Clock.yield();
	        }
	        if (curRuin == null) {
	        	try {
		    		Pathfinding.move(target, false);
		    		rc.setIndicatorString("Moving toward target at "+ Pathfinding.target);
	    		}
	        	catch (Exception e) {
	    			System.out.println(rc.getType() + " Exception");
	    		}
	        }
		    Clock.yield();
    	}
    }

    /**
     * Run a single turn for a Splasher.
     * This code is wrapped inside the infinite loop in run(), so it is called once per turn.
     */
    public static void runSplasher(RobotController rc) throws GameActionException{
    	
    	int coord_x = originalLocation.x;
    	int coord_y = originalLocation.y;
    	
    	if (coord_x < coord_y) {
	    	if (coord_x < map_width / 2) {
	            Random rand = new Random();
	            int randomInd = rand.nextInt(map_height);
	            MapLocation my_target = new MapLocation(map_width, randomInd);
	            
	            Pathfinding.init(rc);
	            Pathfinding.initTurn();
	            Pathfinding.move(my_target, true);
	    	}
	    	else {
	    		Random rand = new Random();
	            int randomInd = rand.nextInt(map_height);
	            MapLocation my_target = new MapLocation(0, randomInd);
	            
	            Pathfinding.init(rc);
	            Pathfinding.initTurn();
	            Pathfinding.move(my_target, true);
	    	}
	        updateEnemyRobots(rc);
    	}
    	else {
    		if (coord_y < map_height / 2) {
	            Random rand = new Random();
	            int randomInd = rand.nextInt(map_width);
	            MapLocation my_target = new MapLocation(randomInd, map_height);
	            
	            Pathfinding.init(rc);
	            Pathfinding.initTurn();
	            Pathfinding.move(my_target, true);
	    	}
	    	else {
	    		Random rand = new Random();
	    		int randomInd = rand.nextInt(map_width);
	            MapLocation my_target = new MapLocation(randomInd, 0);
	            
	            Pathfinding.init(rc);
	            Pathfinding.initTurn();
	            Pathfinding.move(my_target, true);
	    	}
			// We can also move our code into different methods or classes to better organize it!
	        updateEnemyRobots(rc);
        }
    }
    
    public static void runMopper(RobotController rc) throws GameActionException {
    	while (true) {
    	    MapInfo[] nearbyTiles = rc.senseNearbyMapInfos();
    	    
    	    MapLocation ourLoc = rc.getLocation();
    	    //If we can see enemy paint, move towards it
    	    for (MapInfo tile : nearbyTiles){
    	        if (tile.getPaint().isEnemy()){
    	            MapLocation targetLoc = tile.getMapLocation();
    	            Direction my_dir = ourLoc.directionTo(targetLoc);
    	            if (rc.canAttack(targetLoc)){
    	                rc.attack(targetLoc);
    	            }
    	            if (rc.canMove(my_dir)) {
    	                rc.move(my_dir);
    	                ourLoc.add(my_dir);
    	                Clock.yield();
    	            }
    	        }
    	    }
    	    
    	    RobotInfo[] nearbyRobots = rc.senseNearbyRobots();
    	    //If we can see a soldier, follow it
    	    for (RobotInfo robo : nearbyRobots){
    	        if (robo.type==UnitType.SOLDIER){
    	            MapLocation targetLoc = robo.location;
    	            Direction my_dir = rc.getLocation().directionTo(targetLoc);

    	            if (rc.canMopSwing(my_dir) && robo.team!=rc.getTeam()){
    	                rc.mopSwing(my_dir);
    	            }
    	            if (rc.canMove(my_dir)) {
    	                rc.move(my_dir);
    	                Clock.yield();
    	            }
    	        }
    	    }
    	    
    	    int coord_x = originalLocation.x;
        	int coord_y = originalLocation.y;
        	
        	if (coord_x < coord_y) {
    	    	if (coord_x < map_width / 2) {
    	            Random rand = new Random();
    	            int randomInd = rand.nextInt(map_height);
    	            MapLocation my_target = new MapLocation(map_width, randomInd);
    	            
    	            Pathfinding.init(rc);
    	            Pathfinding.initTurn();
    	            Pathfinding.move(my_target, true);
    	    	}
    	    	else {
    	    		Random rand = new Random();
    	            int randomInd = rand.nextInt(map_height);
    	            MapLocation my_target = new MapLocation(0, randomInd);
    	            
    	            Pathfinding.init(rc);
    	            Pathfinding.initTurn();
    	            Pathfinding.move(my_target, true);
    	    	}
    	        updateEnemyRobots(rc);
        	}
        	else {
        		if (coord_y < map_height / 2) {
    	            Random rand = new Random();
    	            int randomInd = rand.nextInt(map_width);
    	            MapLocation my_target = new MapLocation(randomInd, map_height);
    	            
    	            Pathfinding.init(rc);
    	            Pathfinding.initTurn();
    	            Pathfinding.move(my_target, true);
    	    	}
    	    	else {
    	    		Random rand = new Random();
    	    		int randomInd = rand.nextInt(map_width);
    	            MapLocation my_target = new MapLocation(randomInd, 0);
    	            
    	            Pathfinding.init(rc);
    	            Pathfinding.initTurn();
    	            Pathfinding.move(my_target, true);
    	    	}
    			// We can also move our code into different methods or classes to better organize it!
    	        updateEnemyRobots(rc);
            }
    	}
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