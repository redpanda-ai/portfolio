package test;

import java.util.Arrays;

import test.SpreadSheet.CircularReferenceException;

//un-comment this if you have JUnit and want to use it
//import junit.framework.TestCase;

public class TestSpreadSheet extends TestCase {
    public void assertEqualWithin(double precision, Number[] expected, Number[] actual) {
        if (expected == null) {
            if (actual != null) fail("expected null, actual "+Arrays.toString(actual));
            return;
        }
        if (actual == null) fail("expected "+Arrays.toString(expected)+", actual is null");
        if (expected.length != actual.length) fail("expected "+Arrays.toString(expected)+", actual is "+Arrays.toString(actual)+" (different length)");
        for (int i=0; i<expected.length; i++) {
            if (expected[i] == null) {
                if (actual[i] != null) fail("expected "+Arrays.toString(expected)+", actual is "+Arrays.toString(actual)+" (element "+i+" is not null)");
            } else {
                double diff = Math.abs(expected[i].doubleValue() - actual[i].doubleValue());
                if (diff > precision)
                    fail("expected "+Arrays.toString(expected)+", actual is "+Arrays.toString(actual)+" (element "+i+" is too different)");
            }
        }
    }
    public void testRPN() {
        SpreadSheet sp = new SpreadSheet(3,1,
                "10 4 /",
                "10 1 1 + / 1 +",
                "10 2 3 + / 4 5 + *"
        );
        Double[] result = sp.dump();
        assertEqualWithin(1e-4, new Number[]{
                2.5,
                6,
                18
        }, result);
    }
    public void testVerySimple() {
        SpreadSheet sp = new SpreadSheet(1,2,
                "B1",       "4 5 *"
        );
        Double[] result = sp.dump();
        assertEqualWithin(1e-4, new Number[]{
                20,         20
        }, result);
    }
    public void testSimple() {
        SpreadSheet sp = new SpreadSheet(2,2,
                "B1",       "4 5 *",
                "A1 B2 /",  "2"
        );
        Double[] result = sp.dump();
        assertEqualWithin(1e-4, new Number[]{
                20,         20,
                10,         2
        }, result);
    }
    public void testSimple2() {
        SpreadSheet sp = new SpreadSheet(3,3,
                "2",         "A2",      "A1 5 /",
                "3 3 4 + *", "7",       "B2",    
                "A1 B1 +",   "10 C1 /", "3"
        );
        Double[] result = sp.dump();
        assertEqualWithin(1e-4, new Number[]{
                2,   21,0.4,
                21,   7,  7,
                23,  25,  3
        }, result);
    }
	public void testCircle() {
		SpreadSheet sp = new SpreadSheet(1,5,"A1","2","3","4.0","5");
		try {
			sp.dump();
		} catch (CircularReferenceException e) {
			return;
		}
		fail("should fail for circular reference");
	}

    public void testSimpleCircular() {
        SpreadSheet sp = new SpreadSheet(2,2,
                "A2",       "B2",
                "B1",       "A1"
        );
        try {
            sp.dump();
        } catch (CircularReferenceException e) {
            return; // good
        }
        fail("should fail for circular reference");
    }

    public void testNotCircular() {
        SpreadSheet sp = new SpreadSheet(1,14,
                //               A1    B1    C1    D1    E1    F1    G1    H1    I1    J1    K1    L1    M1    N1
                "N1", "M1", "L1", "K1", "J1", "I1", "H1", "1",  "G1", "F1", "E1", "D1", "C1", "B1"
        );
        Double[] result = sp.dump();
        assertEqualWithin(1e-4, new Number[]{
                1,1,1,1,1,1,1,1,1,1,1,1,1,1
        }, result);
    }
    public void testCircular2() {
        SpreadSheet sp = new SpreadSheet(1,14,
                //               A1    B1    C1    D1    E1    F1    G1      H1      I1    J1    K1    L1    M1    N1  
                "N1", "M1", "L1", "K1", "J1", "I1", "H1", "1 L1 +", "G1", "F1", "E1", "D1", "C1", "B1"
        );
        try {
            sp.dump();
        } catch (CircularReferenceException e) {
            return; // good
        }
        fail("should fail for circular reference");
    }
    public void testCircular3() {
        SpreadSheet sp = new SpreadSheet(1,14,
                //               A1    B1    C1    D1    E1    F1    G1    H1    I1    J1    K1    L1    M1    N1  
                "B1", "C1", "H1", "E1", "F1", "G1", "H1", "I1", "J1", "K1", "L1", "M1", "N1", "D1"
        );
        try {
            sp.dump();
        } catch (CircularReferenceException e) {
            return; // good
        }
        fail("should fail for circular reference");
    }
}
