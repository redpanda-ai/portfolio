/* 
by: J. Andrew Key (Andy) 
	Please visit my website at http://www.joeandrewkey.com 

PURPOSE:  This class creats an Ant that negotiates a maze of tiles searching
for food.
BEHAVIOR:  This Ant tries to learn everything it can about the maze first.  If it encounters another ant on the same tile, it will send its map along with coordinates for food that it has found.  Once it knows the entire maze, it takes the shortest path to the nearest food, picks up some, and returns to the nest.  Once at the nest, it uses its knowledge of the maze to go to the nearest food sourceto bring back more food.
FUTURE IMPROVEMENTS:  

1.  Add JavaDoc comments :)
2.  Implementation message compression before a "send".  However, the datagram is not very large, so it is a judgement call as to whether this of much use. 
*/

import ants.*;
import java.lang.Math;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Random;
import java.util.Set;

public class AndyAnt implements Ant{

	private final int BYTE_SIZE = 8;
	private final int BYTES_PER_INT = 4;
	private final int MAP_SIDE = 35;
	private final int MAP_DIMENSIONS = MAP_SIDE*MAP_SIDE;
	private final int START_LOCATION = MAP_DIMENSIONS/2;
	
	private byte[] map = new byte[MAP_DIMENSIONS];
	//STORAGE FOR SURROUNDINGS
	private int[] ants = { 0, 0, 0, 0, 0 };
	private int[] food = { 0, 0, 0, 0, 0 };
	private int[] path = { 0, 0, 0, 0, 0 };

	private int myLocation = START_LOCATION;
	private Node myNode = new Node(myLocation);
	private Direction chosenDirection = Direction.WEST;
	private Hashtable<Integer,Direction> lookup;
	private Hashtable<Direction,Integer> reverseLookup;
	private ArrayList<Node> pathToTarget
		= new ArrayList<Node>(100);
	private boolean fullMapFound = false;
	private boolean wasEverAlone = false;
	private Set<Node> otherAntDestinations = new HashSet<Node>();
	private Set<Node> foodNodes = new HashSet<Node>();
	private Set<Node> outOfFoodNodes = new HashSet<Node>();
	private boolean hasCargo = false;
	private int age = 0;
	//dijkstra variables
	public int INFINITE_DISTANCE = Integer.MAX_VALUE;
	public int INITIAL_CAPACITY = 20;
	//Settled Nodes
	private Set<Node> settledNodes = new HashSet<Node>();
	private boolean isSettled(Node node) {
		return settledNodes.contains(node);
	}
	//Shortest Distances
	private Map<Node, Integer> shortestDistances = 
		new HashMap<Node, Integer>();
	private void setShortestDistance(Node node, int distance) {
		unsettledNodes.remove(node);
		shortestDistances.put(node, distance);
		unsettledNodes.add(node);
	}
	private int getShortestDistance(Node node) {
		Integer d = shortestDistances.get(node);
		return (d == null) ? INFINITE_DISTANCE : d;
	}
	//Predecessors
	private Map<Node, Node> predecessors = 
		new HashMap<Node, Node>();
	private void setPredecessor(Node a, Node b) {
		predecessors.put(a,b);
	}
	public Node getPredecessor(Node node) {
		return predecessors.get(node);
	}
	//Unsettled Nodes
	private final Comparator<Node> shortestDistanceComparator = 
	new Comparator<Node>() {
		public int compare(Node left, Node right) {
			int result = getShortestDistance(left) - getShortestDistance(right);
			return (result == 0) ? left.compareTo(right) : result;
		}
	};

	//Min-heap dijkstra's unsettled nodes that uses our comparator
	private PriorityQueue<Node> unsettledNodes =
		new PriorityQueue<Node>(INITIAL_CAPACITY, shortestDistanceComparator);
	
	//MysteriousNodes
	private Set<Node> mysteriousNodes = new HashSet<Node>();

	//dijkstra Node class
	private final class Node implements Comparable<Node> {
		private int offset; 
		public Node(int offset) {
			this.offset = offset;
		}
		public int getOffset() {
			return this.offset;
		}
		public void setOffset(int o) {
			this.offset = o;
		}
		@Override public boolean equals(Object oThat) {
			if ( this == oThat) return true;
			if ( !(oThat instanceof Node) ) return false;
			Node that = (Node) oThat;
			return (this.offset == that.getOffset());
		}
		@Override public int hashCode() {
			final int SEED = 193;
			final int PRIME = 37;
			return (PRIME * SEED) + this.offset;
		}
		public int compareTo(Node that) {
			final int BEFORE = -1;
			final int EQUAL = 0;
			final int AFTER = 1;

			if ( this == that) return EQUAL;
			
			if (this.offset < that.getOffset()) return BEFORE;
			if (this.offset > that.getOffset()) return AFTER;

			assert this.equals(that) : "compareTo inconsistent with equals!";

			return EQUAL;
		}
		public String toString() {
			return (new Integer(offset)).toString();
		}
	}

	//Ant class
	public AndyAnt() {
		for(int i =0;i < MAP_DIMENSIONS;i++) {
				map[i] = '?';
		}
		map[myLocation] = '1';
		lookup = new Hashtable<Integer,Direction>();
		lookup.put(1,Direction.NORTH);
		lookup.put(2,Direction.EAST);
		lookup.put(3,Direction.SOUTH);
		lookup.put(4,Direction.WEST);
		reverseLookup = new Hashtable<Direction,Integer>();
		reverseLookup.put(Direction.NORTH,1);
		reverseLookup.put(Direction.EAST,2);
		reverseLookup.put(Direction.SOUTH,3);
		reverseLookup.put(Direction.WEST,4);
		outOfFoodNodes.add((new Node(START_LOCATION)));
	}

	//determines if nodes in any cardinal direction are mysterious
	private boolean isMysterious(int offset) {
		int testIndex = offset;
		for (Direction c : Direction.values()) {
			if (map[offset + getOffset(c)] == '?') {
				return true;
			}
		}
		return false;
	}

	//scans the entire map, if there is a path tile, check for mysteries
	private void scanPathForMysteries() {
		boolean isMysterious = false;
		mysteriousNodes.clear();
		for (int i=0;i<map.length;i++) {
			if (map[i] == '1') {
				if (isMysterious(i)) { 
					mysteriousNodes.add(new Node(i));
				}
			}
		}
	}

	//selects a random direction from a list of possible directions
	private Action getRandomDirection() {
		ArrayList<Direction> possibles = new ArrayList<Direction>();
		int i = reverseLookup.get(chosenDirection).intValue() + 1;
		for(;i<5;i++) {
			if (path[i] != 0) {
				possibles.add(lookup.get((new Integer(i))));
			}
		}
		for(int j = 1;j<i;j++) {
			if (path[j] != 0) {
				possibles.add(lookup.get((new Integer(j))));
			}
		}
		Random randomGenerator = new Random();
		chosenDirection = possibles.get(randomGenerator.nextInt(
			possibles.size()));
		this.myLocation += getOffset(chosenDirection);
		return Action.move(chosenDirection);
	}

	private Node getClosestFood() {
		//HERE CHECK BLACKLIST, MAYBE YOU DON'T NEED TO
		Iterator<Node> iter = foodNodes.iterator();
		Node nextFoodNode;
		int shortestPathSize = 0;
		if (iter.hasNext() ) {
			nextFoodNode = iter.next();
			dijkstra(myNode,nextFoodNode);
		} else {
			System.out.println("Some mazes are cruel. No food. Age of dying ant: " 
				+ (new Integer(age)).toString());
			System.exit(0);
		}
		ArrayList<Node> shortestPathSoFar = pathToTarget;
		shortestPathSize = shortestPathSoFar.size();
		while (iter.hasNext()) {
			nextFoodNode = iter.next();
			dijkstra(myNode,nextFoodNode);
			if( pathToTarget.size() < shortestPathSize ) {
				shortestPathSoFar = pathToTarget;
				shortestPathSize = pathToTarget.size();
			}
		}
		pathToTarget = shortestPathSoFar;
		return pathToTarget.get(pathToTarget.size()-1);
	}

	public Action getAction(Surroundings surroundings){
		myNode = new Node(myLocation);
		scan(surroundings);
		updateMap();
		Action a = Action.HALT;
		if (wasEverAlone == false) {
			a = getRandomDirection();
		} else {
			a = selectDirection();
		}
		age+= 1;
		return a;
	}

	private void getPathToTarget(Node start, Node destination) {
		pathToTarget = new ArrayList<Node>(100);
		Node step = destination;
		int counter = 0;
		while ( (step != start) && (step != null)) {
			pathToTarget.add(step);
			step = getPredecessor(step);
		}
		Collections.reverse(pathToTarget);
	}

	private void relaxNeighbors(Node node) {
		ArrayList<Node> adjacentNodes = getAdjacentNodesNotSettled(node);
		Node adjacentNode;
		int newShortestDistance = getShortestDistance(node) + 1;
		while (! adjacentNodes.isEmpty()) {
			adjacentNode = adjacentNodes.remove(0);
			if (getShortestDistance(adjacentNode) > newShortestDistance) {
				setShortestDistance(adjacentNode, newShortestDistance);
				setPredecessor(adjacentNode,node);
			}
		}
		return;
}

	private ArrayList<Node> getAdjacentNodesNotSettled (Node n) {
		ArrayList<Node> results = new ArrayList<Node>();
		int newOffset = 0;
		Node tempNode;
		for (Direction c : Direction.values()) {
			newOffset = n.getOffset() + getOffset(c);
			tempNode = new Node(newOffset);
			if ( (map[newOffset] == '1') && (! isSettled(tempNode))) {
				results.add(tempNode);
			}
		}
		return results;
	}

	private int getOffset(Direction direction) {
		int offset = 0;
		switch(direction) {
			case NORTH: offset = -MAP_SIDE; break;
			case WEST: offset = -1; break;
			case EAST: offset = 1; break;
			case SOUTH: offset = MAP_SIDE; break;
			default : offset = 0; break;
		}
		return offset;
	}

	private void updateMap(Direction direction) {
		int newOffset = myLocation + getOffset(direction);
		if ( (newOffset >= MAP_DIMENSIONS) || (newOffset < 0) ) {
			return;
		}
		if (map[newOffset] == '?') {
			map[newOffset] = (byte)
				(new String(
					(new Integer(path[reverseLookup.get(direction).intValue()])
					.toString())
				))
				.charAt(0);
		}
	}

	private void updateMap() {
		updateMap(Direction.NORTH);
		updateMap(Direction.EAST);
		updateMap(Direction.SOUTH);
		updateMap(Direction.WEST);
	}

	public Direction getDirectionFromPathToTarget() {
		int sizeOfPath = pathToTarget.size();
		Direction result = Direction.NORTH;
		if (pathToTarget.size() == 0) {
			getRandomDirection();
			result = chosenDirection;
			return result;
		}
		Node temp = pathToTarget.remove(0);
		int d = temp.getOffset() - myLocation;
		switch (d) {
			case 1: result = Direction.EAST; break;
			case -1: result = Direction.WEST; break;
			case MAP_SIDE: result = Direction.SOUTH; break;
			case -MAP_SIDE: result = Direction.NORTH; break;
			case 0:
				break;
			default: 
				System.out.println("Huge problem, offset is : " + 
				(new Integer(d)).toString());
				System.out.println("Path size : " + 
				(new Integer(sizeOfPath)).toString());
				System.exit(0);
		}
		return result;
	}

	private void dijkstra(Node start, Node destination) {
		shortestDistances.clear();
		predecessors.clear();
		settledNodes.clear(); 
		unsettledNodes.clear(); 

		unsettledNodes.add(start); 
		shortestDistances.put(start, (new Integer(0)));

		Node minimumNode;
		while ((minimumNode = unsettledNodes.poll()) != null) {
			assert !isSettled(minimumNode);
			if (minimumNode.equals(destination)) {
				break;
			}
			//end new stuff
			settledNodes.add(minimumNode);
			relaxNeighbors(minimumNode);
		}
		getPathToTarget(start, destination);
	}

	private Node getBestMysteryNode () {
		Iterator<Node> iter = mysteriousNodes.iterator();
		Node nextMysteryNode;
		int penalty = 0;
		int shortestPathSize = 0;
		boolean useFirstPenalty = true;
		if (iter.hasNext() ) {
			nextMysteryNode = iter.next();
			penalty = otherAntDestinations.contains(nextMysteryNode) ? 10 : 0;
			dijkstra(myNode,nextMysteryNode);
		}
		ArrayList<Node> shortestPathSoFar = pathToTarget;
		shortestPathSize = shortestPathSoFar.size() + penalty;
		while(iter.hasNext()) {
			nextMysteryNode = iter.next();
			dijkstra(myNode,nextMysteryNode);
			penalty = otherAntDestinations.contains(nextMysteryNode) ? 10 : 0;
			if( (pathToTarget.size() + penalty) < shortestPathSize ) {
				shortestPathSoFar = pathToTarget;
				shortestPathSize = pathToTarget.size() + penalty;
			}
		}
		pathToTarget = shortestPathSoFar;
		return pathToTarget.get(pathToTarget.size()-1);
	}

	public Action selectDirection() {
		Action action = Action.HALT;
		if (pathToTarget.isEmpty()) {
		//REBUILD PATH
			if (fullMapFound == false) { 
			//FIND MYSTERIES
				scanPathForMysteries();
				if (mysteriousNodes.size() == 0) { 
				//NO MORE MYSTERIES, FALL THROUGH TO "RETRIEVE FOOD"
					fullMapFound = true;
				} else { 
				//BUILD PATH TO NEAREST MYSTERY
					Node bestMystery = getBestMysteryNode(); 
				} 
			}
			//RETRIEVE FOOD
			if (fullMapFound == true) { 
				if ( hasCargo == false) {
				//ARRIVE WITH NO CARGO
					if (myLocation != START_LOCATION) {
						if (food[0] > 0) {
						//IF THERE IS FOOD, GATHER CARGO
							hasCargo = true;
							return Action.GATHER;
						} else {
						//IF NO FOOD, BLACKLIST LOCATION AND BUILD PATH TO NEW FOOD
							outOfFoodNodes.add((new Node(myLocation)));
							foodNodes.remove((new Node(myLocation)));
							Node closestFood = getClosestFood();
						}
					}
				} else {
				//ARRIVE WITH CARGO
					if (myLocation != START_LOCATION) {
					//BUILD PATH TO NEST
						dijkstra(myNode,(new Node(START_LOCATION)));
					} else {
					//DROP OFF CARGO AT NEST
						hasCargo = false;
						Node closestFood = getClosestFood();
						return Action.DROP_OFF;
					}
				}
			}
		}
		//WALK PATH
		chosenDirection = getDirectionFromPathToTarget();
		action = Action.move(chosenDirection);
		this.myLocation += getOffset(chosenDirection);
		return action;
	}

	public void recordFoodLocations() {
		Direction direction;
		int offset = 0;
		Node temp;
		for(int i = 1;i<5;i++) {
			if (food[i] > 0) {
				direction = lookup.get(i);
				offset = getOffset(direction);
				temp = new Node(myLocation + offset);
				//offset temp correctly
				if (! outOfFoodNodes.contains(temp)) {
					foodNodes.add(temp);
				}
			}
		}
	}
	public void scan(Surroundings surroundings) {

		//Here
		Tile tile = surroundings.getCurrentTile();
		int index = 0;
		path[index] = tile.isTravelable() ? 1 : 0;
		food[index] = tile.getAmountOfFood();
		ants[index] = tile.getNumAnts();

		//Everywhere else
		for(index = 1;index<=4;index++) {
			tile = surroundings.getTile(lookup.get(new Integer(index)));
			path[index] =  tile.isTravelable() ? 1 : 0;
			food[index] = tile.getAmountOfFood();
			ants[index] = tile.getNumAnts();
		}

		//ever alone?
		if ((ants[0] == 1) && (wasEverAlone == false)) {
			wasEverAlone = true;
		}

		//record food
		recordFoodLocations();
	}

	public byte[] send(){
		//IF THERE ARE NO OTHER ANTS ON YOUR TILE, SEND NO MESSAGE
		if (ants[0] < 2) {
			return null;
		}
		int target = 0;
		int lenPath = 0;

		lenPath = pathToTarget.size();
		if (lenPath == 0) {
			target = myLocation;
		} else {
			target = pathToTarget.get(lenPath-1).getOffset();
		}

		//CRAFT DATAGRAM OUTPUT
		byte[] heading = intToByte(target);
		int firstNonMystery = getIndexOfFirstNonMystery();
		int lastNonMystery = getIndexOfLastNonMystery();
		byte[] firstNonMysteryBytes = intToByte(firstNonMystery);
		byte[] lastNonMysteryBytes = intToByte(lastNonMystery);
		byte[] mapPortionBytes = getMapPortion(firstNonMystery,lastNonMystery);
		byte[] foodLocationBytes = getFoodLocationBytes();
		byte[] datagram = new byte[
			heading.length + 
			firstNonMysteryBytes.length +
			lastNonMysteryBytes.length + 
			mapPortionBytes.length +
			foodLocationBytes.length
		];
		System.arraycopy(heading,0,datagram,0,BYTES_PER_INT);
		System.arraycopy(firstNonMysteryBytes,0,datagram,4,BYTES_PER_INT);
		System.arraycopy(lastNonMysteryBytes,0,datagram,8,BYTES_PER_INT);
		System.arraycopy(mapPortionBytes,0,datagram,12,mapPortionBytes.length);
		System.arraycopy(foodLocationBytes,0,datagram
			, 12+mapPortionBytes.length,foodLocationBytes.length);

		return datagram;
	}

	public byte[] getFoodLocationBytes() {
		Node nextFoodNode;
		int len = foodNodes.size();
		byte[] results = new byte[len*BYTES_PER_INT+1];
		results[0] = (byte) len;
		int resultOffset = 1;
		byte[] chunk = new byte[BYTES_PER_INT];
		Iterator<Node> iter = foodNodes.iterator();
		while (iter.hasNext()) {
			nextFoodNode = iter.next();
			chunk = intToByte(nextFoodNode.getOffset());
			System.arraycopy(chunk,0,results,resultOffset,BYTES_PER_INT);
			resultOffset +=BYTES_PER_INT;
		}
		return results;
	}

	public byte[] getMapPortion(int firstNonMystery, int lastNonMystery) {
		int mapSize = lastNonMystery - firstNonMystery + 1;
		byte[] output = new byte[mapSize];
		System.arraycopy(map,firstNonMystery,output,0,mapSize);
		return output;
	}

	public int getIndexOfFirstNonMystery() {
		String temp = new String(map);
		int a = temp.indexOf("0");
		int b = temp.indexOf("1");
		int indexZero = ( a >= 0) ? a : Integer.MAX_VALUE;
		int indexOne = ( b >= 0) ? b : Integer.MAX_VALUE;
		return (indexZero < indexOne) ? indexZero : indexOne;
	}

	public int getIndexOfLastNonMystery() {
		String temp = new String(map);
		int a = temp.lastIndexOf("0");
		int b = temp.lastIndexOf("1");
		int indexZero = ( a >= 0) ? a : Integer.MIN_VALUE;
		int indexOne = ( b >= 0) ? b : Integer.MIN_VALUE;
		return (indexZero > indexOne) ? indexZero : indexOne;
	}

	public byte[] intToByte(int input) {
		byte[] output = new byte[] { 
			(byte) (input >>> (BYTE_SIZE * 3)),
			(byte) (input >>> (BYTE_SIZE * 2)),
			(byte) (input >>> (BYTE_SIZE)),
			(byte) (input) } ;
		return output;
	}

	public int byteToInt(byte[] input) {
		int output = 0;
		output = 
			(input[0] << (BYTE_SIZE * 3)) +
			((input[1] & 0xFF) << (BYTE_SIZE * 2)) + 
			((input[2] & 0xFF) << (BYTE_SIZE * 1)) + 
			(input[3] & 0xFF)
			;
		return output;
	}

	public void processForeignMap(int firstNonMystery, byte[] foreignMap) {
		int count = 0;
		for(int i=0;i<foreignMap.length;i++) {
			if ((map[firstNonMystery+i] == '?') && (foreignMap[i] != '?')) {
				map[firstNonMystery+i] = foreignMap[i];
				count+=1;
			}
		}
	}

	public void receive(byte[] input){
		//IF NO MESSAGE RECIEVED, DON'T BOTHER DECODING 
		if (input == null) {
			return;
		}
		byte[] chunk = new byte[BYTES_PER_INT];

		//DECODE INPUT BYTES
		System.arraycopy(input,0,chunk,0,BYTES_PER_INT);
		int heading = byteToInt(chunk);
		System.arraycopy(input,4,chunk,0,BYTES_PER_INT);
		int firstNonMystery = byteToInt(chunk);
		System.arraycopy(input,8,chunk,0,BYTES_PER_INT);
		int lastNonMystery = byteToInt(chunk);
		int mapSize = lastNonMystery - firstNonMystery + 1;
		byte[] newMap = new byte[mapSize];
		System.arraycopy(input,12,newMap,0,mapSize);
		byte[] newFoodLocationBytes = new byte[input.length-12-mapSize];
		byte[] foreignFoodNodeCountBytes = new byte[1];
		System.arraycopy(input,12+mapSize,foreignFoodNodeCountBytes,0,1);
		int foreignFoodNodeCount = (int) foreignFoodNodeCountBytes[0];
		byte[] foreignFoodNodeBytes 
			= new byte[foreignFoodNodeCount*BYTES_PER_INT];

		//PROCESS OTHER ANT'S DESTINATION
		otherAntDestinations.add(new Node(heading));

		//PROCESS NEW MAP INFORMATION
		processForeignMap(firstNonMystery,newMap);
		System.arraycopy(input,12+mapSize+1,foreignFoodNodeBytes
			,0,foreignFoodNodeCount*BYTES_PER_INT);
		//PROCESS NEW FOOD COORDINATES
		if (foreignFoodNodeCount > 0) {
			Node nextNode;
			for(int i=0;i<foreignFoodNodeCount;i++) {
				System.arraycopy(foreignFoodNodeBytes,i*BYTES_PER_INT
					,chunk,0,BYTES_PER_INT);
				nextNode = new Node(byteToInt(chunk));
				if (! outOfFoodNodes.contains(nextNode)) {
					foodNodes.add(nextNode);
				}
			}
		}

		otherAntDestinations.add(new Node(heading));
	}
}
