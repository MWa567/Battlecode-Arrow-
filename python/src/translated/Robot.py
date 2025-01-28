from battlecode25.stubs import *

class Robot:
    rc = None
    turn_count = 0

    map_width, map_height = 0, 0

    my_team = None
    opp_team = None

    indicator = None
    start_round = None

    target = None
    my_target = None
    original_location = None

    # Array containing all the possible movement directions
    directions = [
        Direction.NORTH,
        Direction.NORTHEAST,
        Direction.EAST,
        Direction.SOUTHEAST,
        Direction.SOUTH,
        Direction.SOUTHWEST,
        Direction.WEST,
        Direction.NORTHWEST,
    ]

    def __init__(self, rc: RobotController):
        self.rc = rc
        self.original_location = rc.getLocation()
        Robot.rc = rc
        Robot.my_team = rc.getTeam()
        Robot.opp_team = rc.getTeam().opponent()
        Robot.map_width = rc.getMapWidth()
        Robot.map_height = rc.getMapHeight()
        Robot.turn_count = 0

    def play(self):
        raise NotImplementedError("Subclasses should implement the play method")

    def init_turn(self):
        pass

    def end_turn(self):
        # rc.setIndicatorString("Robot turn")
        Robot.turn_count += 1
        if Robot.start_round != self.rc.getRoundNum():
            # print(f"overran turn from {Robot.start_round} to {self.rc.getRoundNum()}")
            pass

        Clock.yield()
