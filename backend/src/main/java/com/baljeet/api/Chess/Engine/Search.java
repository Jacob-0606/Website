package com.baljeet.api.Chess.Engine;

import com.baljeet.api.Chess.Core.Board;
import com.baljeet.api.Chess.Core.MoveGeneration;
import com.baljeet.api.Chess.Core.MoveList;
import com.baljeet.api.Chess.Core.Piece;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;

public class Search {

    private final MoveGeneration moveGeneration;
    private final Board board;
    private final Evaluation evaluation;

    private int searchDepth;
    private long start;
    private long timeForMove;
    private long nodesSearched;
    private long quiescenceNodes;
    private boolean searchCancelled;
    private int optimalMoveIteration;
    public int optimalMove;
    public int eval;
    private TranspositionTable.TTEntry entry;
    private final ArrayList<Long> gameHistory;
    private final MoveOrdering moveOrdering;
    private static final Logger logger = LoggerFactory.getLogger(Search.class);

    private int[][] killerMoves;

    TranspositionTable tt;
    // Must use power of two
    private final int tableSize = 1 << 19;
    public static final byte EXACT = 0;
    private static final byte LOWER_BOUND = 1;
    private static final byte UPPER_BOUND = 2;
    private static final int maxSearchExtensions = 2;
    private int generation = 0;

    public Search(Board board, MoveGeneration moveGeneration) {
        this.board = board;
        this.moveGeneration = moveGeneration;
        this.gameHistory = board.repetitionTable;
        evaluation = new Evaluation(board);
        tt = new TranspositionTable(tableSize);
        moveOrdering = new MoveOrdering(board, evaluation, tt);
    }

    public int iterativeDeepening(long time, long increment) {
        if (board.fullMoveNumber < 10) {
            int move = OpeningDatabase.lookupPosition(moveGeneration.getAllMoves(false) , board.toString());
            if (move != 0) {
                optimalMove = move;
                return 0;
            }
        }

        logger.debug("******* SEARCH STARTED ********");
        logger.debug("FEN: {}", board);
        boolean mateFound = false;
        searchCancelled = false;
        nodesSearched = 0;
        quiescenceNodes = 0;

        timeForMove = chooseTimeForMove(time, increment);
        start = System.currentTimeMillis();
        logger.debug("Allocated time: {}", timeForMove);

        for (int i = 1; i < 20; i++) {
            searchDepth = i;
            killerMoves = new int[i + 1 + maxSearchExtensions][2];
            moveOrdering.killerMoves = killerMoves;
            int evalIteration = negaMax(Integer.MIN_VALUE / 2, Integer.MAX_VALUE / 2, searchDepth, 0, 0);
            if (searchCancelled) break;
            else {
                optimalMove = optimalMoveIteration;
                eval = evalIteration;
            }
            logger.debug("Depth: {} Eval: {} Best Move: {}", i, eval, MoveList.moveToString(optimalMoveIteration));

            if (eval > 90000) {
                mateFound = true;
                break;
            }
            generation++;

        }
        logger.debug("Mate Found: {}", mateFound);
        logger.debug("Time used [ms]: {}", (System.currentTimeMillis() - start));
        logger.debug("Time remaining [ms]: {}", (timeForMove - System.currentTimeMillis() + start));
        logger.debug("Nodes searched: {}", nodesSearched);
        logger.debug("Quiescence Nodes searched: {}", quiescenceNodes);
        logger.debug("******* SEARCH ENDED ********");
        return eval;

    }

    private int negaMax(int alpha, int beta, int depth, int searchExtensions, int plyFromRoot) {
        if (searchCancelled) return 0;
        nodesSearched++;
        // Check every 1024 nodes if time is up
        if ((nodesSearched & 0x3FF) == 0 && System.currentTimeMillis() - start > timeForMove) {
            searchCancelled = true;
            return 0;
        }
        // Check if evaluated position is already in TT
        entry = tt.lookup(board.zobristHash);
        if (entry != null && entry.depth() >= searchDepth - plyFromRoot && plyFromRoot != 0) {
            if (entry.boundType() == EXACT) return entry.score();
            // New lower bound (beta)
            else if (entry.boundType() == LOWER_BOUND && entry.score() >= beta) return entry.score();
            // New upper bound (alpha)
            else if (entry.boundType() == UPPER_BOUND && entry.score() <= alpha) return entry.score();
        }

        MoveList moveList = moveGeneration.getAllMoves(false);
        if (moveList.isEmpty()) {
            if (moveGeneration.check) {
                return -100000 + plyFromRoot; // checkmate
            } else {
                return 0; // stalemate
            }
        }

        // Static evaluation
        if (depth == 0) return quiescenceSearch(alpha, beta, plyFromRoot);
        int max = Integer.MIN_VALUE / 2;
        moveOrdering.orderMoves(moveList, plyFromRoot, optimalMove, false);

        int initialAlpha = alpha;
        int bestMove = 0;
        for (int i = 0; i < moveList.size(); i++) {
            int move = moveList.get(i);
            board.makeMove(move);
            int score;
            if (board.halfMoveClock >= 100 || (board.halfMoveClock >= 4 && checkForRepetition())) {
                score = 0;
            }
            else {
                int extensions = 0;
                if(searchExtensions < maxSearchExtensions){
                    if (board.isInCheck()) extensions = 1;
                }
                score = -negaMax(-beta, -alpha, depth - 1 + extensions, searchExtensions + extensions, plyFromRoot + 1);
            }
            board.undoMove(move);
            if (score > max) {
                max = score;
                bestMove = move;
                if (plyFromRoot == 0) {
                    optimalMoveIteration = move;
                }
                alpha = Math.max(max, alpha);
            }
            if (score >= beta) {
                if(MoveList.getFlag(move) != Piece.CAPTURE && killerMoves[plyFromRoot][0] != move){
                        killerMoves[plyFromRoot][1] = killerMoves[plyFromRoot][0];
                        killerMoves[plyFromRoot][0] = move;
                }
                return score;
            }

        }
        byte bound;
        if (max <= initialAlpha) {
            bound = UPPER_BOUND;
        } else if (max >= beta) {
            bound = LOWER_BOUND;
        } else {
            bound = EXACT;
        }
        if (!searchCancelled)
            tt.store(board.zobristHash, bestMove, max, searchDepth - plyFromRoot, bound, generation);
        return max;
    }

    private long chooseTimeForMove(long timeLeft, long increment) {

        long buffer = 100;
        long base = (timeLeft - buffer) / 30;

        long timeForMove = base + increment / 2;

        // Never use more than 10 seconds
        return Math.min(timeForMove, 10000);
    }


    private boolean checkForRepetition() {
        long currentHash = board.zobristHash;
        int count = 0;

        // Only look back as far as the half-move clock allows
        int limit = Math.min(gameHistory.size(), board.halfMoveClock + 1);

        for (int i = gameHistory.size() - limit; i < gameHistory.size(); i++) {
            if (gameHistory.get(i) == currentHash) {
                count++;
                if (count >= 3) {
                    return true;
                }
            }
        }
        return false;
    }

    private int quiescenceSearch(int alpha, int beta, int plyFromRoot){
        if (searchCancelled) return 0;
        quiescenceNodes++;
        // Check every 1024 nodes if time is up
        if ((quiescenceNodes & 0x3FF) == 0 && System.currentTimeMillis() - start > timeForMove) {
            searchCancelled = true;
            return 0;
        }
        int e = evaluation.evaluate();
        if (e >= beta) return beta;
        if (e > alpha) alpha = e;

        int max = Integer.MIN_VALUE / 2;

        MoveList moves = moveGeneration.getAllMoves(true);
        if(moves.isEmpty()) return evaluation.evaluate();
        moveOrdering.orderMoves(moves, plyFromRoot, optimalMove, true);

        for (int i = 0; i < moves.size(); i++){
            int move = moves.get(i);
            board.makeMove(move);
            int score = -quiescenceSearch(-beta, -alpha, plyFromRoot + 1);
            board.undoMove(move);
            if(score > max){
                max = score;
                alpha = Math.max(score,alpha);
            }
            if (score >= beta){
                return score;
            }
        }
        return max;
    }
}
