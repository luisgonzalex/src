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
    static int droneCount = 0;
    
    static MapLocation[] enemyHQLocs;
    static MapLocation enemyHqLoc;
    static MapLocation hqLoc;
    static int hqElevation;
    static MapLocation lastSoupMined;
    static MapLocation depositLoc;
    static int teamSecret = 72151689;
    static int vaporatorSecret = 721516891;
    static int netgunSecret = 721516892;
    static HashMap<Direction, Direction> oppositeDirection = new HashMap<>();
    static HashMap<Direction, Direction[]> alternateDirs = new HashMap<>();
    static Direction last;
    static MapLocation d; //for sensing soup
    
    static MapLocation loc;
    static MapLocation fcLoc;
	static MapLocation vap1Loc;
	static MapLocation vap2Loc;
	static MapLocation vap3Loc;
	static MapLocation dsLoc;
	static MapLocation ng1Loc;
	static MapLocation ng2Loc;
	static MapLocation ng3Loc;
	
	static boolean buildingMiner = false;
	static boolean protectorDrone = false;
	
	static boolean adjacent = false;
	
	static boolean fcBuilt = false;
	static boolean vap1Built = false;
	static boolean vap2Built = false;
	static boolean vap3Built = false;
	static boolean dsBuilt = false;
	static boolean ng1Built = false;
	static boolean ng2Built = false;
	static boolean ng3Built = false;
	static boolean canBuild = false;


    
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

        oppositeDirection.put(Direction.NORTH, Direction.SOUTH);
        oppositeDirection.put(Direction.NORTHEAST, Direction.SOUTHWEST);
        oppositeDirection.put(Direction.EAST, Direction.WEST);
       	oppositeDirection.put(Direction.SOUTHEAST, Direction.NORTHWEST);
       	oppositeDirection.put(Direction.SOUTH, Direction.NORTH);
       	oppositeDirection.put(Direction.SOUTHWEST, Direction.NORTHEAST);
       	oppositeDirection.put(Direction.WEST, Direction.EAST);
       	oppositeDirection.put(Direction.NORTHWEST, Direction.SOUTHEAST);
       	
       	Direction[] north = {Direction.NORTHWEST, Direction.NORTHEAST};
       	Direction[] northeast = {Direction.NORTH, Direction.EAST};
       	Direction[] east = {Direction.NORTHEAST, Direction.SOUTHEAST};
       	Direction[] southeast = {Direction.SOUTH, Direction.EAST};
       	Direction[] south = {Direction.SOUTHEAST, Direction.SOUTHWEST};
       	Direction[] southwest = {Direction.SOUTH, Direction.WEST};
       	Direction[] west = {Direction.NORTHWEST, Direction.SOUTHWEST};
       	Direction[] northwest = {Direction.NORTH, Direction.WEST};
       	
       	alternateDirs.put(Direction.NORTH, north);
        alternateDirs.put(Direction.NORTHEAST, northeast);
        alternateDirs.put(Direction.EAST, east);
       	alternateDirs.put(Direction.SOUTHEAST, southeast);
       	alternateDirs.put(Direction.SOUTH, south);
       	alternateDirs.put(Direction.SOUTHWEST, southwest);
       	alternateDirs.put(Direction.WEST, west);
       	alternateDirs.put(Direction.NORTHWEST, northwest);
       	
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
    	enemyHQCandidates();
    	for(MapLocation loc: enemyHQLocs) {
    	System.out.println("ememy HQ location" + loc);
    	}
    	if(minerCount < MINER_LIMIT) {
	        for (Direction dir : directions)
	            if(tryBuild(RobotType.MINER, dir)) {
	            	minerCount += 1;
	            }
    	}
    	if (rc.getRoundNum() == 1) {
	    	int[] hqCoords = new int[7];
	    	hqCoords[0] = teamSecret;
	    	hqCoords[1] = 1;
	    	hqCoords[6] = rc.getLocation().x;
	    	hqCoords[4] = rc.getLocation().y;
        	if (rc.canSubmitTransaction(hqCoords, 1)) {
        		rc.submitTransaction(hqCoords, 1);
        	}
        	
        	int[] fcCoords = new int[7];
        	fcCoords[0] = teamSecret;
        	fcCoords[1] = 2;
        	fcCoords[6] = rc.getLocation().add(Direction.SOUTHEAST).x;
        	fcCoords[4] = rc.getLocation().add(Direction.SOUTHEAST).y;
        	if (rc.canSubmitTransaction(fcCoords, 1)) {
        		rc.submitTransaction(fcCoords, 1);
        	}
        	
        	int[] vapCoords = new int[7];
        	vapCoords[0] = vaporatorSecret;
        	vapCoords[1] = rc.getLocation().add(Direction.NORTHWEST).x;
        	vapCoords[2] = rc.getLocation().add(Direction.NORTHWEST).y;
        	vapCoords[3] = rc.getLocation().add(Direction.NORTHEAST).x;
        	vapCoords[4] = rc.getLocation().add(Direction.NORTHEAST).y;
        	vapCoords[5] = rc.getLocation().add(Direction.SOUTHWEST).x;
        	vapCoords[6] = rc.getLocation().add(Direction.SOUTHWEST).y;
        	if (rc.canSubmitTransaction(vapCoords, 1)) {
        		rc.submitTransaction(vapCoords, 1);
        	}
        	
        	int[] ngCoords = new int[7];
        	ngCoords[0] = netgunSecret;
        	ngCoords[1] = rc.getLocation().add(Direction.NORTH).x;
        	ngCoords[2] = rc.getLocation().add(Direction.NORTH).y;
        	ngCoords[3] = rc.getLocation().add(Direction.WEST).x;
        	ngCoords[4] = rc.getLocation().add(Direction.WEST).y;
        	ngCoords[5] = rc.getLocation().add(Direction.SOUTH).x;
        	ngCoords[6] = rc.getLocation().add(Direction.SOUTH).y;
        	if (rc.canSubmitTransaction(ngCoords, 1)) {
        		rc.submitTransaction(ngCoords, 1);
        	}
    	}
    }
    
    static void enemyHQCandidates(){
    	MapLocation curLoc = rc.getLocation();
    	int x = curLoc.x;
    	int y = curLoc.y;
    	int mapHeight = rc.getMapHeight()-1;
    	int mapWidth = rc.getMapWidth()-1;
    	
    	int top = mapHeight - y;
    	int bottom = y;
    	int left = x;
    	int right = mapWidth - x;
		MapLocation loc1;
		MapLocation loc2;
		MapLocation loc3;

    	if(top > bottom && left < right) {
    		// bottom left corner
    	//	System.out.println("bottom left corner");
    		// right above
    		loc1 = new MapLocation(x, mapHeight-bottom);
    		// diagonally
    		loc2 = new MapLocation(mapWidth-left, mapHeight-bottom);
    		// to the right
    		loc3 = new MapLocation(mapWidth-left, y);

    	}
    	else if(top < bottom && left < right) {
    		// top left corner
    	//	System.out.println("top left corner");
    		// to the right
    		loc1 = new MapLocation(mapWidth-left, y);
    		// diagonally
    		loc2 = new MapLocation(mapWidth-left, top);
    		// right below
    		loc3 = new MapLocation(x, top);


    	}
    	else if(top < bottom && left > right) {
    		// top right corner
    		//System.out.println("top right corner");
    		// to the left
    		loc1 = new MapLocation(right, y);
    		// diagonally
    		loc2 = new MapLocation(right, top);
    		// right below
    		loc3 = new MapLocation(x, top);
    	}
    	else {
    		// bottom right corner
    		//System.out.println("bottom right corner");
    		// to the left
    		loc1 = new MapLocation(right, y);
    		// diagonally
    		loc2 = new MapLocation(right, mapHeight-bottom);
    		// right above
    		loc3 = new MapLocation(x, mapHeight-bottom);
    	}
    	
    	enemyHQLocs = new MapLocation[] {loc1, loc2, loc3};
    }

    static void runMiner() throws GameActionException {
    	if (hqLoc == null) {
    		// search surroundings for HQ
    		RobotInfo[] robots = rc.senseNearbyRobots();
    		for(RobotInfo robot : robots) {
    			if(robot.type == RobotType.HQ && robot.team == rc.getTeam()) {
    				hqLoc = robot.location;
    				hqElevation = rc.senseElevation(hqLoc);
    			}
    		}
    	}
    	// check if miner will be building miner
    	if (rc.getRoundNum() == 2) {
    		buildingMiner = true;
    	}
    	// code for building miner
    	System.out.println(Clock.getBytecodesLeft());
    	if (buildingMiner) {
    		// get locations of buildings from blockchain
    		if (turnCount == 1) {
        		for (Transaction tx : rc.getBlock(1)) {
        			int[] mess = tx.getMessage();
        			if (mess[0] == teamSecret && mess[1] == 2) {
        				fcLoc = new MapLocation(mess[6], mess[4]);
        			} else if (mess[0] == vaporatorSecret) {
        				vap1Loc = new MapLocation(mess[1], mess[2]);
        				vap2Loc = new MapLocation(mess[3], mess[4]);
        				vap3Loc = new MapLocation(mess[5], mess[6]);
        			} else if (mess[0] == netgunSecret) {
        				ng1Loc = new MapLocation(mess[1], mess[2]);
        				ng2Loc = new MapLocation(mess[3], mess[4]);
        				ng3Loc = new MapLocation(mess[5], mess[6]);
        			}
        		}
        	}
    		// check if drone exists
    		for (Transaction tx : rc.getBlock(rc.getRoundNum() - 1)) {
    			int[] mess = tx.getMessage();
    			if (mess[0] == teamSecret && mess[1] == 5) {
    				canBuild = true;
    			}
    		}
        	dsLoc = hqLoc.add(Direction.WEST).add(Direction.WEST).add(Direction.WEST);
    		// check if fulfillment center can be built and build it
        	if (!fcBuilt) {
        		adjacent = rc.getLocation().isAdjacentTo(fcLoc);
	    		if (rc.getTeamSoup() >= RobotType.FULFILLMENT_CENTER.cost && !fcBuilt && adjacent) {
	    			if (rc.canBuildRobot(RobotType.FULFILLMENT_CENTER, rc.getLocation().directionTo(fcLoc))) {
	    				rc.buildRobot(RobotType.FULFILLMENT_CENTER, rc.getLocation().directionTo(fcLoc));
	    				fcBuilt = true;
	    			}
	    		} else if (!adjacent) {
	    			tryMove(rc.getLocation().directionTo(fcLoc));
	    		}
        	}
    		// check if design school can be built and build it
    		if (fcBuilt && !dsBuilt && canBuild) {
    			adjacent = rc.getLocation().isAdjacentTo(dsLoc);
		    	if (rc.getTeamSoup() >= RobotType.DESIGN_SCHOOL.cost && !dsBuilt && adjacent) {
		    		if (rc.canBuildRobot(RobotType.DESIGN_SCHOOL, rc.getLocation().directionTo(dsLoc))) {
		    			rc.buildRobot(RobotType.DESIGN_SCHOOL, rc.getLocation().directionTo(dsLoc));
		    			dsBuilt = true;
		   			}
		    	} else if (!adjacent) {
		    		tryMove(rc.getLocation().directionTo(dsLoc));
		    	}
    		}
    		// check if vaporators can be built
    		if (dsBuilt && !vap1Built) {
    			adjacent = rc.getLocation().isAdjacentTo(vap1Loc);
    			if (rc.getTeamSoup() >= RobotType.VAPORATOR.cost && !vap1Built && adjacent) {
    				if (rc.canBuildRobot(RobotType.VAPORATOR, rc.getLocation().directionTo(vap1Loc))) {
    					rc.buildRobot(RobotType.VAPORATOR, rc.getLocation().directionTo(vap1Loc));
    					vap1Built = true;
    				}
    			} else if (!adjacent) {
    				tryMove(rc.getLocation().directionTo(vap1Loc));
    			}
    		}
    		if (vap1Built && !vap2Built) {
    			adjacent = rc.getLocation().isAdjacentTo(vap2Loc);
    			if (rc.getTeamSoup() >= RobotType.VAPORATOR.cost && !vap2Built && adjacent) {
    				if (rc.canBuildRobot(RobotType.VAPORATOR, rc.getLocation().directionTo(vap2Loc))) {
    					rc.buildRobot(RobotType.VAPORATOR, rc.getLocation().directionTo(vap2Loc));
    					vap2Built = true;
    				}
    			} else if (!adjacent) {
    				tryMove(rc.getLocation().directionTo(vap2Loc));
    			}
    		}
    		if (vap2Built && !vap3Built) {
    			adjacent = rc.getLocation().isAdjacentTo(vap3Loc);
    			if (rc.getTeamSoup() >= RobotType.VAPORATOR.cost && !vap3Built && adjacent) {
    				if (rc.canBuildRobot(RobotType.VAPORATOR, rc.getLocation().directionTo(vap3Loc))) {
    					rc.buildRobot(RobotType.VAPORATOR, rc.getLocation().directionTo(vap3Loc));
    					vap3Built = true;
    				}
    			} else if (!adjacent) {
    				tryMove(rc.getLocation().directionTo(vap3Loc));
    			}
    		}
    		if (vap3Built && !ng1Built) {
    			adjacent = rc.getLocation().isAdjacentTo(ng1Loc);
    			if (rc.getTeamSoup() >= RobotType.NET_GUN.cost && !ng1Built && adjacent) {
    				if (rc.canBuildRobot(RobotType.NET_GUN, rc.getLocation().directionTo(ng1Loc))) {
    					rc.buildRobot(RobotType.NET_GUN, rc.getLocation().directionTo(ng1Loc));
    					ng1Built = true;
    					int[] message = new int[7];
    		        	message[0] = teamSecret;
    		        	message[1] = 6;
    		        	if (rc.canSubmitTransaction(message, 1)) {
    		        		rc.submitTransaction(message, 1);
    		        	}
    				}
    			} else if (!adjacent) {
    				tryMove(rc.getLocation().directionTo(ng1Loc));
    			}
    		}
    		if (ng1Built && !ng2Built) {
    			adjacent = rc.getLocation().isAdjacentTo(ng2Loc);
    			if (rc.getTeamSoup() >= RobotType.NET_GUN.cost && !ng2Built && adjacent) {
    				if (rc.canBuildRobot(RobotType.NET_GUN, rc.getLocation().directionTo(ng2Loc))) {
    					rc.buildRobot(RobotType.NET_GUN, rc.getLocation().directionTo(ng2Loc));
    					ng2Built = true;
    				}
    			} else if (!adjacent) {
    				tryMove(rc.getLocation().directionTo(ng2Loc));
    			}
    		}
    		if (ng2Built && !ng3Built) {
    			adjacent = rc.getLocation().isAdjacentTo(ng3Loc);
    			if (rc.getTeamSoup() >= RobotType.NET_GUN.cost && !ng3Built && adjacent) {
    				if (rc.canBuildRobot(RobotType.NET_GUN, rc.getLocation().directionTo(ng3Loc))) {
    					rc.buildRobot(RobotType.NET_GUN, rc.getLocation().directionTo(ng3Loc));
    					ng3Built = true;
    				}
    			} else if (!adjacent) {
    				tryMove(rc.getLocation().directionTo(ng1Loc));
    			}
    		}
    	}
    	// code for normal miners
    	else {
    		System.out.println(Clock.getBytecodesLeft());
    		loc = rc.getLocation();
	    	// tries to refine soup all around it
	        for (Direction dir : directions) {
	            if (tryRefine(dir)) {
//              System.out.println("I refined soup! " + rc.getTeamSoup());
	            }
	        }
	        // tries to mine all around it
	        for (Direction dir : directions) {
	            if (tryMine(dir)) {
	            	lastSoupMined = loc.add(dir);
//              System.out.println("I mined soup! " + rc.getSoupCarrying());
	            }
	        }
	        // go drop off soup at hq
	        if (rc.getSoupCarrying() == rc.getType().soupLimit) {
//        	System.out.println("at the soup limit: " + rc.getSoupCarrying());
	        	// time to go back to HQ
	        	Direction dirToHQ = loc.directionTo(hqLoc);
	        	if(tryMove(dirToHQ)) {
//        		System.out.println("moved towards HQ");
//	        	System.out.println("moved to: " + rc.getLocation());
	        	}
	        }
	    	// move towards last soup mined
	    	if (lastSoupMined != null && rc.getSoupCarrying() == 0) {
	    		Direction dirToSoup = loc.directionTo(lastSoupMined);
	    		if (dirToSoup == Direction.CENTER) {
	    			System.out.println("ran");
	        		lastSoupMined = null;
	        	} else {
		        	if (tryMove(dirToSoup)) {
	//	        	System.out.println("moved towards last soup");
		        	}
	        	}
	        }
	    	// sense for soup
	    	for (Direction dir : directions) {
	    		if (dir == Direction.NORTH) {
	    			d = loc.add(dir).add(dir);
	    			if (rc.canSenseLocation(d)) {
	    				if (rc.senseSoup(d) > 0) {
	    					tryMove(dir);
	    				}
	    			}
	    		} else if (dir == Direction.NORTHEAST) {
	    			d = loc.add(dir).add(dir);
	    			if (rc.canSenseLocation(d)) {
	    				if (rc.senseSoup(d) > 0) {
	    					tryMove(dir);
	    				}
	    			} 
	    			d = loc.add(dir).add(Direction.NORTH);
	    			if (rc.canSenseLocation(d)) {
	    				if (rc.senseSoup(d) > 0) {
	    					tryMove(dir);
	    				}
	    			} 
	    			d = loc.add(dir).add(Direction.EAST);
	    			if (rc.canSenseLocation(d)) {
	    				if (rc.senseSoup(d) > 0) {
	    					tryMove(dir);
	    				}
	    			}
	    		} else if (dir == Direction.EAST) {
	    			d = loc.add(dir).add(dir);
	    			if (rc.canSenseLocation(d)) {
	    				if (rc.senseSoup(d) > 0) {
	    					tryMove(dir);
	    				}
	    			}
	    		} else if (dir == Direction.SOUTHEAST) {
	    			d = loc.add(dir).add(dir);
	    			if (rc.canSenseLocation(d)) {
	    				if (rc.senseSoup(d) > 0) {
	    					tryMove(dir);
	    				}
	    			} 
	    			d = loc.add(dir).add(Direction.SOUTH);
	    			if (rc.canSenseLocation(d)) {
	    				if (rc.senseSoup(d) > 0) {
	    					tryMove(dir);
	    				}
	    			}
	    			d = loc.add(dir).add(Direction.EAST);
	    			if (rc.canSenseLocation(d)) {
	    				if (rc.senseSoup(d) > 0) {
	    					tryMove(dir);
	    				}
	    			}
	    		} else if (dir == Direction.SOUTH) {
	    			d = loc.add(dir).add(dir);
	    			if (rc.canSenseLocation(d)) {
	    				if (rc.senseSoup(d) > 0) {
	    					tryMove(dir);
	    				}
	    			}
	    		} else if (dir == Direction.SOUTHWEST) {
	    			d = loc.add(dir).add(dir);
	    			if (rc.canSenseLocation(d)) {
	    				if (rc.senseSoup(d) > 0) {
	    					tryMove(dir);
	    				}
	    			}
	    			d = loc.add(dir).add(Direction.SOUTH);
	    			if (rc.canSenseLocation(d)) {
	    				if (rc.senseSoup(d) > 0) {
	    					tryMove(dir);
	    				}
	    			}
	    			d = loc.add(dir).add(Direction.WEST);
	    			if (rc.canSenseLocation(d)) {
	    				if (rc.senseSoup(d) > 0) {
	    					tryMove(dir);
	    				}
	    			}
	    		} else if (dir == Direction.WEST) {
	    			d = loc.add(dir).add(dir);
	    			if (rc.canSenseLocation(d)) {
	    				if (rc.senseSoup(d) > 0) {
	    					tryMove(dir);
	    				}
	    			}
	    		} else if (dir == Direction.NORTHWEST) {
	    			d = loc.add(dir).add(dir);
	    			if (rc.canSenseLocation(d)) {
	    				if (rc.senseSoup(d) > 0) {
	    					tryMove(dir);
	    				}
	    			}
	    			d = loc.add(dir).add(Direction.NORTH);
	    			if (rc.canSenseLocation(d)) {
	    				if (rc.senseSoup(d) > 0) {
	    					tryMove(dir);
	    				}
	    			}
	    			d = loc.add(dir).add(Direction.WEST);
	    			if (rc.canSenseLocation(d)) {
	    				if (rc.senseSoup(d) > 0) {
	    					tryMove(dir);
	    				}
	    			}
	    		}
	    	}
	    	if (Math.abs(GameConstants.getWaterLevel(rc.getRoundNum()) - rc.senseElevation(loc)) <= 0.5) {
	    		for (Direction dir : directions) {
	    			if (rc.senseElevation(loc.add(dir)) > rc.senseElevation(loc)) {
	    				tryMove(dir);
	    			}
	    		}
	    	}
	    	// move in a random direction
	        tryMove(randomDirection(last));
    	}
    }

    static void runRefinery() throws GameActionException {
        // System.out.println("Pollution: " + rc.sensePollution(rc.getLocation()));
    }

    static void runVaporator() throws GameActionException {

    }

    static void runDesignSchool() throws GameActionException {
    	for (Transaction tx : rc.getBlock(rc.getRoundNum() - 1)) {
			int[] mess = tx.getMessage();
			if (mess[0] == teamSecret && mess[1] == 6) {
				canBuild = true;
			}
		}
    	
//    	if(landscaperCount < LANDSCAPER_LIMIT) {
//    		for(Direction direction: directions) {
//    			if(tryBuild(RobotType.LANDSCAPER, direction)) {
//    				landscaperCount += 1;
//    			}
//    		}
//    	}
    }

    static void runFulfillmentCenter() throws GameActionException {
        for (Direction dir : directions) {
        	if (droneCount == 0 && rc.getTeamSoup() > RobotType.DELIVERY_DRONE.cost) {
        		if (tryBuild(RobotType.DELIVERY_DRONE, dir)) {
        			int[] message = new int[7];
        	    	message[0] = teamSecret;
        	    	message[1] = 5;
                	if (rc.canSubmitTransaction(message, 1)) {
                		rc.submitTransaction(message, 1);
                	}
                	droneCount += 1;
        		}
        	}
        }
        
    }

    static void runLandscaper() throws GameActionException {
//    	System.out.println(turnCount);
    	if (turnCount == 1) {
    		for (Transaction tx : rc.getBlock(1)) {
    			int[] mess = tx.getMessage();
    			if (mess[0] == teamSecret && mess[1] == 1) {
    				hqLoc = new MapLocation(mess[6], mess[4]);
    			}
    		}
    	}
    	
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
    	
    }

    static void runDeliveryDrone() throws GameActionException {
        // get the HQ location on its first turn
    	if (turnCount == 1) {
    		for (Transaction tx : rc.getBlock(1)) {
    			int[] mess = tx.getMessage();
    			if (mess[0] == teamSecret && mess[1] == 1) {
    				hqLoc = new MapLocation(mess[6], mess[4]);
    			}
    		}
    	}
    	
    	// reads blockchain for protector drone
    	for (Transaction tx : rc.getBlock(rc.getRoundNum() - 1)) {
			int[] mess = tx.getMessage();
			if (mess[0] == teamSecret && mess[1] == 1) {
				protectorDrone = true;
			}
		}
    	
    	Team enemy = rc.getTeam().opponent();
    	// controls for protector drone
    	if (protectorDrone) {
    		MapLocation droneLoc = hqLoc.add(Direction.EAST);
    		if(!rc.getLocation().equals(droneLoc) && !rc.isCurrentlyHoldingUnit()){
    			Direction dirToDroneLoc = rc.getLocation().directionTo(droneLoc);
	        	if (tryMove(dirToDroneLoc)) {
//	        	System.out.println("moved towards last soup");
	        	}
    			
    		}
    		if (!rc.isCurrentlyHoldingUnit()) {
                // See if there are any enemy robots within striking range (distance 1 from lumberjack's radius)
                RobotInfo[] pickupRobots = rc.senseNearbyRobots(GameConstants.DELIVERY_DRONE_PICKUP_RADIUS_SQUARED, enemy);

                if (pickupRobots.length > 0) {
                    // Pick up a first robot within range
                    rc.pickUpUnit(pickupRobots[0].getID());
//                    System.out.println("I picked up " + robots[0].getID() + "!");
                }
                
                RobotInfo[] nearbyRobots = rc.senseNearbyRobots(-1, enemy);
                RobotInfo approachToRobot = null;
                int closestBot = Integer.MAX_VALUE;
   
                for(RobotInfo r: nearbyRobots) {
                	int distToBot = rc.getLocation().distanceSquaredTo(r.location);
                	if(distToBot < closestBot) {
                		approachToRobot = r;
                	}
                }
                
                if(approachToRobot != null) {
                	Direction dirToEnemy = rc.getLocation().directionTo(approachToRobot.location);
                	System.out.println(dirToEnemy);
                	tryMove(dirToEnemy);
                }
            } else {
                // No close robots, so search for robots within sight radius
    	    	// sense for soup
            	System.out.println("carrying a unit");
            	loc = rc.getLocation();
    	    	for (Direction dir : directions) {
    	    		if (dir == Direction.NORTH) {
    	    			d = loc.add(dir).add(dir);
    	    			if (rc.canSenseLocation(d)) {
    	    				if (rc.senseFlooding(d)) {
    	    					tryMove(dir);
    	    				}
    	    			}
    	    		} else if (dir == Direction.NORTHEAST) {
    	    			d = loc.add(dir).add(dir);
    	    			if (rc.canSenseLocation(d)) {
    	    				if (rc.senseFlooding(d)) {
    	    					tryMove(dir);
    	    				}
    	    			} 
    	    			d = loc.add(dir).add(Direction.NORTH);
    	    			if (rc.canSenseLocation(d)) {
    	    				if (rc.senseFlooding(d)) {
    	    					tryMove(dir);
    	    				}
    	    			} 
    	    			d = loc.add(dir).add(Direction.EAST);
    	    			if (rc.canSenseLocation(d)) {
    	    				if (rc.senseFlooding(d)) {
    	    					tryMove(dir);
    	    				}
    	    			}
    	    		} else if (dir == Direction.EAST) {
    	    			d = loc.add(dir).add(dir);
    	    			if (rc.canSenseLocation(d)) {
    	    				if (rc.senseFlooding(d)) {
    	    					tryMove(dir);
    	    				}
    	    			}
    	    		} else if (dir == Direction.SOUTHEAST) {
    	    			d = loc.add(dir).add(dir);
    	    			if (rc.canSenseLocation(d)) {
    	    				if (rc.senseFlooding(d)) {
    	    					tryMove(dir);
    	    				}
    	    			} 
    	    			d = loc.add(dir).add(Direction.SOUTH);
    	    			if (rc.canSenseLocation(d)) {
    	    				if (rc.senseFlooding(d)) {
    	    					tryMove(dir);
    	    				}
    	    			}
    	    			d = loc.add(dir).add(Direction.EAST);
    	    			if (rc.canSenseLocation(d)) {
    	    				if (rc.senseFlooding(d)) {
    	    					tryMove(dir);
    	    				}
    	    			}
    	    		} else if (dir == Direction.SOUTH) {
    	    			d = loc.add(dir).add(dir);
    	    			if (rc.canSenseLocation(d)) {
    	    				if (rc.senseFlooding(d)) {
    	    					tryMove(dir);
    	    				}
    	    			}
    	    		} else if (dir == Direction.SOUTHWEST) {
    	    			d = loc.add(dir).add(dir);
    	    			if (rc.canSenseLocation(d)) {
    	    				if (rc.senseFlooding(d)) {
    	    					tryMove(dir);
    	    				}
    	    			}
    	    			d = loc.add(dir).add(Direction.SOUTH);
    	    			if (rc.canSenseLocation(d)) {
    	    				if (rc.senseFlooding(d)) {
    	    					tryMove(dir);
    	    				}
    	    			}
    	    			d = loc.add(dir).add(Direction.WEST);
    	    			if (rc.canSenseLocation(d)) {
    	    				if (rc.senseFlooding(d)) {
    	    					tryMove(dir);
    	    				}
    	    			}
    	    		} else if (dir == Direction.WEST) {
    	    			d = loc.add(dir).add(dir);
    	    			if (rc.canSenseLocation(d)) {
    	    				if (rc.senseFlooding(d)) {
    	    					tryMove(dir);
    	    				}
    	    			}
    	    		} else if (dir == Direction.NORTHWEST) {
    	    			d = loc.add(dir).add(dir);
    	    			if (rc.canSenseLocation(d)) {
    	    				if (rc.senseFlooding(d)) {
    	    					tryMove(dir);
    	    				}
    	    			}
    	    			d = loc.add(dir).add(Direction.NORTH);
    	    			if (rc.canSenseLocation(d)) {
    	    				if (rc.senseFlooding(d)) {
    	    					tryMove(dir);
    	    				}
    	    			}
    	    			d = loc.add(dir).add(Direction.WEST);
    	    			if (rc.canSenseLocation(d)) {
    	    				if (rc.senseFlooding(d)) {
    	    					tryMove(dir);
    	    				}
    	    			}
    	    		}
    	    	}
                tryMove(randomDirection(last));
            }
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
        if (rc.isReady() && rc.canMove(dir) && (!rc.senseFlooding(rc.getLocation().add(dir)) || rc.getType() == RobotType.DELIVERY_DRONE)) {
            rc.move(dir);
            last = dir;
            return true;
        } else {
        	Direction[] altDirs = alternateDirs.get(dir);
        	for(Direction d: altDirs) {
                if (rc.isReady() && rc.canMove(d) && (!rc.senseFlooding(rc.getLocation().add(d)) || rc.getType() == RobotType.DELIVERY_DRONE)) {
                    rc.move(d);
                    last = d;
                    return true;
                }
        	}
        }
        return false;
        
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
