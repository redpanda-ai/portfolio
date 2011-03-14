package test;

import java.lang.reflect.Method;

/**
 * poor-man's JUnit test...  
 * you don't need to use this if you have junit installed
 */
public class TestCase {
    public static class TestFailException extends RuntimeException {
        private static final long serialVersionUID = 1L;
        public TestFailException(String msg) {
            super(msg);
        }
    }
    protected void fail(String string) {
        throw new TestFailException(string);
    }
    protected void assertTrue(boolean value) {
        if (!value) fail("");
    }
    public static void main(String args[]) {
        try {
            int ok=0;
            int err=0;
            TestSpreadSheet ts = new TestSpreadSheet();
            Method m[] = ts.getClass().getDeclaredMethods();
            for (int i = 0; i < m.length; i++) {
                if (m[i].toString().matches("^.*\\.test[^.]+$")) {
                    System.out.println("running: "+m[i].toString());
                    try {
                        m[i].invoke(ts);
                        System.out.println("       ok");
                        ok++;
                    } catch (Exception e) {
                        Throwable cause = e.getCause();
                        if (cause != null) {
                            if (cause instanceof TestFailException) {
                                System.out.println("failed: "+cause.getMessage());
                            } else {
                                System.out.println("caught: "+cause);
                            }
                        } else {
                            System.out.println("caught: "+e);
                        }
                        err++;
                    }
                }
            }
            System.out.format("%-3d tests passed\n%-3d tests failed\n", ok, err);
        }
        catch (Throwable e) {
            System.err.println(e);
        }
    }
}
