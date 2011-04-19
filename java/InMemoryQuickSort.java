/*	Author: J. Andrew Key
	Objective: Produce an in-memory QuickSort that works for any Comparable
	Class.  This allows us to Quicksort any heterogeneous ArrayList of 
	Comparable Objects.
	Time Complexity: O(n log n) (best case) / O(n^2) (worst case)

*/
import java.util.ArrayList;
import java.util.Random;

public class InMemoryQuickSort<E extends Comparable<E>> {

	//instance variables
	private int swaps = 0;
	private boolean verbose = false;
	//This does all of the swapping between list elements
	public void swap(ArrayList<E> array, int index1, int index2) {
		E temp = array.get(index1);
		array.set(index1,array.get(index2));
		array.set(index2,temp);
		swaps+= 1;
	}

	//Constructors
	public InMemoryQuickSort () { }
	public InMemoryQuickSort (boolean verbose) { this.verbose = verbose; }

	//Polymorphic method, this form is the only form we ever call from main
	//It accepts an ArrayList<E> ( E is our "Comparable" class)
	public int quickSort(ArrayList<E> array) {
		swaps = 0;
		quickSort(array,0,array.size()-1);
		if (verbose == true) {
			System.out.println("Swaps: " + (new Integer(swaps)).toString());
		}
		return swaps;
	}

	//Polymorphic method, this is the recursive form that operates on
	//only a portion of the array, between the start and end indices
	public void quickSort(ArrayList<E> array, int start, int end) {
		int i = start;
		int k = end;

		//continue to recurse if there are elements between the start and end
		//index
		if (end - start >= 1) {
			//The choice of pivot can be detrimental for already sorted lists
			//it would turn our O(n log(n)) quicksort into an O(n^2) worst case
			//performance.  A random or median pivot would overcome this. 
			E pivot = array.get(start);
			//k will start to the right of i,
			while (k > i) {
				//move "i" to the right for each element that is LTE the "pivot"
				//but don't move "i" beyond the "end"
				//and don't let "i" go any further than immediately left of "k"
				while ( (array.get(i).compareTo(pivot) <= 0) 
					&& (i <= end) && (k > i)) { i++; }
				//move "k" to the left for each element that is GT the "pivot"
				//but don't move "k" before the "start"
				//and don't let "k" go to the left of "i"
				while ( (array.get(k).compareTo(pivot) > 0)
					&& (k >= start) && (k >= i)) { k--; }
				//now that i and k may have moved
				//if "k" is to the right of "i", swap the elements
				if (k > i) { swap(array, i, k); }
			}
			//swap "start" and "k"
			swap(array, start, k);

			//quickSort from "start" to "left of k" 
			quickSort(array, start, k - 1);
			//quickSort from "right of k" to "end"
			quickSort(array, k + 1, end);
		//return if there is nothing left to sort
		} else { return; }
	}

	public static void main(String [] args) {
		//The first quickSorter will show the number of swaps
		InMemoryQuickSort<String> qsString = new InMemoryQuickSort<String>(true);
		//The second quickSorter will remain silent
		InMemoryQuickSort<Integer> qsInteger = new InMemoryQuickSort<Integer>();

		//Generic in-place quicksort on Strings
		ArrayList<String> x = new ArrayList<String> ();
		x.add("andy"); x.add("key"); x.add("eats"); x.add("carrots");
		x.add("and"); x.add("peas");
		System.out.println(x);
		qsString.quickSort(x);
		System.out.println(x);

		//Generic in-place quicksort on Integers
		ArrayList<Integer> y = new ArrayList<Integer> ();
		y.add(new Integer(1)); y.add(new Integer(5)); y.add(new Integer(3));
		y.add(new Integer(-2));
		System.out.println(y);
		qsInteger.quickSort(y);
		System.out.println(y);
	}
}
