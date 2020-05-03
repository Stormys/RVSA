package vsa;

import soot.Local;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class Sigma {
    public Map<Local, L> map;

    public Sigma() {
        this.map = new HashMap<Local, L>();
    }

    public Sigma(Iterable<Local> locals, state initalState) {
        this.map = new HashMap<Local, L>();

        for (Local l : locals) {
            this.map.put(l, new L(initalState));
        }
    }

    public void copy(Sigma destSet) {
        for (Map.Entry<Local, L> mapEntry : this.map.entrySet()) {
            destSet.map.put(mapEntry.getKey(), new L(mapEntry.getValue()));
        }
    }

    public String toString() {
        Set<Local> keys = map.keySet();
        StringBuilder str = new StringBuilder("[ ");
        for (Local key : keys) {
            str.append(key.getName()).append(": [").append(map.get(key).min)
                .append(", ").append(map.get(key).max).append("]")
                .append("; ");
        }
        return str + " ]";
    }

    @Override
    public int hashCode() {
        return this.map.hashCode();
    }
    
}
