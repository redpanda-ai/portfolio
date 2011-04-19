/*	Author: J. Andrew Key
	Objective: Produce and in-memory quicksort that works for any "Comparable"
	class.  This allows us to quicksort any heterogeneous ArrayList of
	Comparable Objects.
	Time Complexity: O(n log (n))
	Features: A random pivot, to prevent worst case scenario of an already
	sorted list.
*/

import java.util.ArrayList;
import java.util.Random;

public class InPlaceQuickSort<E extends Comparable<E>> {

	//instance variables
	//This does all of the swapping between list elements
	public void swap(ArrayList<E> array, int index1, int index2) {
		E temp = array.get(index1);
		array.set(index1,array.get(index2));
		array.set(index2,temp);
	}

	public int partition(ArrayList<E> array, int left, int right
	, int pivotIndex) {
		E pivotValue = array.get(pivotIndex);
		swap(array,pivotIndex,right);
		int storeIndex = left;
		for (int i = left;i<right;i++) {
			if (array.get(i).compareTo(pivotValue) <= 0) {
				swap(array,i,storeIndex);
				storeIndex +=1;
			}
		}
		swap(array,storeIndex,right);
		return storeIndex;
	}

	public void quickSort(ArrayList<E> array, int left, int right) {
		if (right > left) {
			//I am using a random pivot to prevent a quicksort of an already
			//sorted list (which is common) from degrading our quickSort to 
			//O(n^2)
			Random r = new Random();
			int pivotIndex = left + r.nextInt(right - left);
			int pivotNewIndex = partition(array, left, right, pivotIndex);
			quickSort(array, left, pivotNewIndex - 1);
			quickSort(array, pivotNewIndex + 1, right);
		}
	}

	public static void main(String [] args) {
		InPlaceQuickSort<String> qsString = new InPlaceQuickSort<String>();
		//Generic in-place quicksort on Strings
		ArrayList<String> x = new ArrayList<String> ();
		x.add("andy"); x.add("key"); x.add("eats"); x.add("carrots");
		x.add("and"); x.add("peas");
		System.out.println(x);
		qsString.quickSort(x,0,x.size()-1);
		System.out.println(x);

		//Generic in-place quicksort on Integers
		InPlaceQuickSort<Integer> qsInteger = new InPlaceQuickSort<Integer>();
		ArrayList<Integer> y = new ArrayList<Integer> ();
		y.add(new Integer(1)); y.add(new Integer(5)); y.add(new Integer(3));
		y.add(new Integer(-2));
		System.out.println(y);
		qsInteger.quickSort(y,0,y.size()-1);
		System.out.println(y);
	}

}
