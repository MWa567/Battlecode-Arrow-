package archer;

import battlecode.common.*;

public class Soldier extends Robot {

    Soldier(RobotController rc) throws GameActionException {
        super(rc);
    }

    void play() throws GameActionException {
        Util.init(rc);
        Explore.init(rc);
        Pathfinding.init(rc);
        Pathfinding.initTurn();

        target = Explore.getExploreTarget();
    	while (true) {
	    	// Sense information about all visible nearby tiles.
		    MapInfo[] nearbyTiles = rc.senseNearbyMapInfos();


		    MapInfo curRuin = null;
		    int ruinDist = 999999;
		    int towerDist = 999999;
		    // Search for a nearby ruin to complete. If ruin spotted, move towards it.
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

            Random random = new Random();
            int randomNumber = random.nextInt(3);

            if (randomNumber == 1) {
            	// Mark the pattern for paint tower
                MapLocation shouldBeMarked = curRuin.getMapLocation().subtract(dir);
                if (rc.senseMapInfo(shouldBeMarked).getMark() == PaintType.EMPTY && rc.canMarkTowerPattern(UnitType.LEVEL_ONE_PAINT_TOWER, targetLoc)){
                	rc.markTowerPattern(UnitType.LEVEL_ONE_PAINT_TOWER, targetLoc);
                    System.out.println("Trying to build a tower at " + targetLoc);
                }
            }
            else {
            	// Mark the pattern for money tower
                MapLocation shouldBeMarked = curRuin.getMapLocation().subtract(dir);
                if (rc.senseMapInfo(shouldBeMarked).getMark() == PaintType.EMPTY && rc.canMarkTowerPattern(UnitType.LEVEL_ONE_MONEY_TOWER, targetLoc)){
                	rc.markTowerPattern(UnitType.LEVEL_ONE_MONEY_TOWER, targetLoc);
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
        if (rc.getPaint() < 30) {
	    	Pathfinding.setTarget(nearestTower);
	    	try {
	    		Pathfinding.move(nearestTower, false);
	    		} catch (Exception e) {
	    			System.out.println(rc.getType() + " Exception");
	    		}
	    	Clock.yield();
	    }
        if (rc.getMovementCooldownTurns() > 10) {
        	Clock.yield();
        }
    	try {
    		Pathfinding.move(target, false);
    		} catch (Exception e) {
    			System.out.println(rc.getType() + " Exception");
    		}
    	}
    }

}
