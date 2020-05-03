package inputs;

public class Test1 {

    public static void test2() {
        int[] array = new int[31];
        int x, y, z;
        int ignore;

        if (condition()) {
            x = 10;
            y = 20;
        } else {
            x = 15;
            y = 15;
        }

        z = x + y;
        ignore = array[z];
    }

    public static void test1() {
        int x, y, z, w;
        int[] array = new int[5];
        int ignore;

        y = -5;
        z = -3;
        y = y * y;
        z = z + z;

        ignore = array[y]; 
        ignore = array[z]; 
    }
    
    public static boolean condition() {
        return false;
    }
}
