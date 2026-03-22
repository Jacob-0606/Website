package com.baljeet.api.Chess.Core;

import org.springframework.stereotype.Component;

@Component
public class PrecomputedData {

    public static long[] knightMoves = new long[64];
    public static long[] whitePawnAttacks = new long[64];
    public static long[] blackPawnAttacks = new long[64];
    public static long[] kingMoves = new long[64];

    //sliding Pieces (edge squares are excluded):
    public static long[] rookMoves = new long[64];
    public static long[] bishopMoves = new long[64];

    //Magics from https://github.com/SebLague/Chess-Coding-Adventure/blob/Chess-V2-UCI/Chess-Coding-Adventure/src/Core/Move%20Generation/Magics/PrecomputedMagics.cs
    //https://github.com/Luecx/Koivisto/blob/master/src_files/attacks.h#L26
    // and https://github.com/milostatarevic/xiphos/blob/master/src/magic.h#L24 for some reason only a few from each worked
    public static final int[] rookShifts =
            {52, 53, 53, 53, 53, 53, 53, 52,
            53, 54, 54, 54, 54, 54, 54, 53,
            53, 54, 54, 54, 54, 54, 54, 53,
            53, 54, 54, 54, 54, 54, 54, 53,
            53, 54, 54, 54, 54, 54, 54, 53,
            53, 54, 54, 54, 54, 54, 54, 53,
            53, 54, 54, 54, 54, 54, 54, 53,
            52, 53, 53, 53, 53, 53, 53, 52};
    public static final int[] bishopShifts =
            {58, 59, 59, 59, 59, 59, 59, 58,
            59, 59, 59, 59, 59, 59, 59, 59,
            59, 59, 57, 57, 57, 57, 59, 59,
            59, 59, 57, 55, 55, 57, 59, 59,
            59, 59, 57, 55, 55, 57, 59, 59,
            59, 59, 57, 57, 57, 57, 59, 59,
            59, 59, 59, 59, 59, 59, 59, 59,
            58, 59, 59, 59, 59, 59, 59, 58};

    public static final long[] rookMagics = {  0x0080001020400080L, 0x0040001000200040L, 0x0080081000200080L, 0x0080040800100080L,
            0x0080020400080080L, 0x0080010200040080L, 0x0080008001000200L, 0x0080002040800100L,
            0x0000800020400080L, 0x0000400020005000L, 0x0000801000200080L, 0x0000800800100080L,
            0x0000800400080080L, 0x0000800200040080L, 0x0000800100020080L, 0x0000800040800100L,
            0x0000208000400080L, 0x0000404000201000L, 0x0000808010002000L, 0x0000808008001000L,
            0x0000808004000800L, 0x0000808002000400L, 0x0000010100020004L, 0x0000020000408104L,
            0x0000208080004000L, 0x0000200040005000L, 0x0000100080200080L, 0x0000080080100080L,
            0x0000040080080080L, 0x0000020080040080L, 0x0000010080800200L, 0x0000800080004100L,
            0x0000204000800080L, 0x0000200040401000L, 0x0000100080802000L, 0x0000080080801000L,
            0x0000040080800800L, 0x0000020080800400L, 0x0000020001010004L, 0x0000800040800100L,
            0x0000204000808000L, 0x0000200040008080L, 0x0000100020008080L, 0x0000080010008080L,
            0x0000040008008080L, 0x0000020004008080L, 0x0000010002008080L, 0x0000004081020004L,
            0x0000204000800080L, 0x0000200040008080L, 0x0000100020008080L, 0x0000080010008080L,
            0x0000040008008080L, 0x0000020004008080L, 0x0000800100020080L, 0x0000800041000080L,
            0x2000804026001102L, 0x2000804026001102L, 0x0000104008820022L, 0x0000040810002101L,
            0x0001000204080011L, 0x0001000204000801L, 0x0001000082000401L, 4734295326018586370L   };
    public static final long[] bishopMagics = {  0x0002020202020200L, 0x0002020202020000L, 0x0004010202000000L, 0x0004040080000000L,
            0x0001104000000000L, 0x0000821040000000L, 0x0000410410400000L, 0x0000104104104000L,
            0x0000040404040400L, 0x0000020202020200L, 0x0000040102020000L, 0x0000040400800000L,
            0x0000011040000000L, 0x0000008210400000L, 0x0000004104104000L, 0x0000002082082000L,
            0x0004000808080800L, 0x0002000404040400L, 0x0001000202020200L, 0x0000800802004000L,
            0x0000800400A00000L, 0x0000200100884000L, 0x0000400082082000L, 0x0000200041041000L,
            0x0002080010101000L, 0x0001040008080800L, 0x0000208004010400L, 0x0000404004010200L,
            0x0000840000802000L, 0x0000404002011000L, 0x0000808001041000L, 0x0000404000820800L,
            0x0001041000202000L, 0x0000820800101000L, 0x0000104400080800L, 0x0000020080080080L,
            0x0000404040040100L, 0x0000808100020100L, 0x0001010100020800L, 0x0000808080010400L,
            0x0000820820004000L, 0x0000410410002000L, 0x0000082088001000L, 0x0000002011000800L,
            0x0000080100400400L, 0x0001010101000200L, 0x0002020202000400L, 0x0001010101000200L,
            0x0000410410400000L, 0x0000208208200000L, 0x0000002084100000L, 0x0000000020880000L,
            0x0000001002020000L, 0x0000040408020000L, 0x0004040404040000L, 0x0002020202020000L,
            0x0000104104104000L, 0x0000002082082000L, 0x0000000020841000L, 0x0000000000208800L,
            0x0000000010020200L, 0x0000000404080200L, 0x0000040404040400L, 0x0002020202020200L };

    public static long[][] rookAttacks = new long[64][];
    public static long[][] bishopAttacks = new long[64][];
    public static final int[] kingCastlingPositions = {1, 5, 57, 61};
    public static final int[] rookCastlingPositions = {2, 4, 58, 60};

    //a and b need to be on the same file, rank or diagonal
    public static long[][] rayBetween = new long[64][64];
    public static long[][] rayMask = new long[64][64];

    public PrecomputedData(){
        generateKingMoves();
        generateKnightMoves();
        generateBlackPawnAttacks();
        generateWhitePawnAttacks();
        generateRookMoves();
        generateBishopMoves();
        generateBishopAttackMaps();
        generateRookAttackMaps();
        generateRayBetween();
        generateRayMask();
    }

    public static long getRookAttacks(int square, long blockers) {
        int index = (int) (((rookMoves[square] & blockers) * rookMagics[square]) >>> rookShifts[square]);
        return rookAttacks[square][index];
    }

    public static long getBishopAttacks(int square, long blockers) {
        int index = (int) (((bishopMoves[square] & blockers) * bishopMagics[square]) >>> bishopShifts[square]);
        return bishopAttacks[square][index];
    }

    public static long xrayRookAttacks(long occ, long blockers, int rookSq) {
        long attacks = getRookAttacks(rookSq, occ);
        blockers &= attacks;
        return attacks ^ getRookAttacks(rookSq, occ ^ blockers);
    }

    public static long xrayBishopAttacks(long occ, long blockers, int bishopSq) {
        long attacks = getBishopAttacks(bishopSq, occ);
        blockers &= attacks;
        return attacks ^ getBishopAttacks(bishopSq, occ ^ blockers);
    }

    private void generateKnightMoves() {
        for (int square = 0; square < 64; square++) {
            int rank = square / 8;
            int file = square % 8;
            long attacks = 0L;

            int[] dr = {-2, -1, +1, +2, +2, +1, -1, -2};
            int[] df = {+1, +2, +2, +1, -1, -2, -2, -1};

            for (int i = 0; i < 8; i++) {
                int r = rank + dr[i];
                int f = file + df[i];

                if (r >= 0 && r < 8 && f >= 0 && f < 8) {
                    int target = r * 8 + f;
                    attacks |= (1L << target);
                }
            }

            knightMoves[square] = attacks;
        }
    }

    private void generateWhitePawnAttacks() {
        for (int i = 0; i < 64; i++) {
            long bit = 1L << i;
            long leftAttack = (bit << 9) & 0xfefefefefefefefeL;
            long rightAttack = (bit << 7) & 0x7f7f7f7f7f7f7f7fL;

            whitePawnAttacks[i] = leftAttack | rightAttack;

        }
    }

    private void generateBlackPawnAttacks() {
        for (int i = 0; i < 64; i++) {
            long bit = 1L << i;
            long leftAttack = (bit >>> 7) & 0xfefefefefefefefeL;
            long rightAttack = (bit >>> 9) & 0x7f7f7f7f7f7f7f7fL;

            blackPawnAttacks[i] = leftAttack | rightAttack;

        }
    }
    //exclude last square before edge
    private void generateRookMoves() {
        for (int i = 0; i < 64; i++) {
            long mask = 0L;
            int rank = i / 8;
            int file = i % 8;

            // Horizontal (rank)
            for (int f = file + 1; f < 7; f++) mask |= 1L << (rank * 8 + f);
            for (int f = file - 1; f > 0; f--) mask |= 1L << (rank * 8 + f);

            // Vertical (file)
            for (int r = rank + 1; r < 7; r++) mask |= 1L << (r * 8 + file);
            for (int r = rank - 1; r > 0; r--) mask |= 1L << (r * 8 + file);

            rookMoves[i] = mask;
        }
    }
    //exclude last square before edge
    private void generateBishopMoves() {
        for (int i = 0; i < 64; i++) {
            long mask = 0L;
            int rank = i / 8;
            int file = i % 8;

            for (int j = 1; j < Math.min(7 - rank, 7 - file); j++) mask |= 1L << (i + 9 * j);
            for (int j = 1; j < Math.min(rank, file); j++) mask |= 1L << (i - 9 * j);

            for (int j = 1; j < Math.min(rank, 7 - file); j++) mask |= 1L << (i - 7 * j);
            for (int j = 1; j < Math.min(7 - rank, file); j++) mask |= 1L << (i + 7 * j);

            bishopMoves[i] = mask;

        }
    }

    private void generateKingMoves() {
        for (int i = 0; i < 64; i++) {
            long mask = 0L;
            int rank = i / 8;
            int file = i % 8;

            for (int x = -1; x <= 1; x++) {
                for (int y = -1; y <= 1; y++) {
                    if (x == 0 && y == 0) continue;

                    int positionX = file + x;
                    int positionY = rank + y;

                    if (positionX >= 0 && positionX <= 7 && positionY >= 0 && positionY <= 7) {
                        mask |= (1L << (positionX + 8 * positionY));
                    }
                }
            }
            kingMoves[i] = mask;
        }
    }

    private void generateRookAttackMaps() {
        for (int i = 0; i < 64; i++) {

            long[] blockerConfigs = getBlockerConfigs(i, true);
            long[] attacks = new long[blockerConfigs.length];

            for (long blocker : blockerConfigs) {

                int index = (int) ((blocker * rookMagics[i]) >>> rookShifts[i]);
                attacks[index] = getAttacks(i, blocker, true);
            }
            rookAttacks[i] = attacks;
        }
    }

    private void generateBishopAttackMaps() {
        for (int i = 0; i < 64; i++) {

            long[] blockerConfigs = getBlockerConfigs(i, false);
            long[] attacks = new long[blockerConfigs.length];

            for (long blocker : blockerConfigs) {
                int index = (int) ((blocker * bishopMagics[i]) >>> bishopShifts[i]);
                attacks[index] = getAttacks(i, blocker, false);

            }
            bishopAttacks[i] = attacks;
        }
    }

    private long[] getBlockerConfigs(int square, boolean orthogonal) {

        long mask = orthogonal ? rookMoves[square] : bishopMoves[square];
        int relevantBits = Long.bitCount(mask);
        long[] blockers = new long[1 << relevantBits];

        for (int i = 0; i < (1 << relevantBits); i++) {

            long blocker = 0L;
            long tempMask = mask;

            for (int j = 0; j < relevantBits; j++) {
                int relevantSquare = Long.numberOfTrailingZeros(tempMask);
                tempMask &= tempMask - 1;

                if (((i >>> j) & 1) == 1) {
                    blocker |= 1L << relevantSquare;
                }
            }
            blockers[i] = blocker;
        }
        return blockers;

    }

    private long getAttacks(int square, long blockers, boolean orthogonal) {
        long mask = 0L;
        int rank = square / 8;
        int file = square % 8;

        if (orthogonal) {
            // Horizontal (rank)
            for (int f = file + 1; f <= 7; f++) {
                mask |= 1L << (rank * 8 + f);
                if (((1L << (rank * 8 + f)) & blockers) != 0) {
                    break;
                }
            }
            for (int f = file - 1; f >= 0; f--) {
                mask |= 1L << (rank * 8 + f);
                if (((1L << (rank * 8 + f)) & blockers) != 0) {
                    break;
                }
            }

            // Vertical (file)
            for (int r = rank + 1; r <= 7; r++) {
                mask |= 1L << (r * 8 + file);
                if (((1L << (r * 8 + file)) & blockers) != 0) {
                    break;
                }
            }
            for (int r = rank - 1; r >= 0; r--) {
                mask |= 1L << (r * 8 + file);
                if (((1L << (r * 8 + file)) & blockers) != 0) {
                    break;
                }
            }
        } else {
            for (int j = 1; j <= Math.min(7 - rank, 7 - file); j++) {
                mask |= 1L << (square + 9 * j);
                if (((1L << (square + 9 * j)) & blockers) != 0) {
                    break;
                }
            }
            for (int j = 1; j <= Math.min(rank, file); j++) {
                mask |= 1L << (square - 9 * j);
                if (((1L << (square - 9 * j)) & blockers) != 0) {
                    break;
                }
            }

            for (int j = 1; j <= Math.min(rank, 7 - file); j++) {
                mask |= 1L << (square - 7 * j);
                if (((1L << (square - 7 * j)) & blockers) != 0) {
                    break;
                }
            }
            for (int j = 1; j <= Math.min(7 - rank, file); j++) {
                mask |= 1L << (square + 7 * j);
                if (((1L << (square + 7 * j)) & blockers) != 0) {
                    break;
                }
            }
        }
        return mask;
    }
    private void generateRayBetween(){
        for (int sq1 = 0; sq1 < 64; sq1++) {
            for (int sq2 = 0; sq2 < 64; sq2++) {

                int fileDiff = sq2 % 8 - sq1 % 8;
                int rankDiff = sq2 / 8 - sq1 / 8;
                long ray = 0L;

                if (fileDiff == 0 || rankDiff == 0) {
                    int fileStep = 0;
                    if (fileDiff > 0) fileStep = 1;
                    else if (fileDiff < 0) fileStep = -1;

                    int rankStep = 0;
                    if (rankDiff > 0) rankStep = 1;
                    else if (rankDiff < 0) rankStep = -1;

                    int currentSq = sq1 + 8 * rankStep + fileStep;
                    while (currentSq != sq2) {
                        ray |= (1L << currentSq);
                        currentSq += 8 * rankStep + fileStep;
                    }

                } else if (Math.abs(fileDiff) == Math.abs(rankDiff)) {

                    int rankStep = (rankDiff > 0) ? 1: -1;
                    int fileStep = (fileDiff > 0) ? 1 : -1;

                    int currentSq = sq1 + 8 * rankStep + fileStep;
       
                    while (currentSq != sq2) {
                        ray |= (1L << currentSq);
                        currentSq += 8 * rankStep + fileStep;
                    }
                }
                rayBetween[sq1][sq2] = ray;
            }
        }
    }
    private void generateRayMask(){
        for (int sq1 = 0; sq1 < 64; sq1++) {
            for (int sq2 = 0; sq2 < 64; sq2++) {

                int fileDiff = sq2 % 8 - sq1 % 8;
                int rankDiff = sq2 / 8 - sq1 / 8;
                long ray = 0L;

                if (fileDiff == 0) {
                    ray |= 0x101010101010101L << (sq2 % 8);
                }
                if (rankDiff == 0) {
                    ray |= 0xffL << ((sq2 / 8)*8);
                }
                if (Math.abs(rankDiff) == Math.abs(fileDiff)) {
                    for(int a = -7;a <= 7;a++){

                        int rankStep = (rankDiff > 0) ? 1 : -1;
                        int fileStep = (fileDiff > 0) ? 1 : -1;
                        int currentSq = sq1 + 8 * rankStep * a + fileStep * a;
                        if (currentSq >= 0 && currentSq <= 63) {
                            if (ray != 0 && (1L << currentSq & 0xff818181818181ffL) != 0) {
                                ray |= 1L << currentSq;
                                break;
                            }
                            ray |= 1L << currentSq;
                        }
                    }

                }

                rayMask[sq1][sq2] = ray;
            }
        }
    }

}
