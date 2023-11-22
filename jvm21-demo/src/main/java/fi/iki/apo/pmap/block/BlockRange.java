package fi.iki.apo.pmap.block;

public record BlockRange(int min, int max) {
    public int size() {
        return max-min+1;
    }
}
