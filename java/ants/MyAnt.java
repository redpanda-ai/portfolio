import ants.*;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.Hashtable;

public class MyAnt implements Ant{

	public enum LittleDirection { L, N, E, S, W; }
	private byte[] map = new byte[1225];
	private int[] path = { 0, 0, 0, 0, 0 };
	private int[] food = { 0, 0, 0, 0, 0 };
	private int[] ants = { 0, 0, 0, 0, 0 };
	public int myLocation = 612;
	private int[] isTravelable = new int[5];
	private Direction chosenDirection;
	private Hashtable<Integer,Direction> lookup;
	private Hashtable<Direction,Integer> reverseLookup;

	public MyAnt() {
		for(int i =0;i < 35*35;i++) {
				map[i] = '?';
		}
		map[myLocation] = 'H';
		chosenDirection = Direction.WEST;
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
	}

	public Action getAction(Surroundings surroundings){
		scan(surroundings);
		updateMap();
		showAntVision();
		selectDirection();
		showMap();
		this.myLocation += getOffset(chosenDirection);
		System.out.println("Location is " + (new Integer(myLocation)).toString());
		return Action.move(chosenDirection);
	}

	private int getOffset(Direction direction) {
		int offset = 0;
		switch(direction) {
			case NORTH: offset = -35; break;
			case WEST: offset = -1; break;
			case EAST: offset = 1; break;
			case SOUTH: offset = 35; break;
			default : offset = 0; break;
		}
		System.out.println("Offsetting " + (new Integer(offset)).toString());
		return offset;
	}

	private void updateMap(Direction direction) {
		int newOffset = myLocation + getOffset(direction);
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

	public ArrayList<Direction> getPossibleDirections() {
		ArrayList<Direction> result = new ArrayList<Direction>();
		int i = reverseLookup.get(chosenDirection).intValue() + 1;
		for(;i<5;i++) {
			if (path[i] != 0) {
				result.add(lookup.get((new Integer(i))));
			}
		}
		for(int j = 1;j<i;j++) {
			if (path[j] != 0) {
				result.add(lookup.get((new Integer(j))));
			}
		}
		return result;
	}

	public void selectDirection() {
		if (path[reverseLookup.get(chosenDirection)] == 0) {
			chosenDirection = getPossibleDirections().get(0);
		System.out.println("New direction -> " + chosenDirection);
		}
		return;
	}
	public void showMap() {
		System.out.println("map");
		byte[] temp = new byte[35];
		for(int i = 0;i<35;i++) {
			System.arraycopy(map,i*35,temp,0,35);
			System.out.println(new String(temp));
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
	}

	public void showAntVision() {
		String t = "";
		for(int k = 0;k<path.length;k++) {
			t += " " + (new Integer(path[k]).toString());
		} 
		System.out.println("Path: " + t);

		String f = "";
		for(int i = 0;i<food.length;i++) {
			f += " " + (new Integer(food[i]).toString());
		} 
		System.out.println("Food: " + f);

		String a = "";
		for(int j = 0;j<ants.length;j++) {
			a += " " + (new Integer(ants[j]).toString());
		} 
		System.out.println("Ants: " + a);
	}

	public byte[] send(){
		return null;
	}
	
	public void receive(byte[] data){
		//Do nothing
	}

}
