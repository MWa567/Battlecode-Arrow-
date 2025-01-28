from battlecode25.stubs import *

INF = 1000000
MAX_MAP_SIZE = GameConstants.MAP_MAX_HEIGHT

class BugNav:
    def __init__(self, rc):
        self.rc = rc
        self.target = None
        self.impassable = None
        self.should_guess_rotation = True  # if I should guess which rotation is the best
        self.rotate_right = True  # if I should rotate right or left
        self.last_obstacle_found = None  # latest obstacle I've found in my way
        self.min_dist_to_enemy = INF  # minimum distance I've been to the enemy while going around an obstacle
        self.prev_target = None  # previous target
        self.visited = set()
        self.id = 12620
        self.dont_move = set()

    directions = [
        Direction.NORTH,
        Direction.NORTHEAST,
        Direction.EAST,
        Direction.SOUTHEAST,
        Direction.SOUTH,
        Direction.SOUTHWEST,
        Direction.WEST,
        Direction.NORTHWEST,
        Direction.CENTER
    ]

    def init_turn(self):
        self.impassable = [False] * len(self.directions)

    def move(self, loc):
        if not self.rc.isMovementReady():
            return
        self.init_turn()
        self.target = loc
        self.nav(self.target)

    def can_move(self, dir):
        if not self.rc.canMove(dir):
            return False
        if self.impassable[dir.ordinal()]:
            return False
        return True

    def get_base_movement_cooldown(self):
        return 10

    def distance(self, A, B):
        return max(abs(A.x - B.x), abs(A.y - B.y))

    def get_code(self):
        x = self.rc.getLocation().x
        y = self.rc.getLocation().y
        obstacle_dir = self.rc.getLocation().directionTo(self.target)
        if self.last_obstacle_found is not None:
            obstacle_dir = self.rc.getLocation().directionTo(self.last_obstacle_found)
        bit = 1 if self.rotate_right else 0
        return (((((x << 6) | y) << 4) | obstacle_dir.ordinal()) << 1) | bit

    def nav(self, loc):
        self.target = loc
        try:
            # Different target? ==> previous data does not help!
            if self.prev_target is None or self.distance(self.rc.getLocation(), self.prev_target) > 0:
                self.reset_pathfinding()

            # If I'm at a minimum distance to the target, I'm free!
            my_loc = self.rc.getLocation()
            d = self.distance(my_loc, self.target)
            if d < self.min_dist_to_enemy:
                self.reset_pathfinding()
                self.min_dist_to_enemy = d

            code = self.get_code()

            if code in self.visited:
                self.reset_pathfinding()
            self.visited.add(code)

            # Update data
            self.prev_target = self.target

            # If there's an obstacle I try to go around it [until I'm free] instead of
            # going to the target directly
            dir = my_loc.directionTo(self.target)
            if self.last_obstacle_found is not None:
                dir = my_loc.directionTo(self.last_obstacle_found)

            if self.can_move(dir):
                self.reset_pathfinding()

            # I rotate clockwise or counterclockwise (depends on 'rotate_right'). If I try
            # to go out of the map I change the orientation
            for _ in range(8):
                new_loc = my_loc.add(dir)
                if self.rc.canSenseLocation(new_loc):
                    if self.can_move(dir):
                        self.rc.move(dir)
                        return True
                if not self.rc.onTheMap(new_loc):
                    self.rotate_right = not self.rotate_right
                elif self.rc.senseRobotAtLocation(new_loc) is not None:
                    pass  # Do nothing if a robot is there
                elif not self.rc.sensePassability(new_loc):
                    self.last_obstacle_found = new_loc
                    if self.should_guess_rotation:
                        self.should_guess_rotation = False
                        # Rotate left and right and find the first dir that you can move in
                        dir_l = dir
                        for _ in range(8):
                            if self.can_move(dir_l):
                                break
                            dir_l = dir_l.rotateLeft()

                        dir_r = dir
                        for _ in range(8):
                            if self.can_move(dir_r):
                                break
                            dir_r = dir_r.rotateRight()

                        # Check which results in a location closer to the target
                        loc_l = my_loc.add(dir_l)
                        loc_r = my_loc.add(dir_r)

                        l_dist = self.distance(self.target, loc_l)
                        r_dist = self.distance(self.target, loc_r)
                        l_dist_sq = self.target.distanceSquaredTo(loc_l)
                        r_dist_sq = self.target.distanceSquaredTo(loc_r)

                        if l_dist < r_dist:
                            self.rotate_right = False
                        elif r_dist < l_dist:
                            self.rotate_right = True
                        else:
                            self.rotate_right = r_dist_sq < l_dist_sq

                if self.rotate_right:
                    dir = dir.rotateRight()
                else:
                    dir = dir.rotateLeft()

            if self.can_move(dir):
                self.rc.move(dir)
        except Exception as e:
            print(e)
        return True

    def reset_pathfinding(self):
        self.last_obstacle_found = None
        self.min_dist_to_enemy = INF
        self.visited.clear()
        self.should_guess_rotation = True
