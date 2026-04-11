package com.baljeet.api.Chess.Engine;

public class TranspositionTable {

    private final TTEntry[] tt;
    private final int tableSize;

    public record TTEntry(
            long zobristKey,
            int bestMove,
            int depth,
            int score,
            byte boundType,
            int age
    ) {}

    TranspositionTable(int tableSize) {
        this.tableSize = tableSize;
        tt = new TTEntry[tableSize];
    }

    public void store(long zobristKey, int move, int score, int depth, byte bound, int age) {
        int index = getTTIndex(zobristKey);
        TTEntry entry = tt[index];

        // deeper searches and old ones are overwritten
        if (bound == Search.EXACT ||
                entry == null ||
                entry.zobristKey() != zobristKey ||
                entry.depth() < depth ||
                entry.age() - age > 8) {
            tt[index] = new TTEntry(zobristKey, move, depth, score, bound, age);
        }
    }

    public TTEntry lookup(long zobristKey) {
        TTEntry entry = tt[getTTIndex(zobristKey)];

        if (entry == null)
            return null;
        else if (entry.zobristKey() == zobristKey)
            return entry;

        return null;
    }

    private int getTTIndex(long zobristKey){
        return (int) (zobristKey & (tableSize - 1));
    }
}