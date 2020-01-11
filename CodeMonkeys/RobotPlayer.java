package CodeMonkeys;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import battlecode.common.*;


public strictfp class RobotPlayer {

	static RobotController rc;

    static Direction[] directions = {
        Direction.NORTH,
        Direction.NORTHEAST,
        Direction.EAST,
        Direction.SOUTHEAST,
        Direction.SOUTH,
        Direction.SOUTHWEST,
        Direction.WEST,
        Direction.NORTHWEST
    };
    static RobotType[] spawnedByMiner = {RobotType.REFINERY, RobotType.VAPORATOR, RobotType.DESIGN_SCHOOL,
            RobotType.FULFILLMENT_CENTER, RobotType.NET_GUN};
 
    static int ELEVATION_LIMIT = 25;
    static int MINER_LIMIT = 3;
    static int VAPORATOR_LIMIT = 2;
    static int DESIGN_LIMIT = 2;  
    static int LANDSCAPER_LIMIT = 10;
    static int TURN_LIMIT = 1000;
    static int MINER_SPAWN_RATE = 2;
    
    static int turnCount;
    static int minerCount;
    static int vaporatorCount;
    static int designCount;
    static int landscaperCount;
    
    static MapLocation hqLoc;
    static int hqElevation;
    static MapLocation lastSoupMined;
    static MapLocation depositLoc;
    static int teamSecret = 72151689;
    static HashMap<Direction, Direction> oppositeDirection = new HashMap<>();
    static Direction last;
//    {
//    	oppositeDirection.put(Direction.NORTH, Direction.SOUTH);
//    	oppositeDirection.put(Direction.NORTHEAST, Direction.SOUTHWEST);
//    	oppositeDirection.put(Direction.EAST, Direction.WEST);
//    	oppositeDirection.put(Direction.SOUTHEAST, Direction.NORTHWEST);
//    	oppositeDirection.put(Direction.SOUTH, Direction.NORTH);
//    	oppositeDirection.put(Direction.SOUTHWEST, Direction.NORTHEAST);
//    	oppositeDirection.put(Direction.WEST, Direction.EAST);
//    	oppositeDirection.put(Direction.NORTHWEST, Direction.SOUTHEAST);
//    	
//    }
    static List<Direction> dirs = Arrays.asList(directions);
//    {
//   ;
//    }
    static List<MapLocation> hqNeighbors = new ArrayList<>();
    
    /**
     * run() is the method that is called when a robot is instantiated in the Battlecode world.
     * If this method returns, the robot dies!
     **/
    @SuppressWarnings("unused")
    public static void run(RobotController rc) throws GameActionException {
    	
        // This is the RobotController object. You use it to perform actions from this robot,
        // and to get information on its current status.
        RobotPlayer.rc = rc;
        turnCount = 0;
        {
        	oppositeDirection.put(Direction.NORTH, Direction.SOUTH);
        	oppositeDirection.put(Direction.NORTHEAST, Direction.SOUTHWEST);
        	oppositeDirection.put(Direction.EAST, Direction.WEST);
        	oppositeDirection.put(Direction.SOUTHEAST, Direction.NORTHWEST);
        	oppositeDirection.put(Direction.SOUTH, Direction.NORTH);
        	oppositeDirection.put(Direction.SOUTHWEST, Direction.NORTHEAST);
        	oppositeDirection.put(Direction.WEST, Direction.EAST);
        	oppositeDirection.put(Direction.NORTHWEST, Direction.SOUTHEAST);
        	
        }
        System.out.println(oppositeDirection);
//       System.out.println("I'm a " + rc.getType() + " and I just got created!");
//       System.out.println("and my turn count is " + turnCount);
        while (true) {
        	turnCount += 1;
//            System.out.println("I'm a " + rc.getType() + " and my turn count is " + turnCount);
            // Try/catch blocks stop unhandled exceptions, which cause your robot to explode
            try {
                // Here, we've separated the controls into a different method for each RobotType.
                // You can add the missing ones or rewrite this into your own control structure.
//                System.out.println("I'm a " + rc.getType() + "! Location " + rc.getLocation());
                switch (rc.getType()) {
                    case HQ:                 runHQ();                break;
                    case MINER:              runMiner();             break;
                    case REFINERY:           runRefinery();          break;
                    case VAPORATOR:          runVaporator();         break;
                    case DESIGN_SCHOOL:      runDesignSchool();      break;
                    case FULFILLMENT_CENTER: runFulfillmentCenter(); break;
                    case LANDSCAPER:         runLandscaper();        break;
                    case DELIVERY_DRONE:     runDeliveryDrone();     break;
                    case NET_GUN:            runNetGun();            break;
                }

                // Clock.yield() makes the robot wait until the next turn, then it will perform this loop again
                Clock.yield();

            } catch (Exception e) {
//                System.out.println(rc.getType() + " Exception");
                e.printStackTrace();
            }
        }
    }

    static void runHQ() throws GameActionException {
    	hqElevation = rc.senseElevation(rc.getLocation());
    	if(minerCount < MINER_LIMIT || rc.getRoundNum() > TURN_LIMIT && rc.getRoundNum() % MINER_SPAWN_RATE == 0) {
        for (Direction dir : directions)
            if(tryBuild(RobotType.MINER, dir)) {
            	minerCount += 1;
            }
    	}
    	if (rc.getRoundNum() == 1) {
	    	int[] message = new int[7];
	    	message[0] = teamSecret;
	    	message[6] = rc.getLocation().x;
	    	message[4] = rc.getLocation().y;
        	if (rc.canSubmitTransaction(message, 1)) {
        		rc.submitTransaction(message, 1);
        	}
    	}
    }

    static void runMiner() throws GameActionException {
    	if(hqLoc == null) {
    		// search surroundings for HQ
    		RobotInfo[] robots = rc.senseNearbyRobots();
    		for(RobotInfo robot : robots) {
    			if(robot.type == RobotType.HQ && robot.team == rc.getTeam()) {
    				hqLoc = robot.location;
    				hqElevation = rc.senseElevation(hqLoc);
    			}
    		}
    	}
//      tryBlockchain(rc.getRoundNum());
        // tryBuild(randomSpawnedByMiner(), randomDirection());
//        for (Direction dir : directions)
//            tryBuild(RobotType.FULFILLMENT_CENTER, dir);
    	
//    	if(vaporatorCount >= VAPORATOR_LIMIT && designCount < DESIGN_LIMIT) {
//    		for(Direction dir: directions) {
//    			if(rc.canBuildRobot(RobotType.DESIGN_SCHOOL, dir) && rc.canSenseLocation(hqLoc) && designCount < DESIGN_LIMIT) {
//    				rc.buildRobot(RobotType.DESIGN_SCHOOL, dir);
//    				designCount += 1;
//    			}
//    		}
//    	}
    	
    	if(rc.getRoundNum() < 500) {
//	    	if(rc.getTeamSoup() >= RobotType.VAPORATOR.cost && !rc.canSenseLocation(hqLoc) && vaporatorCount < VAPORATOR_LIMIT) {
//	    		for(Direction dir: directions) {
//	    			if(rc.senseElevation(rc.getLocation()) >= hqElevation && rc.canBuildRobot(RobotType.VAPORATOR, dir)) {
//	    				rc.buildRobot(RobotType.VAPORATOR, dir);
//	    				vaporatorCount += 1;
//	    			}
//	    		}
//	    	}
	    	
	    	if(rc.getTeamSoup() >= RobotType.DESIGN_SCHOOL.cost && !rc.canSenseLocation(hqLoc) && designCount < DESIGN_LIMIT) { // && rc.getRoundNum() > 400) {
	    		for(Direction dir: directions) {
	    			if(rc.senseElevation(rc.getLocation()) >= hqElevation && rc.canBuildRobot(RobotType.DESIGN_SCHOOL, dir)) {
	    				rc.buildRobot(RobotType.DESIGN_SCHOOL, dir);
	    				designCount += 1;
	    			}
	    		}
	    	}
	    	
	        for (Direction dir : directions) {
	            if (tryRefine(dir)) {
	//                System.out.println("I refined soup! " + rc.getTeamSoup());
	            }
	        }
	        for (Direction dir : directions) {
	            if (tryMine(dir)) {
	            	lastSoupMined = rc.getLocation().add(dir);
	//                System.out.println("I mined soup! " + rc.getSoupCarrying());
	            }
	        }
	        if (rc.getSoupCarrying() == rc.getType().soupLimit) {
	//        	System.out.println("at the soup limit: " + rc.getSoupCarrying());
	        	// time to go back to HQ
	        	Direction dirToHQ = rc.getLocation().directionTo(hqLoc);
	        	if(tryMove(dirToHQ)) {
	//        		System.out.println("moved towards HQ");
	        		System.out.println("moved to: " + rc.getLocation());
	        	}
	        }
	        if (lastSoupMined != null && rc.getSoupCarrying() == 0) {
	        	Direction dirToSoup = rc.getLocation().directionTo(lastSoupMined);
	        	if (tryMove(dirToSoup)) {
	        		System.out.println("moved towards last soup");
	        	}
	        	if (rc.getLocation() == lastSoupMined) {
	        		lastSoupMined = null;
	        	}
	        }
	        if (tryMove(randomDirection(last))) {
	        	System.out.println("moved: " + rc.getLocation());
	        }
    	}
    	else if (rc.senseElevation(rc.getLocation()) <= hqElevation) {
        	// otherwise move randomly as usual
//            System.out.println("I moved!");
        	tryMove(randomDirection(last));
        }
    }

    static void runRefinery() throws GameActionException {
        // System.out.println("Pollution: " + rc.sensePollution(rc.getLocation()));
    }

    static void runVaporator() throws GameActionException {

    }

    static void runDesignSchool() throws GameActionException {
    	if(landscaperCount < LANDSCAPER_LIMIT) {
    		for(Direction direction: directions) {
    			if(tryBuild(RobotType.LANDSCAPER, direction)) {
    				landscaperCount += 1;
    			}
    		}
    	}
    }

    static void runFulfillmentCenter() throws GameActionException {
        for (Direction dir : directions)
            tryBuild(RobotType.DELIVERY_DRONE, dir);
    }

    static void runLandscaper() throws GameActionException {
//    	System.out.println(turnCount);
    	if (turnCount == 1) {
    		for (Transaction tx : rc.getBlock(1)) {
    			int[] mess = tx.getMessage();
    			if (mess[0] == teamSecret) {
    				hqLoc = new MapLocation(mess[6], mess[4]);
    			}
    		}
    	}
    	
//    	if(hqLoc == null) {
//   		// search surroundings for HQ
//    		System.out.println("searching for hq....");
//   		RobotInfo[] robots = rc.senseNearbyRobots();
//    		for(RobotInfo robot : robots) {
//    			if(robot.type == RobotType.HQ && robot.team == rc.getTeam()) {
//    				hqLoc = robot.location;
//    	    		for(Direction dir: directions) {
//    	    			hqNeighbors.add(hqLoc.add(dir));
//    	    		}
//    	    		depositLoc = hqNeighbors.remove(0);
//    				System.out.println("my depositLoc is: " + depositLoc);
//    			}
//    		}
//    	}
//    	System.out.println("Im a landscaper and have dirt: " + rc.getDirtCarrying());
// 		checks if it is at HQ    	
    	Direction hqDir = null;
    	Direction behind = null;
    	boolean  atHQ = false;
    	if (atHQ == false) {
	    	RobotInfo[] rob = rc.senseNearbyRobots(2);
	    	for (RobotInfo r : rob) {
		    	if (r.type == RobotType.HQ && r.team == rc.getTeam()) {
		    		for (Direction dir : directions) {
		    			RobotInfo attempt = rc.senseRobotAtLocation(rc.getLocation().add(dir));
		    			if (attempt != null) {
			    			if (attempt.type == RobotType.HQ) {
			    				hqDir = dir;
			    			}
			    		}
		    		}
		    		behind = oppositeDirection.get(hqDir);
		    		atHQ = true;
	    		}
	    	}
	    }
// 		dig dirt behind 
    	if (atHQ == true && rc.getDirtCarrying() < RobotType.LANDSCAPER.dirtLimit) {
    		if (rc.canDigDirt(behind)) { 
    			rc.digDirt(behind);
    		}
    	}
// 		build wall
    	if (rc.getDirtCarrying() >= RobotType.LANDSCAPER.dirtLimit) {
    		rc.depositDirt(Direction.CENTER);
    	}
//		walking to HQ
    	Direction dirToHQ = rc.getLocation().directionTo(hqLoc);
//    	System.out.println(hqLoc);
//    	System.out.println(dirToHQ);
    	if(tryMove(dirToHQ)) {
//        		System.out.println("moved towards HQ");
//    		System.out.println("moved to: " + rc.getLocation());
    	} else {
    		tryMove(randomDirection(last));
    	}
    	
    	
//    	if(depositLoc != null) {
//	    	if(depositLoc.equals(rc.getLocation()) && rc.canDepositDirt(Direction.CENTER)) {
//	    		System.out.println("Depositing dirt at loc: " + rc.getLocation());
//	    		rc.depositDirt(Direction.CENTER);
//	    	}
//	    	
//	    	if(rc.canSenseLocation(depositLoc) && rc.senseElevation(depositLoc) >= ELEVATION_LIMIT) {
//	    		if(!hqNeighbors.isEmpty()) {
//	    			depositLoc = hqNeighbors.remove(0);
//	    		}
//	    	}
//	    		
//	    	
//	    	if(rc.getDirtCarrying() == RobotType.LANDSCAPER.dirtLimit) {
//	    		
//	    		Direction dirToDeposit = rc.getLocation().directionTo(depositLoc);
//	        	if(tryMove(dirToDeposit)) {
//	        		System.out.println("moved towards depositLoc");
//	        	}
//	    	}
//	    	
//	    	else{
//		    	for (Direction dir: directions) {
//			    	if(!rc.canSenseLocation(hqLoc) && rc.canDigDirt(dir)) {
//			    		System.out.println("digging in this direction: " + rc.getLocation().add(dir));
//			    		rc.digDirt(dir);
//			    	}
//		    	}
//	    	}
//	    	Collections.shuffle(dirs);
//	    	for(Direction dir: dirs) {
//	    		tryMove(dir);
//	    	}
//    	}
//    	if(rc.getDirtCarrying() < RobotType.LANDSCAPER.dirtLimit) {
//	    	for (Direction dir: directions) {
//		    	if(rc.canDigDirt(dir)) {
//		    		System.out.println("digging in this direction: " + rc.getLocation().add(dir));
//		    		rc.digDirt(dir);
//		    	}
//	    	}
//    	}
//    	Collections.shuffle(dirs);
//    	for(Direction dir: dirs) {
//    		tryMove(dir);
//    	}
    }

    static void runDeliveryDrone() throws GameActionException {
        Team enemy = rc.getTeam().opponent();
        if (!rc.isCurrentlyHoldingUnit()) {
            // See if there are any enemy robots within striking range (distance 1 from lumberjack's radius)
            RobotInfo[] robots = rc.senseNearbyRobots(GameConstants.DELIVERY_DRONE_PICKUP_RADIUS_SQUARED, enemy);

            if (robots.length > 0) {
                // Pick up a first robot within range
                rc.pickUpUnit(robots[0].getID());
//                System.out.println("I picked up " + robots[0].getID() + "!");
            }
        } else {
            // No close robots, so search for robots within sight radius
            tryMove(randomDirection(last));
        }
    }

    static void runNetGun() throws GameActionException {

    }

    /**
     * Returns a random Direction.
     *
     * @return a random Direction
     */
    static Direction randomDirection(Direction last) {
    	Direction random = directions[(int) (Math.random() * directions.length)];
    	while (true) {
	    	if (random != oppositeDirection.get(last)) {
	    		return random;
	    	} else {
	    		random = directions[(int) (Math.random() * directions.length)];
	    	}
	    }
    }

    /**
     * Returns a random RobotType spawned by miners.
     *
     * @return a random RobotType
     */
    static RobotType randomSpawnedByMiner() {
        return spawnedByMiner[(int) (Math.random() * spawnedByMiner.length)];
    }

    static boolean tryMove() throws GameActionException {
        for (Direction dir : directions)
            if (tryMove(dir))
                return true;
        return false;
        // MapLocation loc = rc.getLocation();
        // if (loc.x < 10 && loc.x < loc.y)
        //     return tryMove(Direction.EAST);
        // else if (loc.x < 10)
        //     return tryMove(Direction.SOUTH);
        // else if (loc.x > loc.y)
        //     return tryMove(Direction.WEST);
        // else
        //     return tryMove(Direction.NORTH);
    }

    /**
     * Attempts to move in a given direction.
     *
     * @param dir The intended direction of movement
     * @return true if a move was performed
     * @throws GameActionException
     */
    static boolean tryMove(Direction dir) throws GameActionException {
        // System.out.println("I am trying to move " + dir + "; " + rc.isReady() + " " + rc.getCooldownTurns() + " " + rc.canMove(dir));
        if (rc.isReady() && rc.canMove(dir) && !rc.senseFlooding(rc.getLocation().add(dir))) {
            rc.move(dir);
            last = dir;
            return true;
        } else return false;
    }

    /**
     * Attempts to build a given robot in a given direction.
     *
     * @param type The type of the robot to build
     * @param dir The intended direction of movement
     * @return true if a move was performed
     * @throws GameActionException
     */
    static boolean tryBuild(RobotType type, Direction dir) throws GameActionException {
        if (rc.isReady() && rc.canBuildRobot(type, dir)) {
            rc.buildRobot(type, dir);
            return true;
        } else return false;
    }

    /**
     * Attempts to mine soup in a given direction.
     *
     * @param dir The intended direction of mining
     * @return true if a move was performed
     * @throws GameActionException
     */
    static boolean tryMine(Direction dir) throws GameActionException {
        if (rc.isReady() && rc.canMineSoup(dir)) {
            rc.mineSoup(dir);
            return true;
        } else return false;
    }

    /**
     * Attempts to refine soup in a given direction.
     *
     * @param dir The intended direction of refining
     * @return true if a move was performed
     * @throws GameActionException
     */
    static boolean tryRefine(Direction dir) throws GameActionException {
        if (rc.isReady() && rc.canDepositSoup(dir)) {
            rc.depositSoup(dir, rc.getSoupCarrying());
            return true;
        } else return false;
    }


    static void tryBlockchain(int round) throws GameActionException {
        if (round < 3) {
            int[] message = new int[7];
            for (int i = 0; i < 7; i++) {
                message[i] = 123;
            }
            if (rc.canSubmitTransaction(message, 10))
                rc.submitTransaction(message, 10);
        }
        // System.out.println(rc.getRoundMessages(turnCount-1));
    }
}
