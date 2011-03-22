import java.util.Hashtable;

/*	Author: J. Andrew Key
	Objective: Write a function which is passed two strings and returns true
	if one is a permutation of the other.
	Time Complexity: Since each Hashtable operation is an "amortized" O(1),
	we can treat them as constant operations. Because of the for loop in 
	"bucketFrequencies", the comparison of any two strings is only an 
	O(n) operation, where n is the length of the firstString + secondString.
*/

public class PermutationDiscovery
{

	private boolean showProof;
	private boolean verbose;

	public PermutationDiscovery () {
		showProof = false;
		verbose = false;
	}

	public void setShowProof (boolean showProof) { this.showProof = showProof; }
	public void setVerbose (boolean verbose) { this.verbose = verbose; }

	//Creates a Hashtable of character frequencies for the supplied string
	public Hashtable<Character,Integer> bucketFrequencies(String str) {
		Character c = null;
		int d = 0;
		Hashtable<Character,Integer> result = new Hashtable();
		for(int i = 0; i < str.length(); i ++) {
			c = new Character(str.charAt(i));
			if (! result.containsKey(c)) {
				result.put(c,new Integer(1));
			} else {
				d = result.get(c).intValue() + 1;
				result.put(c, new Integer(d));
			}
		}
		return result;
	}

	//Method to determine if one string is a permutation of another
	public boolean isPermutation(String firstString, String secondString ) {
		boolean answer = false;
		boolean differentLengths = false;
		Hashtable<Character,Integer> firstBucket = new Hashtable();
		Hashtable<Character,Integer> secondBucket = new Hashtable();
		if (firstString.length() != secondString.length()) {
			differentLengths = true;
			answer = false;
		} else {
			firstBucket = bucketFrequencies(firstString);
			secondBucket = bucketFrequencies(secondString);
			if (! firstBucket.equals(secondBucket)) {
				answer = false;
			} else {
				answer = true;
			}
		}
		if (verbose) {
			System.out.println("Permutation? -> " + firstString + " AND " +
				secondString + " -> " + (answer ? "TRUE" : "FALSE"));
			
		}
		if (showProof) {
			System.out.println("PROOF");
			if (differentLengths) {
				System.out.println("Strings are different lengths.\n");
			} else {
				System.out.println(firstBucket.toString());
				System.out.println(secondBucket.toString() + "\n");
			}
		}
		return answer;
	}

	//Main Program
	public static void main (String [] args) {
		System.out.println("PermutationDiscovery");
		PermutationDiscovery pd = new PermutationDiscovery();
		pd.setVerbose(true);
		//Simple Anagarams
		pd.isPermutation("deductions","discounted");
		pd.isPermutation("angered","enraged");
		//Same length, Same characters, different frequencies
		pd.isPermutation("aabbccdd","abbbccdd");
		//Different length, Same characters
		pd.isPermutation("aabbccdd","aabbccdda");
		//Completely dissimilar strings
		pd.isPermutation("abcdef","ghijklmnop");
		//A very long anagram from Shakespeare's Hamlet
		pd.setShowProof(true);
		pd.isPermutation(
			"tobeornottobethatisthequestionwhethertisnoblerinthemindtosuffertheslingsandarrowsofoutrageousfortune",
			"inoneofthebardsbestthoughtoftragediesourinsistentherohamletqueriesontwofrontsabouthowlifeturnsrotten");
	}
}
