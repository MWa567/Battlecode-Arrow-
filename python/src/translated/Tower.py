import random
from battlecode25.stubs import *

class Tower:
    FRIENDLY = 0
    ENEMY = 1
    TOWER = 2

    def __init__(self, rc):
        self.rc = rc
        self.random = random.Random()
        self.hasResource = False
        self.friendlyTowers = [None] * battlecode.GameConstants.MAX_NUMBER_OF_TOWERS
        self.enemyTowers = [None] * battlecode.GameConstants.MAX_NUMBER_OF_TOWERS
        if self.rc.get_map_width() >= 40 or self.rc.get_map_height() >= 40:
            self.hasResource = True
        self.turn = 0

    def play(self):
        self.turn += 1
        # Pick a direction to build in
        directions = battlecode.Direction.values()  # Assuming directions is an enum or list of directions
        dir = directions[self.random.randint(0, len(directions) - 1)]
        nextLoc = self.rc.get_location().add(dir)
        ourLoc = self.rc.get_location()

        if self.rc.get_round_num() <= 50:
            if self.rc.can_build_robot(battlecode.UnitType.SOLDIER, nextLoc):
                self.rc.build_robot(battlecode.UnitType.SOLDIER, nextLoc)
                print("BUILT A SOLDIER")
        else:
            randomNumber = self.random.randint(0, 5)
            if randomNumber < 3 and self.rc.get_number_towers() <= 20 and self.rc.get_chips() > 500 and self.rc.can_build_robot(battlecode.UnitType.SOLDIER, nextLoc):
                self.rc.build_robot(battlecode.UnitType.SOLDIER, nextLoc)
                print("BUILT A SOLDIER")
            elif (randomNumber == 2 or randomNumber == 3) and self.rc.get_chips() > 500 and self.rc.can_build_robot(battlecode.UnitType.MOPPER, nextLoc):
                self.rc.build_robot(battlecode.UnitType.MOPPER, nextLoc)
                print("BUILT A MOPPER")
            elif self.rc.get_chips() > 500 and self.rc.can_build_robot(battlecode.UnitType.SPLASHER, nextLoc):
                self.rc.build_robot(battlecode.UnitType.SPLASHER, nextLoc)
                print("BUILT A SPLASHER")

        # Attack Nearby Bots (AOE)
        nearbyRobots = self.rc.sense_nearby_robots()
        if len(nearbyRobots) <= 8:
            for robot in nearbyRobots:
                if robot.get_team() != self.rc.get_team():
                    enemyLoc = robot.location
                    if self.rc.can_attack(enemyLoc):
                        self.rc.attack(enemyLoc)
        else:
            if self.rc.can_attack(None):
                self.rc.attack(None)

        # Update friendly towers array
        if self.turn < 5:
            inArray = False
            for i in range(len(self.friendlyTowers)):
                if self.friendlyTowers[i] is not None and self.friendlyTowers[i] == ourLoc:
                    inArray = True
                    break
                elif self.friendlyTowers[i] is None:
                    self.friendlyTowers[i] = ourLoc
                    inArray = True
                    break

        # Broadcast tower locations
        roundNum = self.rc.get_round_num()
        check = roundNum % 4
        sourceArray = self.friendlyTowers if check < 2 else self.enemyTowers
        startIndex = 0 if check % 2 == 0 else 1

        # Process received messages
        messages = self.rc.read_messages(-1)
        for m in messages:
            encoded = m.get_bytes()
            decoded = self.int2loc(encoded)
            if decoded:
                loc, marker = decoded
                if marker == self.FRIENDLY:
                    self.add_to_array(self.friendlyTowers, loc)
                elif marker == self.ENEMY:
                    self.add_to_array(self.enemyTowers, loc)

    def add_to_array(self, array, loc):
        for i in range(len(array)):
            if array[i] is None:
                array[i] = loc
                break

    @staticmethod
    def loc2int(loc, marker):
        if loc is None:
            return 0  # Null location is encoded as 0
        encodedLoc = ((loc.x + 1) * 64) + (loc.y + 1)  # Encode x and y
        encodedLoc |= (marker << 30)  # Use top 2 bits for marker
        return encodedLoc

    @staticmethod
    def int2loc(encoded):
        if encoded == 0:
            return None  # Null location
        marker = (encoded >> 30) & 0b11  # Extract top 2 bits for marker
        raw = encoded & 0x3FFFFFFF  # Clear top 2 bits
        x = (raw // 64) - 1  # Decode x
        y = (raw % 64) - 1  # Decode y
        loc = battlecode.MapLocation(x, y)
        return loc, marker
