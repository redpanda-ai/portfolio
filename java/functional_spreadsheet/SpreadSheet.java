package test;

import java.lang.Math;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Stack;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
/**
 * A spreadsheet consists of a two-dimensional array of cells, labeled
 * A1, B1, ...., A2, B2, ....
 * Each cell contains either a number (its value) or an expression.
 * 
 * <p> For simplicity, expressions are given as RPN (reverse-polish notation).
 * They contain space-separated terms that are either numbers (in our case
 * non-negative values), cell references, and the operators '+', '-', '*', '/'.
 * 
 * <p>Examples:
 *
 *  +----------------------+------------------+--------------+
 *  | RPN                  | infix notation   | evaluates to |
 *  +----------------------+------------------+--------------+
 *  | "10 4 /"             | "10/4"           |          2.5 |  
 *  +----------------------+------------------+--------------+
 *  | "10 1 1 + / 1 +"     | "10/(1+1) + 1    |          6.0 | 
 *  +----------------------+------------------+--------------+
 *  | "10 2 3 + / 4 5 + *" | "10/(2+3)*(4+5)" |         18.0 |
 *  +----------------------+------------------+--------------+
 *
 * <p> Your task is to implement a functional SpreadSheet. The main
 * functionality is:
 * <li> 1. constructor: construct the spreadsheet from the provided expressions.
 * <li> 2. dump(): solve the spreadsheet and return the result. If
 * a cycle of references exists, detect it and throw a
 * CircularReferenceException.
 * 
 * <p>
 * Comments: You can assume that there are no more than 26 columns (A-Z) in the spreadsheet.
*/
public class SpreadSheet {
	/**
	 * Construct a nRows x nCols SpreadSheet, with cells containing
	 * the expressions passed in the exprArray.
	 * 
	 * <p> The expressions passed in the exprArray String array are in row
	 * by row order, i.e.:

	 * +----+----+----+
	 * | A1 | B1 | C1 |
	 * +----+----+----+
	 * | A2 | B2 | C2 |
	 * +----+----+----+
	 *
	 * etc.
	 * @param nRows
	 * @param nCols
	 * @param exprArray
	 */

	//Regular expressions used to identify tokens
	Pattern numeric = Pattern.compile("[0-9]+.?[0-9]*");
	Pattern oper = Pattern.compile("[+-/*^]");
	Pattern cell = Pattern.compile("[A-Z]+[0-9]+");

	//These lists keep track of our solve progress and possible circuits
	Hashtable solvedCells, unsolvedCells = null;
	//Hashtable unsolvedCells = null;
	Hashtable circuit = new Hashtable();

	//Our spreadsheet answer
	Double[] answer = null;
	int nRows, nCols = 0;
	//int nCols = 0;
	boolean circuitDetected = false;
	
	public SpreadSheet(int nRows, int nCols, String... exprArray) {
		this.nRows = nRows;
		this.nCols = nCols;
		solvedCells = new Hashtable( nRows * nCols );
		unsolvedCells = new Hashtable( nRows * nCols );
		for( int i = 0; i < nRows * nCols; i++ ) {
			unsolvedCells.put(Integer.toString(i),exprArray[i]);
		}
		Enumeration unsolvedKeys = unsolvedCells.keys();
		String key = null;
		while (unsolvedKeys.hasMoreElements() && (circuitDetected == false)) {
			key = (String) unsolvedKeys.nextElement();	
			if (unsolvedCells.containsKey(key)) {
				solvedCells.put(key,solveCell(key));
				if (circuitDetected) { return; }
				unsolvedCells.remove(key);
				circuit.clear();
			}
		}
	}
	private String solveCell (String index) {
		String result = null; 
		if (solvedCells.containsKey(index)) {
			result = solvedCells.get(index).toString();
		} else {
			result = solveRPN((String) unsolvedCells.get(index),index).toString();
			solvedCells.put(index,result);
			unsolvedCells.remove(index);
		}
		return result;		
	}

	private void applyOperator (String op, Stack<Double> operandStack) {
		double a = operandStack.pop().doubleValue();
		double b = operandStack.pop().doubleValue();
		double result;
		
		if (op.equals("+")) { result = b + a; }
		else if (op.equals("-")) { result = b - a; }
		else if (op.equals("/")) { result = b / a; }
		else { result = b * a; }
		operandStack.push(new Double(result));
	}
	private String getSpreadsheetIndex(String cellNotation) {
		int minor = cellNotation.toUpperCase().charAt(0) - 65;
		int major = Integer.parseInt(cellNotation.substring(1)) - 1;
		return Integer.toString( nCols * major + minor );
	}
	private Double solveRPN(String expr, String cellKey) {
		String[] tokens = expr.split(" ");
		Stack<Double> operandStack = new Stack();
		String index = null;
		if (circuit.containsValue(cellKey)) {
			circuitDetected = true;
			return 0.0;
		}
		circuit.put(cellKey,cellKey);
		String token = null;
		for (int i = 0; i < tokens.length ; i++ ) {
			token = tokens[i];	
			if (numeric.matcher(token).matches()) {
					operandStack.push(Double.valueOf(tokens[i]));
					continue;
			}
			if (oper.matcher(token).matches()) {
					applyOperator(tokens[i],operandStack);
					continue;
			}
			if (cell.matcher(token).matches()) {
					index = getSpreadsheetIndex(tokens[i]);	
					operandStack.push(Double.valueOf(solveCell(index)));
					continue;
			}
		}
		if (operandStack.size() == 1) {
			return operandStack.pop();
		}
		return null;
	}
	/**
	 * @return the values from a "solved" SpreadSheet
	 */
	public Double[] dump() throws CircularReferenceException {
		Double[] answer = null;
		if (circuitDetected) {
			throw new CircularReferenceException ("Circular Reference");
		} else {
			answer = new Double[ nRows * nCols ];
			for (int j = 0; j < nRows * nCols; j++ ) {
				answer[j] = Double.parseDouble(solvedCells.get(Integer.toString(j))
				.toString());
			}
		}
		return answer;
	}
	public class CircularReferenceException extends RuntimeException {
	    private static final long serialVersionUID = 1L;
	    public CircularReferenceException(String msg) {
	        super(msg);
		}
	}
}
