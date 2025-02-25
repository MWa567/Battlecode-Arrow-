package funcsplayer2;

import java.util.Random;
import java.lang.reflect.Array;
import java.util.ArrayList;

import battlecode.common.*;

public class Mopper extends Robot {
	static MapLocation nearestTower;
	static MapLocation my_target;
	static boolean reached_target = false;
	static ArrayList<MapLocation> ruins = new ArrayList<>();
	static ArrayList<MapLocation> visited = new ArrayList<>();
	
    Mopper(RobotController rc) throws GameActionException {
        super(rc);
        MapRecorder.initMap(mapWidth, mapHeight);
        my_target = rc.getLocation();
    }

    void play() throws GameActionException {
    	funcsplayer2.Util.init(rc);
    	funcsplayer2.BugNav.init(rc);
    	funcsplayer2.Pathfinding.init(rc);
    	funcsplayer2.Pathfinding.initTurn();
        while (true) {
        	RobotInfo[] nearbyRobots = rc.senseNearbyRobots();
            //If we can see a soldier, follow it
            for (RobotInfo robo : nearbyRobots){
            	if (robo.team != rc.getTeam() && (robo.type == UnitType.SOLDIER || robo.type == UnitType.SPLASHER)) {
	                my_target = robo.location;
	                Direction dir = rc.getLocation().directionTo(my_target);
	                if (rc.canMopSwing(dir) && robo.team!=rc.getTeam()){
	                    rc.mopSwing(dir);
	                    funcsplayer2.Pathfinding.move(my_target, false);
		                Clock.yield();
	                }
            	}
            }
            
        	MapInfo[] nearbyTiles = rc.senseNearbyMapInfos();
    	    // Search for a nearby ruin to complete.

    	    for (MapInfo tile : nearbyTiles){
    	    	if (tile.hasRuin()){
    	    		if (!ruins.contains(tile.getMapLocation())) {
    	    			for (MapLocation symmetricRuin: getSymmetry(tile.getMapLocation())) {
    	    				if (!ruins.contains(symmetricRuin)) {
    	    					ruins.add(symmetricRuin);
    	    					Clock.yield();
    	    				}
    	    			}
    	    		}
    	    	}
	    		if (tile.getPaint().isEnemy()){
	                MapLocation targetLoc = tile.getMapLocation();
	                if (rc.canAttack(targetLoc)){
	                	rc.attack(targetLoc);
	                }
	    		}
        	}
    	    
    	    
    	    if (ruins.isEmpty()) {
    	    	ruins = visited;
    	    	visited = new ArrayList<>();
    	    }
    	    
    	    if (funcsplayer2.Util.distance(rc.getLocation(), my_target) <= 5) {
    	    	ruins.remove(my_target);
    	    	visited.add(my_target);
    			int randomIndex = (int) (Math.random() * ruins.size());
    			my_target = ruins.get(randomIndex);
    			Clock.yield();
    		}
    		rc.setIndicatorString("Moving toward target at "+ target);
    		funcsplayer2.Pathfinding.move(my_target, false);
    	    Clock.yield();
        }
    }

    void eliminateSymmetry(MapLocation myLocation) throws GameActionException {
        // Check visible tiles for symmetry elimination
        MapInfo[] infos = rc.senseNearbyMapInfos();
        for (MapInfo info : infos) {
            if (!Comm.isSymmetryConfirmed) {
                MapLocation loc = info.getMapLocation();
                char val = MapRecorder.vals[loc.x * rc.getMapHeight() + loc.y];
                if ((val & MapRecorder.SEEN_BIT) != 0) {
                    for (int sym = 0; sym < 3; sym++) {
                        if (!Comm.isSymEliminated[sym] && !validateSymmetry(myLocation, loc, sym)) {
                            Comm.eliminateSym(sym);
                        }
                    }
                }
            }
        }
    }
    
    public static MapLocation[] getSymmetry(MapLocation loc) throws GameActionException {
    	int x = loc.x;
    	int y = loc.y;
    	MapLocation horizontal = new MapLocation(mapWidth - x - 1, y);
    	MapLocation vertical = new MapLocation(x, mapHeight - y - 1);
    	MapLocation diagonal = new MapLocation(y, x);
    	MapLocation antiDiagonal = new MapLocation(mapWidth - y - 1, mapHeight - x - 1);
    	MapLocation[] symmetries = {horizontal, vertical, diagonal, antiDiagonal};
    	return symmetries;
    }
    
    public static void getTarget() throws GameActionException {
    	int coord_x = originalLocation.x;
    	int coord_y = originalLocation.y;
    	
    	funcsplayer2.Pathfinding.init(rc);
    	funcsplayer2.Pathfinding.initTurn();
    	if (coord_x < coord_y) {
	    	if (coord_x < mapWidth / 2) {
	            Random rand = new Random();
	            int randomInd = rand.nextInt(mapHeight);
	            my_target = new MapLocation(mapWidth - 1, randomInd);
	    	}
	    	else {
	    		Random rand = new Random();
	            int randomInd = rand.nextInt(mapHeight);
	            my_target = new MapLocation(0, randomInd);
	    	}
    	}
    	else {
    		if (coord_y < mapHeight / 2) {
	            Random rand = new Random();
	            int randomInd = rand.nextInt(mapWidth);
	            my_target = new MapLocation(randomInd, mapHeight - 1);
	    	}
	    	else {
	    		Random rand = new Random();
	    		int randomInd = rand.nextInt(mapWidth);
	            my_target = new MapLocation(randomInd, 0);
	    	}
			// We can also move our code into different methods or classes to better organize it!
        }
    }

    boolean validateSymmetry(MapLocation origin, MapLocation test, int sym) {
        int mapWidth = rc.getMapWidth();
        int mapHeight = rc.getMapHeight();

        // Calculate symmetric location
        MapLocation symLoc = switch (sym) {
            case Comm.SYM_ROTATIONAL -> new MapLocation(mapWidth - 1 - test.x, mapHeight - 1 - test.y);
            case Comm.SYM_VERTICAL -> new MapLocation(mapWidth - 1 - test.x, test.y);
            case Comm.SYM_HORIZONTAL -> new MapLocation(test.x, mapHeight - 1 - test.y);
            default -> null;
        };

        if (symLoc == null) return false;

        // Validate symmetry
        char originalVal = MapRecorder.vals[origin.x * mapHeight + origin.y];
        char symVal = MapRecorder.vals[symLoc.x * mapHeight + symLoc.y];
        return (originalVal & MapRecorder.CURRENT_MASK) == (symVal & MapRecorder.CURRENT_MASK);
    }

    MapLocation findEnemyTower() throws GameActionException {
        Team enemyTeam = rc.getTeam().opponent();
        RobotInfo[] enemyRobots = rc.senseNearbyRobots(-1, enemyTeam);
        MapLocation closestTarget = null;
        int closestDist = Integer.MAX_VALUE;

        // Prioritize enemy towers
        for (RobotInfo robot : enemyRobots) {
            if (robot.type.isTowerType()) {
                int dist = rc.getLocation().distanceSquaredTo(robot.location);
                if (dist < closestDist) {
                    closestTarget = robot.location;
                    closestDist = dist;
                }
            }
        }

        // Use symmetry scouting if no direct enemy structure found
        if (closestTarget == null && !Comm.isSymmetryConfirmed) {
            for (int sym = 0; sym < 3; sym++) {
                if (!Comm.isSymEliminated[sym]) {
                    MapLocation potentialTarget = calculateSymmetricLocation(rc.getLocation(), sym);
                    if (rc.onTheMap(potentialTarget)) {
                        closestTarget = potentialTarget;
                        break;
                    }
                }
            }
        }
        return closestTarget;
    }

    MapLocation calculateSymmetricLocation(MapLocation loc, int sym) {
        int mapWidth = rc.getMapWidth();
        int mapHeight = rc.getMapHeight();
        return switch (sym) {
            case Comm.SYM_ROTATIONAL -> new MapLocation(mapWidth - 1 - loc.x, mapHeight - 1 - loc.y);
            case Comm.SYM_VERTICAL -> new MapLocation(mapWidth - 1 - loc.x, loc.y);
            case Comm.SYM_HORIZONTAL -> new MapLocation(loc.x, mapHeight - 1 - loc.y);
            default -> loc;
        };
    }

    void randomMove() throws GameActionException {
        // Fallback movement if no specific target is found
        Direction randomDir = Direction.values()[(int) (Math.random() * Direction.values().length)];
        if (rc.canMove(randomDir)) {
            rc.move(randomDir);
        }
    }
}