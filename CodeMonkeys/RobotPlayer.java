package CodeMonkeys;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

	static int HQ_LOC_MESSAGE = 1;
	static int FC_LOC_MESSAGE = 2; 
	static int LS_BUILD_WALL = 3;
	static int PROTECTOR_DRONE_MESSAGE = 5;


	static int ELEVATION_LIMIT = 25;
	static int MINER_LIMIT = 4;
	static int VAPORATOR_LIMIT = 2;
	static int DESIGN_LIMIT = 2;  
	static int LANDSCAPER_LIMIT = 6;
	static int TURN_LIMIT = 1000;
	static int MINER_SPAWN_RATE = 1;
	static int DIRT_LIMIT = 9;
	static int DRONE_HOLD_LIMIT = 50;
	static int DRONE_LIMIT = 6;
	static int REFINERY_DIST_LIMIT = 100;

	static int turnCount;
	static int minerCount;
	static int vaporatorCount;
	static int designCount;
	static int landscaperCount = 0;
	static int droneCount = 0;

	static int roundsDisplayed = 0;
	static int ROUND_DISPLAY_LIMIT = 1;
	static boolean firstWave = true;
	static MapLocation dropOffLoc;


	static HashSet<MapLocation> forbiddenLocs = new HashSet<>();
	static MapLocation[] enemyHQLocs;
	static MapLocation enemyHqLoc;
	static MapLocation hqLoc;
	static int hqElevation;
	static MapLocation lastSoupMined;
	static MapLocation depositLoc;
	static int teamSecret = 72151689;
	static int vaporatorSecret = 721516891;
	static int netgunSecret = 721516892;
	static int landscapersSecret1 = 721516893;
	static int landscapersSecret2 = 721516894;
	static int offenseSecret = 72151688;
	static HashMap<Direction, Direction> oppositeDirection = new HashMap<>();
	static HashMap<Direction, Direction[]> alternateDirs = new HashMap<>();
	static Direction last;
	static Direction lastDesiredDir;
	static MapLocation d; //for sensing soup
	static Direction lastLast;
	static boolean stop = false;
	static boolean hasStopped = false;
	static boolean replaceDirt = true;
	static boolean done = false;
	static boolean offensive = false;
	static boolean defensive = false;
	static int index = 0;
	static boolean droneAttack = false;
	static boolean waiting = false;
	static boolean abandonHQ = false;
	static boolean build = false;
	static int counter = 0;


	static int recencyThreshold = 8;
	static MapLocation[] visited = new MapLocation[recencyThreshold];

	static MapLocation loc;
	static MapLocation fcLoc;
	static MapLocation vap1Loc;
	static MapLocation vap2Loc;
	static MapLocation vap3Loc;
	static MapLocation dsLoc;
	static MapLocation ng1Loc;
	static MapLocation ng2Loc;
	static MapLocation ng3Loc;
	static MapLocation[] lsLoc = new MapLocation[7];
	static MapLocation standLoc;
	static MapLocation waterLoc;

	static boolean buildingMiner = false;
	static boolean protectorDrone = false;
	static int lsID;
	static int enemyHQIndex = 0;

	static boolean adjacent = false;
	static boolean atLoc = false;

	static boolean fcBuilt = false;
	static boolean vap1Built = false;
	static boolean vap2Built = false;
	static boolean vap3Built = false;
	static boolean dsBuilt = false;
	static boolean ng1Built = false;
	static boolean ng2Built = false;
	static boolean ng3Built = false;
	static boolean canBuild = false;
	static Direction[] placeDirt = new Direction[3];
	static Direction behind = null;

	static int droneHoldingFor;

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

		Direction[] north = {Direction.NORTHWEST, Direction.NORTHEAST, Direction.WEST, 
				Direction.EAST, Direction.SOUTHWEST, Direction.SOUTHEAST, Direction.SOUTH};
		Direction[] northeast = {Direction.NORTH, Direction.EAST, Direction.NORTHWEST,
				Direction.SOUTHEAST, Direction.SOUTHWEST, Direction.SOUTH};
		Direction[] east = {Direction.NORTHEAST, Direction.SOUTHEAST, Direction.NORTH, 
				Direction.SOUTH, Direction.SOUTHWEST, Direction.SOUTHEAST};
		Direction[] southeast = {Direction.SOUTH, Direction.EAST, Direction.SOUTHWEST, 
				Direction.NORTHEAST, Direction.NORTH, Direction.WEST, Direction.NORTHWEST};
		Direction[] south = {Direction.SOUTHEAST, Direction.SOUTHWEST, Direction.WEST,
				Direction.EAST, Direction.NORTHWEST, Direction.NORTHEAST, Direction.NORTH};
		Direction[] southwest = {Direction.SOUTH, Direction.WEST, Direction.NORTHWEST,
				Direction.SOUTHEAST, Direction.NORTH, Direction.EAST, Direction.NORTHEAST};
		Direction[] west = {Direction.NORTHWEST, Direction.SOUTHWEST, Direction.NORTH,
				Direction.SOUTH, Direction.NORTHEAST, Direction.SOUTHEAST, Direction.EAST};
		Direction[] northwest = {Direction.NORTH, Direction.WEST, Direction.SOUTHWEST, Direction.NORTHEAST, Direction.SOUTH, Direction.EAST, Direction.SOUTHEAST};

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

	static void genForbidden() {
		loc = hqLoc.add(Direction.NORTHWEST).add(Direction.NORTHWEST);
		Direction[] dirOrder = {Direction.EAST, Direction.SOUTH, Direction.WEST, Direction.NORTH};
		for(int i = 0; i < 4; i++) {
			for(int x = 0; x < 4; x++) {
				System.out.println("adding loc: " + loc);
				forbiddenLocs.add(loc);
				loc = loc.add(dirOrder[i]);
			}
		}

		for(MapLocation loc: forbiddenLocs) {
			System.out.println("forbidden Loc: " + loc);
		}
	}

	static void runHQ() throws GameActionException {
		hqElevation = rc.senseElevation(rc.getLocation());
    	for (RobotInfo rob : rc.senseNearbyRobots()) {
    		if (rob.type == RobotType.DELIVERY_DRONE && rob.getTeam() != rc.getTeam()) {
    			if (rc.canShootUnit(rob.ID)) {
    				rc.shootUnit(rob.ID);
    			}
    		}
    	}
    	int lsCount = 0;
    	for (RobotInfo rob : rc.senseNearbyRobots(2)) {
    		if (rob.type == RobotType.LANDSCAPER) {
    			lsCount += 1;
    		}
    		if (lsCount == 8) {
    			int[] mess = new int[7];
    			mess[0] = teamSecret;
    			mess[1] = 23;
    			if (rc.canSubmitTransaction(mess, 1)) {
    				rc.submitTransaction(mess, 1);
    			}
    		}
    	}
		enemyHQCandidates(rc.getLocation());
		for(MapLocation loc: enemyHQLocs) {
			//    	System.out.println("ememy HQ location" + loc);
		}
		if(minerCount < MINER_LIMIT) {
			for (Direction dir : directions) {
	            if(tryBuild(RobotType.MINER, dir)) {
	            	minerCount += 1;
	            	if (minerCount == 2 && offensive) {
	            		int[] mess = new int[7];
		    	    	mess[0] = teamSecret;
		    	    	mess[1] = 20;
		            	if (rc.canSubmitTransaction(mess, 1)) {
		            		rc.submitTransaction(mess, 1);
		            	}
	            	}
	            }
	        }
		}
		if (rc.getRoundNum() >= 240 && !canBuild) {
    		for (Direction dir : directions) {
    			if (rc.canBuildRobot(RobotType.MINER, dir)) {
    				rc.buildRobot(RobotType.MINER, dir);
    				canBuild = true;
    				int[] mess = new int[7];
    				mess[0] = teamSecret;
    				mess[1] = 27;
    				if (rc.canSubmitTransaction(mess, 1)) {
	            		rc.submitTransaction(mess, 1);
	            	}
    			}
    		}
    	}
		if (rc.getRoundNum() == 1) {
			if (rc.onTheMap(rc.getLocation().add(Direction.NORTH).add(Direction.NORTH)) && rc.onTheMap(rc.getLocation().add(Direction.NORTHWEST).add(Direction.NORTHWEST)) && rc.onTheMap(rc.getLocation().add(Direction.NORTHEAST).add(Direction.NORTHEAST)) && rc.onTheMap(rc.getLocation().add(Direction.SOUTH).add(Direction.SOUTH)) && rc.onTheMap(rc.getLocation().add(Direction.SOUTHWEST).add(Direction.WEST)) && rc.onTheMap(rc.getLocation().add(Direction.SOUTHEAST).add(Direction.EAST)) && rc.onTheMap(rc.getLocation().add(Direction.WEST).add(Direction.WEST).add(Direction.WEST))) {
				if (Math.abs(rc.senseElevation(rc.getLocation().add(Direction.NORTH).add(Direction.NORTH)) - rc.senseElevation(rc.getLocation())) < 4 && Math.abs(rc.senseElevation(rc.getLocation().add(Direction.NORTHWEST).add(Direction.NORTHWEST)) - rc.senseElevation(rc.getLocation())) < 4 && Math.abs(rc.senseElevation(rc.getLocation().add(Direction.NORTHEAST).add(Direction.NORTHEAST)) - rc.senseElevation(rc.getLocation())) < 4 && Math.abs(rc.senseElevation(rc.getLocation().add(Direction.SOUTH).add(Direction.SOUTH)) - rc.senseElevation(rc.getLocation())) < 4 && Math.abs(rc.senseElevation(rc.getLocation().add(Direction.SOUTHWEST).add(Direction.WEST)) - rc.senseElevation(rc.getLocation())) < 4 && Math.abs(rc.senseElevation(rc.getLocation().add(Direction.SOUTHEAST).add(Direction.EAST)) - rc.senseElevation(rc.getLocation())) < 4 && Math.abs(rc.senseElevation(rc.getLocation().add(Direction.WEST).add(Direction.WEST).add(Direction.WEST))) - rc.senseElevation(rc.getLocation()) < 4) {
					if (!rc.senseFlooding(rc.getLocation().add(Direction.NORTH).add(Direction.NORTH)) && !rc.senseFlooding(rc.getLocation().add(Direction.NORTHWEST).add(Direction.NORTHWEST)) && !rc.senseFlooding(rc.getLocation().add(Direction.NORTHEAST).add(Direction.NORTHEAST)) && !rc.senseFlooding(rc.getLocation().add(Direction.SOUTH).add(Direction.SOUTH)) && !rc.senseFlooding(rc.getLocation().add(Direction.SOUTHWEST).add(Direction.WEST)) && !rc.senseFlooding(rc.getLocation().add(Direction.SOUTHEAST).add(Direction.EAST)) && !rc.senseFlooding(rc.getLocation().add(Direction.WEST).add(Direction.WEST).add(Direction.WEST))) {
						if 	(rc.senseElevation(rc.getLocation().add(Direction.NORTH).add(Direction.NORTH)) > 2 && rc.senseElevation(rc.getLocation().add(Direction.NORTHWEST).add(Direction.NORTHWEST)) > 2 && rc.senseElevation(rc.getLocation().add(Direction.NORTHEAST).add(Direction.NORTHEAST)) > 2 && rc.senseElevation(rc.getLocation().add(Direction.SOUTH).add(Direction.SOUTH)) > 2 && rc.senseElevation(rc.getLocation().add(Direction.SOUTHWEST).add(Direction.WEST)) > 2 && rc.senseElevation(rc.getLocation().add(Direction.SOUTHEAST).add(Direction.EAST)) > 2 && rc.senseElevation(rc.getLocation().add(Direction.WEST).add(Direction.WEST).add(Direction.WEST)) > 2) {
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

							int[] ngCoords = new int[7];
							ngCoords[0] = netgunSecret;
							ngCoords[1] = rc.getLocation().add(Direction.NORTHEAST).x;
							ngCoords[2] = rc.getLocation().add(Direction.NORTHEAST).y;
							ngCoords[3] = rc.getLocation().add(Direction.NORTHWEST).x;
							ngCoords[4] = rc.getLocation().add(Direction.NORTHWEST).y;
							ngCoords[5] = rc.getLocation().add(Direction.SOUTHWEST).x;
							ngCoords[6] = rc.getLocation().add(Direction.SOUTHWEST).y;
							if (rc.canSubmitTransaction(ngCoords, 1)) {
								rc.submitTransaction(ngCoords, 1);
							}

							int[] lsCoords1 = new int[7];
							lsCoords1[0] = landscapersSecret1;
							lsCoords1[3] = rc.getLocation().add(Direction.NORTH).add(Direction.NORTH).x;
							lsCoords1[4] = rc.getLocation().add(Direction.NORTH).add(Direction.NORTH).y;
							lsCoords1[5] = rc.getLocation().add(Direction.NORTHWEST).add(Direction.NORTHWEST).x;
							lsCoords1[6] = rc.getLocation().add(Direction.NORTHWEST).add(Direction.NORTHWEST).y;
							lsCoords1[1] = rc.getLocation().add(Direction.NORTHEAST).add(Direction.NORTHEAST).x;
							lsCoords1[2] = rc.getLocation().add(Direction.NORTHEAST).add(Direction.NORTHEAST).y;
							if (rc.canSubmitTransaction(lsCoords1, 1)) {
								rc.submitTransaction(lsCoords1, 1);
							}

							int[] lsCoords2 = new int[7];
							lsCoords2[0] = landscapersSecret2;
							lsCoords2[3] = rc.getLocation().add(Direction.SOUTH).add(Direction.SOUTH).x;
							lsCoords2[4] = rc.getLocation().add(Direction.SOUTH).add(Direction.SOUTH).y;
							lsCoords2[5] = rc.getLocation().add(Direction.SOUTHWEST).add(Direction.WEST).x;
							lsCoords2[6] = rc.getLocation().add(Direction.SOUTHWEST).add(Direction.WEST).y;
							lsCoords2[1] = rc.getLocation().add(Direction.SOUTHEAST).add(Direction.EAST).x;
							lsCoords2[2] = rc.getLocation().add(Direction.SOUTHEAST).add(Direction.EAST).y;
							if (rc.canSubmitTransaction(lsCoords2, 1)) {
								rc.submitTransaction(lsCoords2, 1);
							}
						} else {
							int[] offense = new int[7];
							offense[0] = teamSecret;
							offense[1] = offenseSecret;
							if (rc.canSubmitTransaction(offense, 1)) {
								rc.submitTransaction(offense, 1);
							}

							int[] hqCoords = new int[7];
							hqCoords[0] = teamSecret;
							hqCoords[1] = 1;
							hqCoords[6] = rc.getLocation().x;
							hqCoords[4] = rc.getLocation().y;
							if (rc.canSubmitTransaction(hqCoords, 1)) {
								rc.submitTransaction(hqCoords, 1);
							}
						}
					} else {
						int[] offense = new int[7];
						offense[0] = teamSecret;
						offense[1] = offenseSecret;
						if (rc.canSubmitTransaction(offense, 1)) {
							rc.submitTransaction(offense, 1);
						}

						int[] hqCoords = new int[7];
						hqCoords[0] = teamSecret;
						hqCoords[1] = 1;
						hqCoords[6] = rc.getLocation().x;
						hqCoords[4] = rc.getLocation().y;
						if (rc.canSubmitTransaction(hqCoords, 1)) {
							rc.submitTransaction(hqCoords, 1);
						}
					}
				} else {
					int[] offense = new int[7];
					offense[0] = teamSecret;
					offense[1] = offenseSecret;
					if (rc.canSubmitTransaction(offense, 1)) {
						rc.submitTransaction(offense, 1);
					}

					int[] hqCoords = new int[7];
					hqCoords[0] = teamSecret;
					hqCoords[1] = 1;
					hqCoords[6] = rc.getLocation().x;
					hqCoords[4] = rc.getLocation().y;
					if (rc.canSubmitTransaction(hqCoords, 1)) {
						rc.submitTransaction(hqCoords, 1);
					}
				}
			} else {
				int[] offense = new int[7];
				offense[0] = teamSecret;
				offense[1] = offenseSecret;
				if (rc.canSubmitTransaction(offense, 1)) {
					rc.submitTransaction(offense, 1);
				}

				int[] hqCoords = new int[7];
				hqCoords[0] = teamSecret;
				hqCoords[1] = 1;
				hqCoords[6] = rc.getLocation().x;
				hqCoords[4] = rc.getLocation().y;
				if (rc.canSubmitTransaction(hqCoords, 1)) {
					rc.submitTransaction(hqCoords, 1);
				}
			}
		}
	}

	static void enemyHQCandidates(MapLocation hqLoc){
		MapLocation curLoc = hqLoc;
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
    	System.out.println(offensive);
    	System.out.println(buildingMiner);
    	System.out.println(build);
    	if (hqLoc == null && !abandonHQ) {
    		// search surroundings for HQ
    		RobotInfo[] robots = rc.senseNearbyRobots();
    		for(RobotInfo robot : robots) {
    			if(robot.type == RobotType.HQ && robot.team == rc.getTeam()) {
    				hqLoc = robot.location;
    				hqElevation = rc.senseElevation(hqLoc);
    			}
    		}
    		genForbidden();
    	}
    	loc = rc.getLocation();
    	// check if miner will be building miner
//    	if (rc.getRoundNum() == 2) {
//    		buildingMiner = true;
//    	}
    	// code for building miner
//    	System.out.println(Clock.getBytecodesLeft());
    	if (rc.getRoundNum() == 2) {
    		for (Transaction tx : rc.getBlock(1)) {
    			int[] mess = tx.getMessage();
    			if (mess[0] == teamSecret && mess[1] == offenseSecret) {
    				offensive = true;
    			} else if(mess[1] == 1){
    				buildingMiner = true;
    			}
    		}
    	} else if (turnCount == 1) {
    		for (Transaction tx : rc.getBlock(rc.getRoundNum() - 1)) {
    			int[] mess = tx.getMessage();
    			if (mess[0] == teamSecret && mess[1] == 27) {
    				build = true;
    			}
    		}
    	}
//    	if (rc.getRoundNum() == 3 && !offensive) { 
//    		buildingMiner = true;
//    	}
    	if (rc.getRoundNum() >= 250 && build) {
    		if (rc.canSenseLocation(hqLoc)) {
    			Direction dir = loc.directionTo(hqLoc);
				if (dir == Direction.SOUTH || dir == Direction.SOUTHEAST || dir == Direction.SOUTHWEST) {
					behind = Direction.NORTH;
				} else if (dir == Direction.NORTH || dir == Direction.NORTHEAST || dir == Direction.NORTHWEST) {
					behind = Direction.SOUTH;
				} else if (dir == Direction.WEST) {
					behind = Direction.EAST;
				} else if (dir == Direction.EAST) {
					behind = Direction.WEST;
				}
	    		if (rc.canBuildRobot(RobotType.DESIGN_SCHOOL, behind)) {
	    			rc.buildRobot(RobotType.DESIGN_SCHOOL, behind);
	    			build = false;
	    			int[] message = new int[7];
    		       	message[0] = teamSecret;
    		       	message[1] = 21;
    		       	if (rc.canSubmitTransaction(message, 1)) {
    		       		rc.submitTransaction(message, 1);
    	        	}
    			}
    		} else {
    			tryMove(loc.directionTo(hqLoc));
    		}
    	}
    	if (offensive) {
    		if (rc.getRoundNum() == 3) {
    			enemyHQCandidates(hqLoc);
    		}
			for (Transaction tx : rc.getBlock(rc.getRoundNum() - 1)) {
				int[] mess = tx.getMessage();
				if (mess[0] == teamSecret && mess[1] == 15) {
					index = mess[2];
					waiting = false;
				}
			}
			if(enemyHQLocs != null && enemyHQLocs[index] != null) {
				if (rc.canSenseLocation(enemyHQLocs[index])) {
					RobotInfo check = rc.senseRobotAtLocation(enemyHQLocs[index]);
					if (check == null) {
						index += 1;
					} else if (check.type != RobotType.HQ) {
						index += 1;
					}
				}
				if (loc.isAdjacentTo(enemyHQLocs[index])) {
					for (Direction dir : directions) {
						if (rc.canBuildRobot(RobotType.DESIGN_SCHOOL, dir)) {
							rc.buildRobot(RobotType.DESIGN_SCHOOL, dir);
							offensive = false;
						}
					}
				} else {
					Direction dirToEnemyHQ = loc.directionTo(enemyHQLocs[index]);
					// add check for other locs
					if(!waiting && tryMoveMiner(dirToEnemyHQ)) {
						
					}
					else if (!hasVisited(loc, dirToEnemyHQ) && (rc.senseFlooding(loc.add(dirToEnemyHQ)) || Math.abs(rc.senseElevation(loc) - rc.senseElevation(loc.add(dirToEnemyHQ))) > 3))  {	
						for (Direction dir : directions) {	
							if (rc.canBuildRobot(RobotType.FULFILLMENT_CENTER, dir) && !waiting) {
								if (rc.getTeamSoup() > RobotType.FULFILLMENT_CENTER.cost) {
									System.out.println("run");
									rc.buildRobot(RobotType.FULFILLMENT_CENTER, dir);
									waiting = true;
									int[] message = new int[7];
			    		        	message[0] = teamSecret;
			    		        	message[1] = 11;
			    		        	message[2] = index;
			    		        	message[3] = rc.getID();
			    		        	if (rc.canSubmitTransaction(message, 1)) {
			    		        		rc.submitTransaction(message, 1);
			    		        	}
			    		        	int[] mess = new int[7];
			    		        	mess[0] = teamSecret;
			    		        	mess[1] = 6;
			    		        	if (rc.canSubmitTransaction(mess, 1)) {
			    		        		rc.submitTransaction(mess, 1);
			    		        	}
								}
							} 
						}
					} 
//					if (!waiting) {
//						tryMove(dirToEnemyHQ);
//					} 
				}
			}
    	}

    	if (buildingMiner) {
    		System.out.println("im a building miner");
    		if (ng3Built) {
    			buildingMiner = false;
    			done = true;
    		}
    		// get locations of buildings from blockchain
    		if (turnCount == 1) {
        		for (Transaction tx : rc.getBlock(1)) {
        			int[] mess = tx.getMessage();
        			if (mess[0] == teamSecret && mess[1] == FC_LOC_MESSAGE) {
        				fcLoc = new MapLocation(mess[6], mess[4]);
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
    			if (mess[0] == teamSecret && mess[1] == PROTECTOR_DRONE_MESSAGE) {
    				canBuild = true;
    			}
    		}
        	dsLoc = hqLoc.add(Direction.WEST).add(Direction.WEST).add(Direction.WEST);
    		// check if fulfillment center can be built and build it
        	if (!fcBuilt) {
        		if(fcLoc != null) {
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
        	}
    		// check if vaporators can be built
    		if (fcBuilt && canBuild && !dsBuilt) {
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
    		if (dsBuilt && !ng1Built) {
    			System.out.println("built");
    			adjacent = rc.getLocation().isAdjacentTo(ng1Loc);
    			if (rc.getTeamSoup() > RobotType.NET_GUN.cost && !ng1Built && adjacent) {
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
    				System.out.println("im here");
    				System.out.println("ng1Loc: " + ng1Loc);
    				System.out.println(rc.getLocation().directionTo(ng1Loc));
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
    			if (rc.getTeamSoup() > RobotType.NET_GUN.cost && !ng3Built && adjacent) {
    				if (rc.canBuildRobot(RobotType.NET_GUN, rc.getLocation().directionTo(ng3Loc))) {
    					rc.buildRobot(RobotType.NET_GUN, rc.getLocation().directionTo(ng3Loc));
    					ng3Built = true;
    				}
    			} else if (!adjacent) {
    				tryMove(rc.getLocation().directionTo(ng3Loc));
    			}
    		}
//    		if(forbiddenLocs.contains(rc.getLocation())) {
//    			tryMove(randomDirection(last));
//    		}
    	}
    	// code for normal miners
    	else {
    		if(turnCount == 1) {
    			int radius = (int) Math.sqrt(RobotType.MINER.sensorRadiusSquared);
    			MapLocation curLoc = rc.getLocation();
    			int maxSoup = 0;
    			for(int i=-radius-1; i < radius+1; i++) {
    				for(int j=-radius-1; j < radius+1;j++) {
    					MapLocation loc = new MapLocation(curLoc.x+i, curLoc.y+j);
    					if(rc.canSenseLocation(loc) && rc.senseSoup(loc) > maxSoup) {
    						maxSoup = rc.senseSoup(loc);
    						System.out.println("Soup found at : " + loc +  "soupAmt: " + maxSoup);
    						lastSoupMined = loc;
    						break;
    					}
    				}
    			}
    		}
    		for (Transaction tx : rc.getBlock(rc.getRoundNum() - 1)) {
    			int[] mess = tx.getMessage();
    			if (mess[0] == teamSecret && mess[1] == 7) {
    				done = true;
    			}
    			else if(mess[0] == teamSecret && mess[1] == LS_BUILD_WALL) {
    				abandonHQ = true;
    				lastSoupMined = null;
    			}
    		}
    		if(abandonHQ) {
    			System.out.println("I should be abandoning the enemy HQ");
    		}
//    		if (done) {
//    			enemyHQCandidates(hqLoc);
//    			Direction dirToEnemyHQ = rc.getLocation().directionTo(enemyHQLocs[0]);
//    			tryMove(dirToEnemyHQ);
//    		}
//    		System.out.println(Clock.getBytecodesLeft());
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
	        if(abandonHQ && rc.canSenseLocation(hqLoc) && rc.senseRobotAtLocation(hqLoc).type == RobotType.HQ && rc.getLocation().distanceSquaredTo(hqLoc) < 25 ) {
	        	tryMove(oppositeDirection.get(rc.getLocation().directionTo(hqLoc)));
	        }
	        // go drop off soup at hq
	        if (rc.getSoupCarrying() == rc.getType().soupLimit || (abandonHQ && rc.getLocation().distanceSquaredTo(hqLoc) < 16) ) {
//        	System.out.println("at the soup limit: " + rc.getSoupCarrying());
	        	// time to go back to HQ
	        	counter += 1;
	        	if (counter >= 10) {
	        		for (Direction dir : directions) {
	        			if (rc.canBuildRobot(RobotType.REFINERY, dir)) {
	        				rc.buildRobot(RobotType.REFINERY, dir);
	        			}
	        		}
	        	}
	        	int distToHQ = rc.getLocation().distanceSquaredTo(hqLoc);
	        	if(distToHQ <= REFINERY_DIST_LIMIT && !abandonHQ) {
		        	Direction dirToHQ = loc.directionTo(hqLoc);
		        	if(tryMove(dirToHQ)) {
//	        		System.out.println("moved towards HQ");
//		        	System.out.println("moved to: " + rc.getLocation());
		        	}
	        	}
	        	else {
	        		RobotInfo[] nearbyRobots = rc.senseNearbyRobots(-1, rc.getTeam());
	        		boolean refineryBuilt = false;
	        		for(RobotInfo rob: nearbyRobots) {
	        			if(rob.getType() == RobotType.REFINERY) {
	        				refineryBuilt = true;
	        				hqLoc = rob.getLocation();
	        			}
	        		}
	        		if(!refineryBuilt) {
	        			for(Direction dir: directions) {
	        				if(rc.canBuildRobot(RobotType.REFINERY, dir) && rc.getTeamSoup() > RobotType.REFINERY.cost) {
	        					rc.buildRobot(RobotType.REFINERY, dir);
	        					hqLoc = rc.getLocation().add(dir);
	        				}
	        			}
	        		}
	        	}
	        }
	    	// move towards last soup mined
	    	if (lastSoupMined != null && rc.getSoupCarrying() == 0) {
	    		counter = 0;
	    		Direction dirToSoup = loc.directionTo(lastSoupMined);
	    		if (dirToSoup == Direction.CENTER) {
//	    			System.out.println("ran");
	        		lastSoupMined = null;
	        	} else {
		        	if (tryMove(dirToSoup)) {
		        	System.out.println("moved towards last soup");
		        	}
	        	}
	        }
	    	// sense for soup
	    	searchSoup();
//	    	for (Direction dir : directions) {
//	    		if (dir == Direction.NORTH) {
//	    			d = loc.add(dir).add(dir);
//	    			if (rc.canSenseLocation(d)) {
//	    				if (rc.senseSoup(d) > 0) {
//	    					tryMove(dir);
//	    				}
//	    			}
//	    		} else if (dir == Direction.NORTHEAST) {
//	    			d = loc.add(dir).add(dir);
//	    			if (rc.canSenseLocation(d)) {
//	    				if (rc.senseSoup(d) > 0) {
//	    					tryMove(dir);
//	    				}
//	    			} 
//	    			d = loc.add(dir).add(Direction.NORTH);
//	    			if (rc.canSenseLocation(d)) {
//	    				if (rc.senseSoup(d) > 0) {
//	    					tryMove(dir);
//	    				}
//	    			} 
//	    			d = loc.add(dir).add(Direction.EAST);
//	    			if (rc.canSenseLocation(d)) {
//	    				if (rc.senseSoup(d) > 0) {
//	    					tryMove(dir);
//	    				}
//	    			}
//	    		} else if (dir == Direction.EAST) {
//	    			d = loc.add(dir).add(dir);
//	    			if (rc.canSenseLocation(d)) {
//	    				if (rc.senseSoup(d) > 0) {
//	    					tryMove(dir);
//	    				}
//	    			}
//	    		} else if (dir == Direction.SOUTHEAST) {
//	    			d = loc.add(dir).add(dir);
//	    			if (rc.canSenseLocation(d)) {
//	    				if (rc.senseSoup(d) > 0) {
//	    					tryMove(dir);
//	    				}
//	    			} 
//	    			d = loc.add(dir).add(Direction.SOUTH);
//	    			if (rc.canSenseLocation(d)) {
//	    				if (rc.senseSoup(d) > 0) {
//	    					tryMove(dir);
//	    				}
//	    			}
//	    			d = loc.add(dir).add(Direction.EAST);
//	    			if (rc.canSenseLocation(d)) {
//	    				if (rc.senseSoup(d) > 0) {
//	    					tryMove(dir);
//	    				}
//	    			}
//	    		} else if (dir == Direction.SOUTH) {
//	    			d = loc.add(dir).add(dir);
//	    			if (rc.canSenseLocation(d)) {
//	    				if (rc.senseSoup(d) > 0) {
//	    					tryMove(dir);
//	    				}
//	    			}
//	    		} else if (dir == Direction.SOUTHWEST) {
//	    			d = loc.add(dir).add(dir);
//	    			if (rc.canSenseLocation(d)) {
//	    				if (rc.senseSoup(d) > 0) {
//	    					tryMove(dir);
//	    				}
//	    			}
//	    			d = loc.add(dir).add(Direction.SOUTH);
//	    			if (rc.canSenseLocation(d)) {
//	    				if (rc.senseSoup(d) > 0) {
//	    					tryMove(dir);
//	    				}
//	    			}
//	    			d = loc.add(dir).add(Direction.WEST);
//	    			if (rc.canSenseLocation(d)) {
//	    				if (rc.senseSoup(d) > 0) {
//	    					tryMove(dir);
//	    				}
//	    			}
//	    		} else if (dir == Direction.WEST) {
//	    			d = loc.add(dir).add(dir);
//	    			if (rc.canSenseLocation(d)) {
//	    				if (rc.senseSoup(d) > 0) {
//	    					tryMove(dir);
//	    				}
//	    			}
//	    		} else if (dir == Direction.NORTHWEST) {
//	    			d = loc.add(dir).add(dir);
//	    			if (rc.canSenseLocation(d)) {
//	    				if (rc.senseSoup(d) > 0) {
//	    					tryMove(dir);
//	    				}
//	    			}
//	    			d = loc.add(dir).add(Direction.NORTH);
//	    			if (rc.canSenseLocation(d)) {
//	    				if (rc.senseSoup(d) > 0) {
//	    					tryMove(dir);
//	    				}
//	    			}
//	    			d = loc.add(dir).add(Direction.WEST);
//	    			if (rc.canSenseLocation(d)) {
//	    				if (rc.senseSoup(d) > 0) {
//	    					tryMove(dir);
//	    				}
//	    			}
//	    		}
//	    	}
	    	if (Math.abs(GameConstants.getWaterLevel(rc.getRoundNum()) - rc.senseElevation(loc)) <= 0.5) {
	    		for (Direction dir : directions) {
	    			if (rc.canSenseLocation(loc.add(dir))) {
		    			if (rc.senseElevation(loc.add(dir)) > rc.senseElevation(loc)) {
		    				tryMove(dir);
		    			}
	    			}
	    		}
	    	}
	    	// move in a random direction
	    	if(lastSoupMined != null) {
	    		Direction dirToSoup = loc.directionTo(lastSoupMined);
	    		if (dirToSoup == Direction.CENTER) {
//	    			System.out.println("ran");
	        		lastSoupMined = null;
	        	} else {
		        	if (tryMove(dirToSoup)) {
		        	System.out.println("moved towards last soup");
		        	}
	        	}
	    		
	    	}
	        tryMove(randomDirection(last));
    	}
    }
    
    
    static void searchSoup() throws GameActionException {
    	if(rc.isReady()) {
			int radius = (int) Math.sqrt(RobotType.MINER.sensorRadiusSquared);
			MapLocation curLoc = rc.getLocation();
			int maxSoup = 0;
			for(int i=-radius-1; i < radius+1; i++) {
				for(int j=-radius-1; j < radius+1;j++) {
					MapLocation loc = new MapLocation(curLoc.x+i, curLoc.y+j);
					if(rc.canSenseLocation(loc) && rc.senseSoup(loc) > maxSoup && !rc.senseFlooding(loc) && rc.senseElevation(rc.getLocation()) - rc.senseElevation(loc) <= 3) {
						maxSoup = rc.senseSoup(loc);
						System.out.println("Soup found at : " + loc +  "soupAmt: " + maxSoup);
						lastSoupMined = loc;
						break;
					}
				}
			}
    	}
    }

    static void runRefinery() throws GameActionException {
        // System.out.println("Pollution: " + rc.sensePollution(rc.getLocation()));
    }

    static void runVaporator() throws GameActionException {

    }

    static void runDesignSchool() throws GameActionException {
    	if (turnCount == 1) {
    		for (Transaction tx : rc.getBlock(1)) {
    			int[] mess = tx.getMessage();
    			if (mess[0] == landscapersSecret1) {
    				lsLoc[1] = new MapLocation(mess[1], mess[2]);
    				lsLoc[2] = new MapLocation(mess[3], mess[4]);
    				lsLoc[3] = new MapLocation(mess[5], mess[6]);
    			} else if (mess[0] == landscapersSecret2) {
    				lsLoc[4] = new MapLocation(mess[1], mess[2]);
    				lsLoc[5] = new MapLocation(mess[3], mess[4]);
    				lsLoc[6] = new MapLocation(mess[5], mess[6]);
    			} else if (mess[0] == teamSecret && mess[1] == offenseSecret) {
    				offensive = true;
    			}
    		}
    		if (offensive) {
    			for (Transaction tx : rc.getBlock(rc.getRoundNum() - 1)) {
    				int[] mes = tx.getMessage();
    				if (mes[0] == teamSecret && mes[1] == 21) {
    					defensive = true;
    				}
    			}
    		}
    	}
    	if (offensive) {
    		if (defensive && landscaperCount < 8) {
    			for (Direction dir : directions) {
    				if (tryBuild(RobotType.LANDSCAPER, dir)) {
    					landscaperCount += 1;
    					int[] mess = new int[7];
    					mess[0] = teamSecret;
    					mess[1] = 21;
    					if(rc.canSubmitTransaction(mess, 1)) {
    						rc.submitTransaction(mess, 1);
    					}
    				}
    			}
    		} else if (!defensive){
	    		for (Direction dir : directions) {
	    			tryBuild(RobotType.LANDSCAPER, dir);
	    		}
    		}
    	} else {
	    	for (Transaction tx : rc.getBlock(rc.getRoundNum() - 1)) {

				int[] mess = tx.getMessage();
				if (mess[0] == teamSecret && mess[1] == 6) {
					canBuild = true;
				}
			}
			if (canBuild && landscaperCount < LANDSCAPER_LIMIT && rc.getTeamSoup() > 250) {
				for (Direction dir : directions) {
					if (tryBuild(RobotType.LANDSCAPER, dir)) {
						landscaperCount += 1;
						int[] mess = new int[7];
						mess[0] = teamSecret;
						mess[5] = lsLoc[landscaperCount].x;
						mess[2] = lsLoc[landscaperCount].y;
						mess[3] = landscaperCount;
						if (rc.canSubmitTransaction(mess, 1)) {
							rc.submitTransaction(mess, 1);
						}
					}
				}
			} else if (landscaperCount >= LANDSCAPER_LIMIT && roundsDisplayed < ROUND_DISPLAY_LIMIT) {
				roundsDisplayed += 1;
				int[] message = new int[7];
				message[0] = teamSecret;
				message[1] = LS_BUILD_WALL;
				if (rc.canSubmitTransaction(message, 1)) {
					rc.submitTransaction(message, 1);
				}
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
		if (turnCount == 1) {
			for (Transaction tx : rc.getBlock(rc.getRoundNum() - 1)) {
				int[] mess = tx.getMessage();
				if (mess[0] == teamSecret && mess[1] == 11) {
					offensive = true;
					index = mess[2];
					lsID = mess[3];
					System.out.println(offensive);
				}
			}
		}
		if (offensive) {
			boolean nearbyMiner = false;
			boolean nearbyDrone = false;
			boolean nearbyEnemyHQ = false;
			RobotInfo[] nearbyBots = rc.senseNearbyRobots();
			MapLocation nearbyMinerLoc = null;
			for(RobotInfo rob: nearbyBots) {
				if(rob.type == RobotType.MINER && rob.team == rc.getTeam()) {
					nearbyMiner = true;
					nearbyMinerLoc = rob.location;
				}
				else if(rob.type == RobotType.DELIVERY_DRONE && rob.team == rc.getTeam()) {
					nearbyDrone = true;
				}
				else if(rob.type == RobotType.HQ && rob.team == rc.getTeam().opponent()) {
					nearbyEnemyHQ = true;
					enemyHqLoc = rob.location;
				}
			}
			System.out.println("nearby enemy HQ: " + nearbyEnemyHQ);
			MapLocation curLoc = rc.getLocation();
			if (nearbyMiner && !nearbyDrone) {
				for (Direction dir : directions) {
					if(nearbyMinerLoc.isAdjacentTo(curLoc.add(dir))){
						if (rc.getTeamSoup() > RobotType.DELIVERY_DRONE.cost && (!nearbyEnemyHQ || curLoc.distanceSquaredTo(enemyHqLoc) < curLoc.add(dir).distanceSquaredTo(enemyHqLoc))){
							if (tryBuild(RobotType.DELIVERY_DRONE, dir)) {
								droneCount += 1;
								int[] message = new int[7];
								message[0] = teamSecret;
								message[1] = 10;
								message[2] = index;
								message[3] = lsID;
								if (rc.canSubmitTransaction(message, 1)) {
									rc.submitTransaction(message, 1);
								}
							}
						}
					}
				}
			}
		} else {
			Transaction[] transactions = rc.getBlock(rc.getRoundNum() - 1);
			for(Transaction tx: transactions) {
				int[] message = tx.getMessage();
				if(message[0] == teamSecret && message[1] == LS_BUILD_WALL) {
					droneAttack = true;
				}
			}
			for (Direction dir : directions) {
				if (droneCount < 1 && rc.getTeamSoup() > RobotType.DELIVERY_DRONE.cost || droneAttack && (rc.getTeamSoup() > 1200 || firstWave)) {
					if (tryBuild(RobotType.DELIVERY_DRONE, dir)) {
						if(droneCount == 0) {
							int[] message = new int[7];
							message[0] = teamSecret;
							message[1] = 5;
							if (rc.canSubmitTransaction(message, 1)) {
								rc.submitTransaction(message, 1);
							}
						}
						else {
							firstWave = true;

						}
						droneCount += 1;
					}
				}
			}
		}
	}


	static void runLandscaper() throws GameActionException {
		//    	System.out.println(turnCount);
		if (turnCount == 1) {
			RobotInfo[] nearbyRobots = rc.senseNearbyRobots();
			for(RobotInfo rob: nearbyRobots) {
				if(rob.team == rc.getTeam() && rob.type == RobotType.HQ) {
					hqLoc = rob.location;
				}
			}
			if(hqLoc != null) {
				enemyHQCandidates(hqLoc);
			}

			for (Transaction tx : rc.getBlock(1)) {
				int[] mess = tx.getMessage();
				if (mess[0] == teamSecret && mess[1] == offenseSecret) {
					offensive = true;
					for (RobotInfo r : rc.senseNearbyRobots()) {
						if (r.getType() == RobotType.HQ) {
							enemyHqLoc = r.getLocation();
						}
					}
				} else if (mess[0] == teamSecret && mess[1] == 1 && hqLoc == null) {
					hqLoc = new MapLocation(mess[6],mess[4]);
				}
			}
			
			if (offensive) {
    			for (Transaction tx : rc.getBlock(rc.getRoundNum() - 1)) {
    				int[] mes = tx.getMessage();
    				if (mes[0] == teamSecret && mes[1] == 21) {
    					defensive = true;
    				}
    			}
    		}
			
			if (!offensive) {
				for (Transaction tx : rc.getBlock(rc.getRoundNum() - 1)) {
					int[] mess = tx.getMessage();
					if (mess[0] == teamSecret) {
						standLoc = new MapLocation(mess[5], mess[2]);
						lsID = mess[3];
						if (mess[3] <= 3) {
							behind = Direction.NORTH;
						} else if (mess[3] == 5) {
							behind = Direction.SOUTH;
						} else if (mess[3] == 6) {
							behind = Direction.SOUTHWEST;
						} else if (mess[3] == 4) {
							behind = Direction.EAST;
						}
					}
				}
				if (lsID == 2 || lsID == 5) {
					placeDirt[0] = Direction.CENTER;
					placeDirt[1] = Direction.WEST;
					placeDirt[2] = Direction.EAST;
				} else if (lsID == 4 || lsID == 6) {
					placeDirt[0] = Direction.CENTER;
					placeDirt[1] = Direction.NORTH;
					placeDirt[2] = Direction.SOUTH;
				} else if (lsID == 3) {
					placeDirt[0] = Direction.CENTER;
					placeDirt[1] = Direction.EAST;
					placeDirt[2] = Direction.SOUTH;
				} else if (lsID == 1) {
					placeDirt[0] = Direction.CENTER;
					placeDirt[1] = Direction.WEST;
					placeDirt[2] = Direction.SOUTH;
				}
			}
		}
		loc = rc.getLocation();
		if (offensive) {
			if (defensive) {
				for (Transaction tx : rc.getBlock(rc.getRoundNum() - 1)) {
					int[] mess = tx.getMessage();
					if (mess[0] == teamSecret && mess[1] == 23) {
						canBuild = true;
					}
				}
				if (loc.isAdjacentTo(hqLoc)) {
					if (rc.getDirtCarrying() == 1 && canBuild) {
						if (rc.canDepositDirt(Direction.CENTER)) {
							rc.depositDirt(Direction.CENTER);
						}
					} else {
						if (behind == null) {
							Direction dir = loc.directionTo(hqLoc);
							if (dir == Direction.SOUTH || dir == Direction.SOUTHEAST || dir == Direction.SOUTHWEST) {
								behind = Direction.NORTH;
							} else if (dir == Direction.NORTH || dir == Direction.NORTHEAST || dir == Direction.NORTHWEST) {
								behind = Direction.SOUTH;
							} else if (dir == Direction.WEST) {
								behind = Direction.EAST;
							} else if (dir == Direction.EAST) {
								behind = Direction.WEST;
							}
						}
						if (rc.canDigDirt(behind) && canBuild) {
							rc.digDirt(behind);
						}
					}
				} else {
					tryMove(loc.directionTo(hqLoc));
				}
			} else {
				if (rc.getLocation().isAdjacentTo(enemyHqLoc) && !defensive) {
					if (rc.getDirtCarrying() < 1) {
						for (Direction dir : directions) {
							if (rc.canDigDirt(dir) && rc.senseRobotAtLocation(rc.getLocation().add(dir)) == null) {
								rc.digDirt(dir);
							}
						}
					} else {
						if (rc.canDepositDirt(rc.getLocation().directionTo(enemyHqLoc))) {
							rc.depositDirt(rc.getLocation().directionTo(enemyHqLoc));
						}
					}
				} else {
					tryMove(rc.getLocation().directionTo(enemyHqLoc));
				}
			}
		} else {
			if (stop || !hasStopped) {
				for (Transaction tx : rc.getBlock(rc.getRoundNum() - 1)) {
					int[] mess = tx.getMessage();
					if (mess[0] == teamSecret && mess[1] == LS_BUILD_WALL) {
						System.out.println("l");
						stop = false;
						hasStopped = true;
					}
				}
			}
			// walk to place
			loc = rc.getLocation();
			if (loc.directionTo(standLoc) != Direction.CENTER) {
				tryMove(loc.directionTo(standLoc));
			} else {
				atLoc = true;
			}
			// 		build wall
			if (!replaceDirt && !stop) {
				for (Direction dir : placeDirt) {
					if (dir != last && dir != lastLast) {
						if (rc.canDepositDirt(dir)) {
							rc.depositDirt(dir);
							lastLast = last;
							last = dir;
							if (rc.getDirtCarrying() == 0) {
								replaceDirt = true;
							}
							if (!hasStopped && rc.getDirtCarrying() == 0) {
								stop = true;
								hasStopped = true;
							}
						}
					}
				}
			}
			// 		dig dirt behind 
			if (atLoc == true && replaceDirt) {
				if (rc.canDigDirt(behind)) { 
					rc.digDirt(behind);
					if (rc.getDirtCarrying() == DIRT_LIMIT) {
						replaceDirt = false;
					}
				}
			}
		}
	}

	static void runDeliveryDrone() throws GameActionException {
		// get the HQ location on its first turn
		if (turnCount == 1) {
			for (Transaction tx : rc.getBlock(1)) {
				int[] mess = tx.getMessage();
				if (mess[0] == teamSecret && mess[1] == HQ_LOC_MESSAGE) {
					hqLoc = new MapLocation(mess[6], mess[4]);
				}
			}

			// reads blockchain for protector drone
			//			int i = 1;
			//			boolean found = false;
			//			while (!found) {
			//				for (Transaction tx : rc.getBlock(rc.getRoundNum() - i)) {
			//					int[] mess = tx.getMessage();
			//					if (mess[0] == teamSecret && mess[1] == PROTECTOR_DRONE_MESSAGE) {
			//						protectorDrone = true;
			//						found = true;
			//					} else if (mess[0] == teamSecret && mess[1] == 10){
			//						offensive = true;
			//						enemyHQCandidates(hqLoc);
			//						index = mess[2];
			//						lsID = mess[3];
			//						found = true;
			//						for (RobotInfo rob : rc.senseNearbyRobots()) {
			//							if (rob.getTeam() == rc.getTeam() && rob.getID() == lsID) {
			//								loc = rob.getLocation();
			//							}
			//						}
			//					} else {
			//						i += 1;
			//					}
			//				}
			//			}
			if(turnCount == 1) {
				for (Transaction tx : rc.getBlock(rc.getRoundNum() - 1)) {
					int[] mess = tx.getMessage();
					if (mess[0] == teamSecret && mess[1] == PROTECTOR_DRONE_MESSAGE) {
						protectorDrone = true;
					} else if (mess[0] == teamSecret && mess[1] == 10){
						offensive = true;
						enemyHQCandidates(hqLoc);
						index = mess[2];
						lsID = mess[3];
						System.out.println(offensive);
					}
				}
				System.out.println("offensive drone: " + offensive);
				System.out.println("protector drone: " + protectorDrone);
			}
		}

		if (offensive) {
			System.out.println("im offensive drone!");
			System.out.println("enemyHQLoc: " + enemyHQLocs[index]);
			if (rc.canSenseLocation(enemyHQLocs[index])) {
				RobotInfo check = rc.senseRobotAtLocation(enemyHQLocs[index]);
				if (check == null) {
					index += 1;
				} else if (check.type != RobotType.HQ) {
					index += 1;
				}
				index = index % enemyHQLocs.length;
			}
			if (!rc.isCurrentlyHoldingUnit()) {
				if (rc.canPickUpUnit(lsID)) {
					rc.pickUpUnit(lsID);
				} else {
					for (RobotInfo rob : rc.senseNearbyRobots()) {
						if (rob.ID == lsID) {
							loc = rob.getLocation();
						}
					}
					moveOffensiveDrone(rc.getLocation().directionTo(loc));
					//					tryMove(rc.getLocation().directionTo(loc));
				}
			}
			if (rc.isCurrentlyHoldingUnit()) {
				int[] message = new int[7];
				message[0] = teamSecret;
				message[1] = 15;
				message[2] = index;
				if (rc.canSubmitTransaction(message, 1)) {
					rc.submitTransaction(message, 1);
				}
				if(dropOffLoc != null) {
					if(rc.getLocation().isAdjacentTo(dropOffLoc)) {
						for (Direction dir : directions) {
//							double elevationDiff = Math.abs(rc.senseElevation(enemyHQLocs[index]) - rc.senseElevation(rc.getLocation().add(dir)));
//							System.out.println("elevation diff: " + elevationDiff);
							if (rc.canDropUnit(dir) && rc.getLocation().add(dir).distanceSquaredTo(enemyHQLocs[index]) > RobotType.NET_GUN.sensorRadiusSquared) {
								rc.dropUnit(dir);
								offensive = false;
							}
						}
					}
					Direction dirToDropOff = rc.getLocation().directionTo(dropOffLoc);
					tryMove(dirToDropOff);
				}
				if (rc.canSenseLocation(enemyHQLocs[index])) {
					if (rc.senseRobotAtLocation(enemyHQLocs[index]).type == RobotType.HQ) {
						searchLowerGround();
						if(dropOffLoc != null) {
							Direction dirToDropOff = rc.getLocation().directionTo(dropOffLoc);
							tryMove(dirToDropOff);
						}
//						if (Math.abs(rc.senseElevation(enemyHQLocs[index]) - rc.senseElevation(rc.getLocation())) > 10) {
//							searchLowerGround();
//							if(dropOffLoc != null) {
//								Direction dirToDropOff = rc.getLocation().directionTo(dropOffLoc);
//								tryMove(dirToDropOff);
//							}
//						}
//						else {
//							for (Direction dir : directions) {
//								double elevationDiff = Math.abs(rc.senseElevation(enemyHQLocs[index]) - rc.senseElevation(rc.getLocation().add(dir)));
//								System.out.println("elevation diff: " + elevationDiff);
//								if (rc.canDropUnit(dir) &&  elevationDiff<= 10 && rc.getLocation().add(dir).distanceSquaredTo(enemyHQLocs[index]) > RobotType.NET_GUN.sensorRadiusSquared) {
//									rc.dropUnit(dir);
//									offensive = false;
//								}
//							}
//						} 
					}

					//						}
				}
				else {
					Direction dirToEnemyHQ = rc.getLocation().directionTo(enemyHQLocs[index]);
					//					System.out.println(di)
					//					tryMove(dirToEnemyHQ);
					moveOffensiveDrone(dirToEnemyHQ);
				}
			}
		}
		else {

			MapLocation curLoc = rc.getLocation();
			Team enemy = rc.getTeam().opponent();
			MapLocation droneLoc = hqLoc.add(Direction.EAST).add(Direction.EAST);
			// controls for protector drone
			if (protectorDrone && rc.isReady() && !rc.isCurrentlyHoldingUnit() && !curLoc.equals(droneLoc)) {
				if(!curLoc.isAdjacentTo(droneLoc) || rc.canSenseLocation(droneLoc) ) {
					Direction dirToDroneLoc = curLoc.directionTo(droneLoc);
					tryMove(dirToDroneLoc);
				}
			}	else if(!protectorDrone && rc.isReady()) {
				//			if(rc.canSenseLocation(hqLoc)) {
				//				RobotInfo[] nearbyMiners = rc.senseNearbyRobots(GameConstants.DELIVERY_DRONE_PICKUP_RADIUS_SQUARED, rc.getTeam());
				//				for(RobotInfo r: nearbyMiners) {
				//					if(rc.canPickUpUnit(r.ID)&& r.type == RobotType.MINER) {
				//						rc.pickUpUnit(r.ID);
				//					}
				//				}
				//			}
				if(enemyHQLocs == null) {
					enemyHQCandidates(hqLoc);
				} 
				if(enemyHqLoc != null) {
					System.out.println(enemyHqLoc);
					if(rc.canSenseLocation(enemyHqLoc)) {

						RobotInfo[] pickupRobots = rc.senseNearbyRobots(GameConstants.DELIVERY_DRONE_PICKUP_RADIUS_SQUARED, enemy);

						for(RobotInfo rob: pickupRobots) {
							if (rob.type != RobotType.HQ && rc.canPickUpUnit(rob.ID)) {
								// Pick up a first robot within range
								rc.pickUpUnit(rob.ID);
								//                    System.out.println("I picked up " + robots[0].getID() + "!");
							}
						}

						RobotInfo[] nearbyRobots = rc.senseNearbyRobots(-1, enemy);
						RobotInfo approachToRobot = null;
						int closestBot = Integer.MAX_VALUE;

						for(RobotInfo r: nearbyRobots) {
							int distToBot = curLoc.distanceSquaredTo(r.location);
							if(r.type != RobotType.HQ && distToBot < closestBot) {
								approachToRobot = r;
							}
						}

						if(approachToRobot != null) {
							Direction dirToEnemy = curLoc.directionTo(approachToRobot.location);
							System.out.println(dirToEnemy);
							tryMove(dirToEnemy);
						}
					}
					Direction dirToEnemyHQ = curLoc.directionTo(enemyHqLoc);
					tryMove(dirToEnemyHQ);
				}
				MapLocation curCandidate = enemyHQLocs[enemyHQIndex];
				Direction desiredDir = curLoc.directionTo(curCandidate);
				if(rc.canSenseLocation(curCandidate)) {
					RobotInfo enemyRobot = rc.senseRobotAtLocation(curCandidate);
					if(enemyRobot != null && enemyRobot.team == enemy && enemyRobot.type == RobotType.HQ) {
						enemyHqLoc = curCandidate;
						System.out.println("found enemy hq");
					}
					else {
						System.out.println("im at hq Loc " + curCandidate + " but i didn't find hq");
						enemyHQIndex = (enemyHQIndex + 1) % enemyHQLocs.length;
						curCandidate = enemyHQLocs[enemyHQIndex];
						desiredDir = curLoc.directionTo(curCandidate);
					}
				}
				System.out.println("current Candidate: " + curCandidate);
				System.out.println("desired Direction: " + desiredDir);
				System.out.println("cooldown turns: " + rc.getCooldownTurns());
				tryMove(desiredDir);

			}
			if (!rc.isCurrentlyHoldingUnit() && rc.isReady()) {
				// See if there are any enemy robots within striking range (distance 1 from lumberjack's radius)
				if(rc.senseFlooding(curLoc)) {
					Direction dirToBase = curLoc.directionTo(droneLoc);
					tryMove(dirToBase);
				}
				RobotInfo[] pickupRobots = rc.senseNearbyRobots(GameConstants.DELIVERY_DRONE_PICKUP_RADIUS_SQUARED, enemy);

				for(RobotInfo rob: pickupRobots) {
					if (rob.type != RobotType.HQ && rc.canPickUpUnit(rob.ID)) {
						// Pick up a first robot within range
						rc.pickUpUnit(rob.ID);
						//                    System.out.println("I picked up " + robots[0].getID() + "!");
					}
				}

				RobotInfo[] nearbyRobots = rc.senseNearbyRobots(-1, enemy);
				RobotInfo approachToRobot = null;
				int closestBot = Integer.MAX_VALUE;

				for(RobotInfo r: nearbyRobots) {
					int distToBot = curLoc.distanceSquaredTo(r.location);
					if(distToBot < closestBot) {
						approachToRobot = r;
					}
				}
				if(approachToRobot != null) {
					Direction dirToEnemy = curLoc.directionTo(approachToRobot.location);
					System.out.println(dirToEnemy);
					tryMove(dirToEnemy);
				}
			}
			else if(rc.isReady()) {
				System.out.println("currently carrying a unit");
				if(waterLoc != null) {
					System.out.println("waterLoc: " + waterLoc);
				}
				if(droneHoldingFor == DRONE_HOLD_LIMIT) {
					if(enemyHQLocs == null) {
						enemyHQCandidates(hqLoc);
					}
					waterLoc = enemyHQLocs[(int) (Math.random()*enemyHQLocs.length)];
				}
				if(waterLoc != null && !curLoc.equals(waterLoc) && !rc.senseFlooding(curLoc) && !curLoc.isAdjacentTo(waterLoc)) {
					Direction dirToWater = curLoc.directionTo(waterLoc);
					System.out.println("trying to move to whhere i can sense water");
					tryMove(dirToWater);
				}


				for(Direction dir: directions) {
					if(rc.canSenseLocation(curLoc.add(dir))) {
						if((rc.senseFlooding(curLoc.add(dir)) || (waterLoc != null && curLoc.isAdjacentTo(waterLoc) && rc.senseFlooding(curLoc.add(dir))))  && rc.canDropUnit(dir)) {
							System.out.println("I am trying to drop the unit");
							rc.dropUnit(dir);
							droneHoldingFor = 0;
							waterLoc = curLoc;
						}
					}
				}

				if(waterLoc == null) {
					searchFlooding();
				}

				if(waterLoc != null) {
					Direction dirToWater = curLoc.directionTo(waterLoc);
					tryMove(dirToWater);
				}
				tryMove(randomDirection(last));
				droneHoldingFor += 1;
			}
		}
	}


	static boolean moveOffensiveDrone(Direction dir) throws GameActionException{

		boolean didMove = false;
		boolean enemyHQNearby = false;
		RobotInfo[] nearbyBots = rc.senseNearbyRobots();
		for(RobotInfo rob: nearbyBots) {
			if(rob.type == RobotType.HQ && rob.team == rc.getTeam().opponent()) {
				enemyHQNearby = true;
				enemyHqLoc = rob.location;
			}
		}

		MapLocation curLoc = rc.getLocation();
		if ((rc.isReady() && rc.canMove(dir)) && (!enemyHQNearby || (enemyHQNearby && curLoc.add(dir).distanceSquaredTo(enemyHqLoc) > RobotType.NET_GUN.sensorRadiusSquared))) {
			rc.move(dir);
			last = dir;
			didMove = true;
		} 
		if(!didMove) {
			Direction [] altDirs = alternateDirs.get(dir);
			if(altDirs != null) {
				for(Direction d: altDirs) {

					if ((rc.isReady() && rc.canMove(dir)) && (!enemyHQNearby || (enemyHQNearby && curLoc.add(dir).distanceSquaredTo(enemyHqLoc) > RobotType.NET_GUN.sensorRadiusSquared))) {
						rc.move(d);
						last = d;
						didMove = true;
					} 
				}
			}
			else {
				return tryMove(randomDirection(last));
			}
		}
		if(didMove) {
			updateVisited();
		}

		return didMove;

	}

	static void searchFlooding() throws GameActionException {
		if(rc.isReady()) {
			int radius = (int) Math.sqrt(RobotType.DELIVERY_DRONE.sensorRadiusSquared);
			MapLocation curLoc = rc.getLocation();
			//		int mostFlooded = Integer.MAX_VALUE;
			boolean hasMoved = false;
			for(int i=-radius-1; i < radius+1; i++) {
				if(hasMoved) {
					break;
				}
				for(int j=-radius-1; j < radius+1;j++) {
					MapLocation loc = new MapLocation(curLoc.x+i, curLoc.y+j);
					if(rc.canSenseLocation(loc) && rc.senseFlooding(loc)) {
						//					mostFlooded = rc.senseElevation(loc);
						//System.out.println("Soup found at : " + loc +  "soupAmt: " + maxSoup);
						waterLoc = loc;
						hasMoved = true;
						break;
					}
				}
			}
		}
	}

	static void searchLowerGround() throws GameActionException {
		if(rc.isReady()) {
			System.out.println("rant");
			int radius = (int) Math.sqrt(RobotType.DELIVERY_DRONE.sensorRadiusSquared);
			MapLocation curLoc = rc.getLocation();
			MapLocation curCandidate = enemyHQLocs[index];
			int lowestElevation = Integer.MAX_VALUE;
			for(int i=-radius-1; i < radius+1; i++) {
				for(int j=-radius-1; j < radius+1;j++) {
					MapLocation loc = new MapLocation(curLoc.x+i, curLoc.y+j);
					double curDist = curLoc.distanceSquaredTo(curCandidate);
					double newDist = loc.distanceSquaredTo(curCandidate);
					Direction dir = curLoc.directionTo(loc);
					System.out.println(newDist);
					System.out.println(curDist);
					
					if(rc.canSenseLocation(loc) && rc.canMove(dir)) {
						System.out.println(newDist);
						boolean isLower = rc.senseElevation(loc) < lowestElevation;
						boolean isSafe = newDist > RobotType.NET_GUN.sensorRadiusSquared;
						//						mostFlooded = rc.senseElevation(loc);
						//System.out.println("Soup found at : " + loc +  "soupAmt: " + maxSoup);
						//if (rc.senseElevation(curLoc) > rc.senseElevation(loc)) {
						if(isLower && isSafe) {
							dropOffLoc = loc;
							lowestElevation = rc.senseElevation(loc);
						}
						//}
					}
				}
			}
		}
	}

	static void runNetGun() throws GameActionException {
		if(rc.isReady()) {
			Team enemy = rc.getTeam().opponent();
			RobotInfo[] nearbyBots = rc.senseNearbyRobots(RobotType.NET_GUN.sensorRadiusSquared, enemy);
			if(nearbyBots.length > 0) {
				for(RobotInfo rob: nearbyBots) {
					if(rob.type == RobotType.DELIVERY_DRONE) {
						if(rc.canShootUnit(rob.getID())) {
							rc.shootUnit(rob.getID());
						}
					}
				}
			}
		}
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
		Direction randDir = directions[(int) (Math.random() * directions.length)];
		MapLocation curLoc = rc.getLocation();
		if(rc.isReady() && rc.canMove(randDir) && !rc.senseFlooding(curLoc.add(randDir))) {
			rc.move(randDir);
			last = randDir;
			return true;
		}
		return false;
		
	}
	
	static boolean tryMoveMiner(Direction dir) throws GameActionException{
		MapLocation curLoc = rc.getLocation();
		if(rc.isReady() && rc.canMove(dir) && !rc.senseFlooding(curLoc.add(dir))) {
			rc.move(dir);
			return true;
		} else {
			if (rc.isReady() && rc.canMove(alternateDirs.get(dir)[0]) && !rc.senseFlooding(curLoc.add(alternateDirs.get(dir)[0]))) {
				rc.move(alternateDirs.get(dir)[0]);
				return true;
			} else if (rc.isReady() && rc.canMove(alternateDirs.get(dir)[1]) && !rc.senseFlooding(curLoc.add(alternateDirs.get(dir)[1]))) {
				rc.move(alternateDirs.get(dir)[1]);
				return true;
			}
		}
		return false;
	}

	//	/**
	//	 * Attempts to move in a given direction.
	//	 *
	//	 * @param dir The intended direction of movement
	//	 * @return true if a move was performed
	//	 * @throws GameActionException
	//	 */
	//	static boolean tryMove(Direction dir) throws GameActionException {
	//		boolean didMove = false;
	//		//        System.out.println(dir);
	//		//       System.out.println(lastDesiredDir);
	//		// System.out.println("I am trying to move " + dir + "; " + rc.isReady() + " " + rc.getCooldownTurns() + " " + rc.canMove(dir));
	//		if (rc.isReady() && rc.canMove(dir) && (!rc.senseFlooding(rc.getLocation().add(dir)) || rc.getType() == RobotType.DELIVERY_DRONE)) {
	//			rc.move(dir);
	//			last = dir;
	//			didMove = true;
	//		} 
	//		if (lastDesiredDir != null) {
	//			if (rc.isReady() && rc.canMove(lastDesiredDir) && (!rc.senseFlooding(rc.getLocation().add(lastDesiredDir)) || rc.getType() == RobotType.DELIVERY_DRONE)) {
	//				rc.move(lastDesiredDir);
	//				last = lastDesiredDir;
	//				didMove = true;
	//			}
	//		}
	//		//        System.out.println(didMove);
	//		if (!didMove){
	//			System.out.println("altDirs: " + alternateDirs);
	//			System.out.println("alts " + alternateDirs.get(dir));
	//			System.out.println("direction " + dir);
	//			Direction[] altDirs = alternateDirs.get(dir);
	//			for(Direction d: altDirs) {
	//				if (rc.isReady() && rc.canMove(d) && (!rc.senseFlooding(rc.getLocation().add(d)) || rc.getType() == RobotType.DELIVERY_DRONE)) {
	//					rc.move(d);
	//					last = d;
	//					didMove = true;
	//				}
	//			}
	//		}
	//		lastDesiredDir = dir;
	//		return didMove;
	//
	//	}


	/**
	 * Attempts to move in a given direction.
	 *
	 * @param dir The intended direction of movement
	 * @return true if a move was performed
	 * @throws GameActionException
	 */
	static boolean tryMove(Direction dir) throws GameActionException {

		boolean didMove = false;
		MapLocation curLoc = rc.getLocation();
		if (rc.isReady() && rc.canMove(dir) && (!rc.senseFlooding(curLoc.add(dir)) && !hasVisited(curLoc, dir) || rc.getType() == RobotType.DELIVERY_DRONE )) {
			rc.move(dir);
			last = dir;
			didMove = true;
		} 
		if(!didMove) {
			Direction [] altDirs = alternateDirs.get(dir);
			if(altDirs != null) {
				for(Direction d: altDirs) {
					if (rc.isReady() && rc.canMove(d) && (!rc.senseFlooding(curLoc.add(d)) && !hasVisited(curLoc, d) || rc.getType() == RobotType.DELIVERY_DRONE)) {
						rc.move(d);
						last = d;
						didMove = true;
					} 
				}
			}
			else {
				return tryMove(randomDirection(last));
			}
		}
		System.out.println(isStuck());
		if(!didMove && isStuck()) {
			while(true) {
				if(tryMove()){
					break;
				};
			}
		}
		if(didMove) {
			updateVisited();
		}

		return didMove;
	}


	static boolean isStuck() {
		System.out.println("start of func");
		for(MapLocation loc: visited) {
			System.out.println("loc: " + loc);
		}
		Set<MapLocation> locMap = new HashSet<>();
		for(MapLocation loc: visited) {
			if(loc == null) {
				return false;
			}
			locMap.add(loc);
		}
		System.out.println("end of func");
		if(locMap.size() <= 2) {
			return true;
		}
		return false;
	}

	/**
	 * checks if the location trying to move to has been visited
	 * @param curLoc robots current location
	 * @param dir direction trying to move to 
	 * @return whether robot can move in that dir
	 */
	static boolean hasVisited(MapLocation curLoc, Direction dir) {
		if(buildingMiner) {
			return false;
		}
		for(MapLocation loc: visited) {
			if(loc != null && loc.equals(curLoc.add(dir))) {
				return true;
			}
		}
		return false;
	}

	/**
	 * updates the visited array
	 */
	static void updateVisited() {
		for(int i = visited.length-1; i > 0; i--) {
			visited[i] = visited[i-1];
		}
		visited[0] = rc.getLocation();
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
