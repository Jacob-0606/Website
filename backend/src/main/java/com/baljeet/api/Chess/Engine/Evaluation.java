package com.baljeet.api.Chess.Engine;

import com.baljeet.api.Chess.Core.Board;
import com.baljeet.api.Chess.Core.Piece;


public class Evaluation {

    private final Board board;


    public Evaluation(Board board){
        this.board = board;
    }


    public int evaluate(){
        long[] white = board.whiteBitboards;
        long[] black = board.blackBitboards;

        // Material score
        int whiteScore = materialScore(white);
        int blackScore = materialScore(black);
        int phase = getPhase();


        // Apply PSTs
        whiteScore += applyPST(white[Piece.PAWN], EvaluationData.W_PAWN_PST);
        blackScore += applyPST(black[Piece.PAWN], EvaluationData.B_PAWN_PST);

        whiteScore += applyPST(white[Piece.KNIGHT], EvaluationData.KNIGHT_PST);
        blackScore += applyPST(black[Piece.KNIGHT], EvaluationData.KNIGHT_PST);

        whiteScore += applyPST(white[Piece.BISHOP], EvaluationData.W_BISHOP);
        blackScore += applyPST(black[Piece.BISHOP], EvaluationData.B_BISHOP);

        whiteScore += getBlendedPSTScore(white[Piece.KING], phase, EvaluationData.W_KING_PST_MIDDLE, EvaluationData.W_KING_PST_END);
        blackScore += getBlendedPSTScore(black[Piece.KING], phase, EvaluationData.B_KING_PST_MIDDLE, EvaluationData.B_KING_PST_END);

        // If winning, move king closer to opponent king
        // Sometimes getting out of bounds exception bug not easily reproducible
        /*if (whiteScore - blackScore > 0){
            whiteScore += (15 - EvaluationData.Manhattan[Long.numberOfTrailingZeros(white[Piece.KING])][Long.numberOfTrailingZeros(black[Piece.KING])]) * phase;
        }
        else{
            blackScore += (15 - EvaluationData.Manhattan[Long.numberOfTrailingZeros(white[Piece.KING])][Long.numberOfTrailingZeros(black[Piece.KING])]) * phase;
        }*/

        whiteScore += evaluatePawnStructure(white[Piece.PAWN]);
        blackScore += evaluatePawnStructure(black[Piece.PAWN]);

        return (whiteScore - blackScore) * ( board.whiteToMove ? 1 : -1);

    }
    private int applyPST(long bitboard,int[] PST){
        int score = 0;
        while(bitboard != 0){
            int position = Long.numberOfTrailingZeros(bitboard);
            score += PST[position];
            bitboard &= (bitboard-1);
        }
        return score;
    }

    public int getPhase() {
        int phase = EvaluationData.TOTAL_PHASE;

        long[] white = board.whiteBitboards;
        long[] black = board.blackBitboards;

        phase -= EvaluationData.PHASE_KNIGHT * (Long.bitCount(white[Piece.KNIGHT]) + Long.bitCount(black[Piece.KNIGHT]));
        phase -= EvaluationData.PHASE_BISHOP * (Long.bitCount(white[Piece.BISHOP]) + Long.bitCount(black[Piece.BISHOP]));
        phase -= EvaluationData.PHASE_ROOK * (Long.bitCount(white[Piece.ROOK]) + Long.bitCount(black[Piece.ROOK]));
        phase -= EvaluationData.PHASE_QUEEN * (Long.bitCount(white[Piece.QUEEN]) + Long.bitCount(black[Piece.QUEEN]));

        return phase;
    }
    public int getBlendedPSTScore(long bitboard, int phase, int[] midgamePST, int[] endgamePST){
        return ((EvaluationData.TOTAL_PHASE - phase) * applyPST(bitboard, midgamePST)
                + phase * applyPST(bitboard, endgamePST)) / EvaluationData.TOTAL_PHASE;
    }

    public int materialScore(long[] bitboards){
        return    EvaluationData.QUEEN_WEIGHT * Long.bitCount(bitboards[Piece.QUEEN])
                + EvaluationData.ROOK_WEIGHT * Long.bitCount(bitboards[Piece.ROOK])
                + EvaluationData.KNIGHT_WEIGHT * Long.bitCount(bitboards[Piece.KNIGHT])
                + EvaluationData.BISHOP_WEIGHT * Long.bitCount(bitboards[Piece.BISHOP])
                + EvaluationData.PAWN_WEIGHT * Long.bitCount(bitboards[Piece.PAWN]);
    }

    public int evaluatePawnStructure(long pawnBitboard) {
        int score = 0;
        long mask = pawnBitboard;
        while (pawnBitboard != 0) {
            int position = Long.numberOfTrailingZeros(pawnBitboard);
            // Doubled Pawns
            if ((mask & EvaluationData.files[position % 8]) != (1L << position)) score -= 5;
            // Isolated Pawns
            if ((mask & EvaluationData.isolatedPawnMask[position % 8]) == 0)
                score -= 5;
            pawnBitboard &= (pawnBitboard - 1);
        }
        return score;
    }
}
