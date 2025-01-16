package archer;

import battlecode.common.*;
import java.util.AbstractMap.SimpleEntry;
import java.util.Map;
import java.util.Random;


public class Tower extends Robot {
    private static final int FRIENDLY = 0;
    private static final int ENEMY = 1;
    private static final int TOWER = 2;

    private MapLocation[] friendlyTowers = new MapLocation[GameConstants.MAX_NUMBER_OF_TOWERS];
    private MapLocation[] enemyTowers = new MapLocation[GameConstants.MAX_NUMBER_OF_TOWERS];

    Tower(RobotController rc) throws GameActionException {
        super(rc);
    }

    static final Random rng = new Random(6147);
    int turn = 0;
    void play() throws GameActionException {
        turn += 1;
        // Pick a direction to build in.
        Direction dir = directions[rng.nextInt(directions.length)];
        MapLocation nextLoc = rc.getLocation().add(dir);
        MapLocation ourLoc = rc.getLocation();

        if (rc.getRoundNum() <= 25 && rc.canBuildRobot(UnitType.MOPPER, nextLoc)){
            if (rc.canBuildRobot(UnitType.MOPPER, nextLoc)){
                rc.buildRobot(UnitType.MOPPER, nextLoc);
                System.out.println("BUILT A MOPPER");
            }
        }
        else if (rc.getRoundNum() <= 300 && rc.getNumberTowers() < 20 && rc.getChips() > 300){
            int num = rng.nextInt(2);
            if (num == 0 && rc.canBuildRobot(UnitType.SOLDIER, nextLoc)){
                rc.buildRobot(UnitType.SOLDIER, nextLoc);
                System.out.println("BUILT A SOLDIER");
            }
            else if (num==1 && rc.canBuildRobot(UnitType.MOPPER, nextLoc)){
                rc.buildRobot(UnitType.MOPPER, nextLoc);
                System.out.println("BUILT A MOPPER");
            }
        }
        else if (rc.getRoundNum() > 300 && rc.getChips() > 300 && rc.canBuildRobot(UnitType.SPLASHER, nextLoc)){
            int num = rng.nextInt(5);
            if (num == 0 && rc.canBuildRobot(UnitType.MOPPER, nextLoc)){
                rc.buildRobot(UnitType.MOPPER, nextLoc);
                System.out.println("BUILT A MOPPER");
            }
            else if (rc.canBuildRobot(UnitType.SPLASHER, nextLoc)){
                rc.buildRobot(UnitType.SPLASHER, nextLoc);
                System.out.println("BUILT A SPLASHER");
            }
        }


        // Update friendly towers array
        if (turn < 5) {
            boolean inArray = false;
            for (int i = 0; i < friendlyTowers.length; i++) {
                if (friendlyTowers[i] != null && friendlyTowers[i].equals(ourLoc)) {
                    inArray = true;
                    break;
                } else if (friendlyTowers[i] == null) {
                    friendlyTowers[i] = ourLoc;
                    inArray = true;
                    break;
                }
            }
        }

        // Broadcast tower locations
        int roundNum = rc.getRoundNum();
        int check = roundNum % 4;
        MapLocation[] sourceArray = (check < 2) ? friendlyTowers : enemyTowers;
        int startIndex = (check % 2 == 0) ? 0 : 1;

        for (int i = startIndex; i < sourceArray.length; i += 2) {
            if (sourceArray[i] != null && rc.canBroadcastMessage()) {
                int marker = (check < 2) ? FRIENDLY : ENEMY;
                rc.broadcastMessage(loc2int(sourceArray[i], marker));
            }
        }

        // Process received messages
        Message[] messages = rc.readMessages(-1);
        for (Message m : messages) {
            int encoded = m.getBytes();
            Map.Entry<MapLocation, Integer> decoded = int2loc(encoded);
            if (decoded != null) {
                MapLocation loc = decoded.getKey();
                int marker = decoded.getValue();

                if (marker == FRIENDLY) {
                    addToArray(friendlyTowers, loc);
                } else if (marker == ENEMY) {
                    addToArray(enemyTowers, loc);
                }
            }
        }

        // Attack Nearby Bots
        // Update: AOE
        RobotInfo[] nearbyRobots = rc.senseNearbyRobots();
        if (nearbyRobots.length <= 8){
            for (RobotInfo robot : nearbyRobots) {
                if (robot.getTeam() != myTeam){
                    MapLocation enemyLoc = robot.location;
                    if (rc.canAttack(enemyLoc)){
                        rc.attack(enemyLoc);
                    }
                }
            }
        }
        else{
            if (rc.canAttack(null)){
                rc.attack(null);
            }
        }
    }

    private void addToArray(MapLocation[] array, MapLocation loc) {
        for (int i = 0; i < array.length; i++) {
            if (array[i] == null) {
                array[i] = loc;
                break;
            }
        }
    }

    private static int loc2int(MapLocation loc, int marker) {
        if (loc == null)
            return 0; // Null location is encoded as 0
        int encodedLoc = ((loc.x + 1) * 64) + (loc.y + 1); // Encode x and y
        encodedLoc |= (marker << 30); // Use top 2 bits for marker
        return encodedLoc;
    }

    private static Map.Entry<MapLocation, Integer> int2loc(int encoded) {
        if (encoded == 0)
            return null; // Null location
        int marker = (encoded >> 30) & 0b11; // Extract top 2 bits for marker
        int raw = encoded & 0x3FFFFFFF; // Clear top 2 bits
        int x = (raw / 64) - 1; // Decode x
        int y = (raw % 64) - 1; // Decode y
        MapLocation loc = new MapLocation(x, y);
        return new SimpleEntry<>(loc, marker);
    }
}
