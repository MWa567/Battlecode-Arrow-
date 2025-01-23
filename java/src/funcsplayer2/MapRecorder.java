package funcsplayer2;

import battlecode.common.*;

public class MapRecorder {
    public static char[] vals; // Store map values (e.g., seen/unseen, etc.)
    public static final char SEEN_BIT = 1; // Bit mask for seen locations
    public static final char CURRENT_MASK = 0b1111; // Current state of the map (e.g., bits for terrain, obstacles)

    private static int mapWidth;
    private static int mapHeight;

    // Initializes map data, typically called during initialization
    public static void initMap(int mapWidth, int mapHeight) {
        MapRecorder.mapWidth = mapWidth;
        MapRecorder.mapHeight = mapHeight;
        vals = new char[mapWidth * mapHeight];
    }

    // Record that a location has been seen
    public static void markAsSeen(MapLocation loc) {
        int index = loc.x * mapHeight + loc.y; // Use mapHeight directly
        vals[index] |= SEEN_BIT; // Set the seen bit for this location
    }

    // Get the current state of a location (useful for symmetry checking)
    public static char getLocationValue(MapLocation loc) {
        int index = loc.x * mapHeight + loc.y; // Use mapHeight directly
        return vals[index];
    }
}