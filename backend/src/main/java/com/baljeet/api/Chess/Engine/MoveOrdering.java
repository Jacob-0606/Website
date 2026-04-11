package com.baljeet.api.Chess.Engine;

import com.baljeet.api.Chess.Core.Board;
import com.baljeet.api.Chess.Core.MoveList;
import com.baljeet.api.Chess.Core.Piece;

public class MoveOrdering {
    private final Board board;
    private final Evaluation evaluation;
    private final TranspositionTable tt;
    public int[][] killerMoves;
    private TranspositionTable.TTEntry entry;

    public MoveOrdering(Board board, Evaluation evaluation, TranspositionTable tt){
        this.board = board;
        this.evaluation = evaluation;
        this.tt = tt;
    }
    public void orderMoves(MoveList moves, int plyFromRoot, int optimalMove, boolean quiescence){

        entry = tt.lookup(board.zobristHash);
        int[] scores = new int[moves.size()];

        for (int i = 0; i < moves.size(); i++) {
            int move = moves.get(i);
            scores[i] = getScore(plyFromRoot, move, optimalMove, quiescence);
        }
        for (int i = 0; i < moves.size(); i++) {
            int maxIndex = i;

            for (int j = i + 1; j < moves.size(); j++) {
                if (scores[j] > scores[maxIndex]) {
                    maxIndex = j;
                }
            }
            int tempScore = scores[i];
            scores[i] = scores[maxIndex];
            scores[maxIndex] = tempScore;

            int tempMove = moves.get(i);
            moves.set(i, moves.get(maxIndex));
            moves.set(maxIndex, tempMove);
        }
    }
    private int getScore(int plyFromRoot, int move, int optimalMove, boolean quiescence) {
        int score = 0;

        if (plyFromRoot == 0 && move == optimalMove)
            return score + 10000;
        else if (entry != null && entry.bestMove() == move)
            return score + 9000;
        else if (!quiescence && (move == killerMoves[plyFromRoot][0] ||
                                 move == killerMoves[plyFromRoot][1]))
            return score + 8000;

        int to = MoveList.getTo(move);
        int from = MoveList.getFrom(move);
        int flag = MoveList.getFlag(move);
        int piece = board.currentPosition[from];
        int phase = evaluation.getPhase();
        boolean w = board.whiteToMove;

        switch (piece) {
            case Piece.KNIGHT -> score += getMovePSTScore(EvaluationData.KNIGHT_PST
                    , from, to);

            case Piece.PAWN -> score += getMovePSTScore(w ? EvaluationData.W_PAWN_PST
                            : EvaluationData.B_PAWN_PST
                    , from, to);
            case Piece.BISHOP -> score += getMovePSTScore(w ? EvaluationData.W_BISHOP
                            : EvaluationData.B_BISHOP
                    , from, to);

            case Piece.KING ->
                    score += w ? getBlendedMovePSTScore(EvaluationData.W_KING_PST_MIDDLE, EvaluationData.W_KING_PST_END, phase, from, to)
                            : getBlendedMovePSTScore(EvaluationData.B_KING_PST_MIDDLE, EvaluationData.B_KING_PST_END, phase, from, to);
        }
        switch(flag) {
            case Piece.NO_FLAG, Piece.DOUBLE_PUSH, Piece.EN_PASSANT -> {}
            case Piece.CAPTURE -> score += getMVVLVAScore(from, to) + 500;
            case Piece.QUEEN_CASTLE, Piece.KING_CASTLE -> score += 50;
            case Piece.PROMOTION_KNIGHT -> score += EvaluationData.KNIGHT_WEIGHT;
            case Piece.PROMOTION_BISHOP -> score += EvaluationData.BISHOP_WEIGHT;
            case Piece.PROMOTION_ROOK -> score += EvaluationData.ROOK_WEIGHT;
            case Piece.PROMOTION_QUEEN -> score += EvaluationData.QUEEN_WEIGHT;
        }
        return score;
    }
    private int getMovePSTScore(int[] PST, int from, int to){
        return PST[to] - PST[from];
    }
    private int getBlendedMovePSTScore(int[] PSTMiddle, int[] PSTEnd, int phase, int from, int to){
        return ((EvaluationData.TOTAL_PHASE - phase) * getMovePSTScore(PSTMiddle, from, to)
                + phase * getMovePSTScore(PSTEnd, from, to)) / EvaluationData.TOTAL_PHASE;
    }
    private int getMVVLVAScore(int from, int to){
        int pieceAttacker = board.currentPosition[from];
        int pieceVictim = board.currentPosition[to];

        return EvaluationData.MVVLVA[pieceVictim][pieceAttacker];
    }
}
