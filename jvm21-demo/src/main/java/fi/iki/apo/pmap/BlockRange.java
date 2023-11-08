package fi.iki.apo.pmap;

import java.util.ArrayList;
import java.util.List;

public record BlockRange(int min, int max) {
    static public List<BlockRange> splitByBlockCount(int size, int blockCount) {
        int blockSize = size / blockCount;
        int correctedBlockSize = blockSize > 0 ? blockSize : 1;
        return splitByBlockSize(size, correctedBlockSize);
    }

    static public ArrayList<BlockRange> splitByBlockSize(final int size, final int blockSize) {
        if (size == 0) {
            return new ArrayList<>();
        }
        if (blockSize <= 0 || size < 0) {
            throw new RuntimeException("Bad blockSize: " + blockSize + " or " + size);
        }
        final int lastBlockSize = size % blockSize;
        final int completeBlocksLastIndex = size - lastBlockSize - 1;
        final int lastBlockCapacity = lastBlockSize > 0 ? 1 : 0;
        final var tasks = new ArrayList<BlockRange>(size / blockSize + lastBlockCapacity);
        var lowerLimit = 0;
        while (lowerLimit <= completeBlocksLastIndex) {
            int upperLimit = lowerLimit + blockSize - 1;
            tasks.add(new BlockRange(lowerLimit, upperLimit));
            lowerLimit += blockSize;
        }
        if (lastBlockSize > 0) {
            tasks.add(new BlockRange(size - lastBlockSize, size - 1));
        }
        return tasks;
    }
}
