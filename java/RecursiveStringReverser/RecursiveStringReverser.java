/*	Author: J. Andrew Key
	Objective: Use recursion to write a method that reverses a given string.
	Time Complexity: O(n), where n is the length of the "inputString", since
	n calls the the "reverse" method must be made for a given string.
*/

public class RecursiveStringReverser
{
	//Tail recursive static method that reverses a String 
	public static String reverse(String inputString) {
		if (inputString.length() == 1) {
			return inputString;
		} 
		int l = inputString.length();
		return inputString.substring(l-1,l) 
			+ RecursiveStringReverser.reverse(inputString.substring(0,l-1));
	}

	//Main Program
	public static void main(String [] args) {
		System.out.println(RecursiveStringReverser.reverse("123456"));
	}
}
