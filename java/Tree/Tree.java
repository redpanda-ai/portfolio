public class Tree 
{
	private int nodes;
	private int numlevels;

/*	Author: J. Andrew Key
	Objective: Write a "Tree" class that creates a "ragged" 2D array of "nodes"
	that can be visualized as tree shaped triangle.  The number of nodes for
	a Tree of n levels would be: Tree(n) = n (n+1) / 2.  This program creates
	a tree and returns references to nodes in the tree via the "index" method.
	Time Complexity:  The "index" and "update" methods are O(1) or constant,
	but allocating the tree nodes is O(n(n+1)/2).
*/

	public String[][] data = null;

	//Constructor
	Tree(int numlevels) {
		this.numlevels = numlevels;
		data = new String[numlevels][];
		for(int i = 0; i< numlevels;i++) {
			data[i] = new String[i+1];
			for(int j=0; j<data[i].length;j++) {
				//Initialize each new element to "X"
				data[i][j] = "X";
			}
		}
	}

	//Method to create a String representation of the associative array
	public String toString() {
		StringBuffer cols = new StringBuffer(numlevels * 2 + 1);
		cols.append("  ");
		StringBuffer sb = 
			new StringBuffer(this.numlevels * (this.numlevels + 1));
		for(int i = 0; i < this.numlevels; i++) {
			cols.append(Integer.toString(i+1) + " ");
			sb.append(Integer.toString(i+1) + " ");
			for(int j = 0; j < data[i].length; j++) {
				sb.append(data[i][j] + " ");
			}
		sb.append("\n");
		}
		cols.append("\n");
		return cols.toString() + sb.toString();
	}

	//Returns a reference to an element in our "ragged" 2D array of data
	public String index(int level, int numdown) {
		return data[level-1][numdown-1];
	}

	//Sets the value of an element in our "ragged" 2D array of data
	public void update(int level, int numdown, String val) {
		data[level-1][numdown-1] = val;
	}

	//Main Program
	public static void main(String [] args) {
		Tree t = new Tree(5);
		System.out.println(t);
		t.update(4,2,"Y");
		System.out.println("Tree(4,2) -> " + t.index(4,2));
		t.update(5,3,"Z");
		System.out.println("Tree(5,3) -> " + t.index(5,3));
		System.out.println(t);
	}
	
}
