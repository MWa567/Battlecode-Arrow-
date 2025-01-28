class MapRecorder:
    vals = []  # Store map values (e.g., seen/unseen, etc.)
    SEEN_BIT = 1  # Bit mask for seen locations
    CURRENT_MASK = 0b1111  # Current state of the map (e.g., bits for terrain, obstacles)

    map_width = 0
    map_height = 0

    @staticmethod
    def init_map(map_width, map_height):
        MapRecorder.map_width = map_width
        MapRecorder.map_height = map_height
        MapRecorder.vals = [0] * (map_width * map_height)

    @staticmethod
    def mark_as_seen(loc):
        index = loc.x * MapRecorder.map_height + loc.y  # Use mapHeight directly
        MapRecorder.vals[index] |= MapRecorder.SEEN_BIT  # Set the seen bit for this location

    @staticmethod
    def get_location_value(loc):
        index = loc.x * MapRecorder.map_height + loc.y  # Use mapHeight directly
        return MapRecorder.vals[index]
