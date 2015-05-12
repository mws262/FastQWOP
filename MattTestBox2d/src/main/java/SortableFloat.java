
/**
 * Basic wrapper for a float so that duplicate values can be stored in a map without overwriting.
 * Implemented Comparable so that MyDouble could be sorted.
 */
public class SortableFloat implements Comparable<SortableFloat> {
    private Float f;
    public SortableFloat(Float v) {
        f = v;
    }
    public Float getValue() {
        return f;
    }
    public int compareTo(SortableFloat o) {
        if (getValue() > o.getValue()) return 1;
        if (getValue() < o.getValue()) return -1;
        return 0;
    }
}