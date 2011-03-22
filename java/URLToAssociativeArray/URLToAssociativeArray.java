import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;

/*	Author: J. Andrew Key
	Objective: Write a function which decodes a URL-encoded string into an
	associative array (Hashtable).  If there are two parameters with the same
	name, stack the value as an array.
	For example, the string "a=1&b=2&a=hello&apple=9&apple=digital" would be 
	converted to: 
	
	associative array( 'a' => array(1,'hello'), 'b'=> 2, 'apple' => array(9,'digital') ) 

	Time Complexity: Since each Hashtable operation is an "amortized" O(1),
	we can treat them as constant operations.  The for loop in "process" causes
	this program to run in O(n) time.
*/

public class URLToAssociativeArray
{

	private Hashtable<String, Object> data;

	//Constructor
	public URLToAssociativeArray(String urlString) {
		data = new Hashtable<String, Object>();
		this.process(urlString);
	}

	//Method to create a String representation of the associative array
	public String toString() {
		StringBuffer sb = new StringBuffer();
		Enumeration<String> e = data.keys();
		String key = null;
		while(e.hasMoreElements()){
			key = e.nextElement();
			sb.append(key + " => " + data.get(key) + "\n");
		}
		return sb.toString();
	}

	//Converts a URL-encoded string into an associative array
	public void process(String urlString) {
		//Each parameter is seperated by a "&"
		String[] params = urlString.split("&");
		for(int i = 0; i < params.length; i++){
			try {
				//Each key is sepearted from its value by a "="
				String[] keyval = params[i].split("=");
				//URL-decode our key/value pairs 
				keyval[0] = URLDecoder.decode(keyval[0],"UTF-8");
				keyval[1] = URLDecoder.decode(keyval[1],"UTF-8");
				//Add keys that are new to the associative array as Strings
				if (! data.containsKey(keyval[0])) {
					data.put(keyval[0],keyval[1]);
				} else {
					//If the value is a String, replace it with an ArrayList 
					//containing the String
					if (data.get(keyval[0]) instanceof String) {
						String temp = (String) data.get(keyval[0]);
						ArrayList<String> newArray = new ArrayList();
						newArray.add(temp);
						data.put(keyval[0],newArray);
					//and add the new value to the ArrayList
					((ArrayList) data.get(keyval[0])).add(keyval[1]);
					}
				}
			} catch (Exception e) {
				System.out.println("foo");
			}
		}
	}

	//Main Program
	public static void main(String [] args) {
		URLToAssociativeArray y = 
			new URLToAssociativeArray("a=1&b=2&a=hello&apple=9&apple=digital");
		System.out.println(y);
	}
}
