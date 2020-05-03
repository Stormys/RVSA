package vsa;

enum state {
    Top, Bot
}

public class L {
    public int min;
    public int max;

    public L(L copy) {
        this.min = copy.min;
        this.max = copy.max;
    }

    public L(state abs) {
        if (abs == state.Top) {
            min = Integer.MIN_VALUE;
            max = Integer.MAX_VALUE;
        } else {
            min = Integer.MAX_VALUE;
            max = Integer.MIN_VALUE;
        }
    }

    public L(int min, int max) {
        this.min = min;
        this.max = max;
    }
}
