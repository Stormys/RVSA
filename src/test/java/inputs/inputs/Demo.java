package inputs;

public class Demo {

    public static void motivation() {
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
        ignore = array[z]; // Should always be 30
    }

    public static void just_wrong() {
        int[] array = new int[5];
        int a, x, y, z;
        int ignore; 

        x = 10;
        a = 1;

        if (condition()) {
            y = 4;
        } else {
            y = 5;
        }

        z = x + a;
        ignore = array[z]; // Always 11, RVSA should do nothing
    }

    public static void demo_3() {
        int[] array = new int[20];
        int x, y, z, a, b;
        int ignore;

        if (condition()) {
            x = 5;
            y = 5;
        } else {
            x = 2;
            y = 2;
        }

        if (condition()) {
            a = x * 5; // Either (5 * 5) or (2 * 5)
            b = y * 3; // Either (5 * 3) or (2 * 3)
        } else {
            a = x + 5; // Either (5 + 5) or (2 + 5)
            b = y + 3; // Either (5 + 3) or (2 + 3)
        }

        ignore = array[a];

        z = a + b; // Either 40 or 16 or 18 or 12
        ignore = array[z];
    }

    public static boolean condition() {
        return false;
    }
}
