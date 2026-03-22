package com.baljeet.api.Chess.Core;


public class MoveGeneration {


    public long friendly;
    public long enemy;
    public long occupied;
    private long empty;
    private long emptyEnemy;

    public boolean check;
    public boolean doubleCheck;
    private final Board board;
    private boolean white;
    public MoveList moves;

    private long[] friendlyBitboards;
    private long[] enemyBitboards;

    public long pinMask;
    public long attack;
    public long kingAttack;

    public long checkersMask;
    public int positionFriendlyKing;

    private long quiescenceMask;
    private boolean quiescenceSearch;
    public MoveGeneration(Board board){
        this.board = board;

    }
    private void init(){
        white = board.whiteToMove;
        friendlyBitboards = white ? board.whiteBitboards : board.blackBitboards;
        enemyBitboards = white ? board.blackBitboards : board.whiteBitboards;

        friendly = friendlyBitboards[Piece.PAWN] | friendlyBitboards[Piece.KNIGHT] |
                   friendlyBitboards[Piece.BISHOP] | friendlyBitboards[Piece.ROOK] |
                   friendlyBitboards[Piece.QUEEN] | friendlyBitboards[Piece.KING];

        enemy = enemyBitboards[Piece.PAWN] | enemyBitboards[Piece.KNIGHT] |
                enemyBitboards[Piece.BISHOP] | enemyBitboards[Piece.ROOK] |
                enemyBitboards[Piece.QUEEN] | enemyBitboards[Piece.KING];

        occupied = friendly | enemy;
        empty = ~occupied;
        emptyEnemy = empty | enemy;
    }

    public void getAttackMaps() {
        attack = 0L;
        checkersMask = 0L;
        pinMask = 0L;
        kingAttack = 0L;

        check = false;
        doubleCheck = false;
        int checkCount = 0;

        long enemyKing = enemyBitboards[Piece.KING];

        int positionEnemyKing = Long.numberOfTrailingZeros(enemyKing);
        attack |= PrecomputedData.kingMoves[positionEnemyKing];

        long friendlyKing = friendlyBitboards[Piece.KING];
        positionFriendlyKing = Long.numberOfTrailingZeros(friendlyKing);

        long enemyKnights = enemyBitboards[Piece.KNIGHT];
        while (enemyKnights != 0) {

            int position = Long.numberOfTrailingZeros(enemyKnights);
            long knightAttack = PrecomputedData.knightMoves[position];
            enemyKnights &= (enemyKnights - 1);

            if ((knightAttack & friendlyBitboards[Piece.KING]) != 0) {
                checkersMask |= 1L << position;
                checkCount++;
            }
            attack |= knightAttack;
        }

        long enemyPawns = enemyBitboards[Piece.PAWN];
        long[] pawnMoves = white ? PrecomputedData.blackPawnAttacks : PrecomputedData.whitePawnAttacks;
        while (enemyPawns != 0) {
            int position = Long.numberOfTrailingZeros(enemyPawns);
            long pawnAttack = pawnMoves[position];
            enemyPawns &= (enemyPawns - 1);

            if ((pawnAttack & friendlyBitboards[Piece.KING]) != 0) {
                checkersMask |= 1L << position;
                checkCount++;
            }
            attack |= pawnAttack;
        }

        long enemyRooks = enemyBitboards[Piece.ROOK] | enemyBitboards[Piece.QUEEN];
        while (enemyRooks != 0) {
            int position = Long.numberOfTrailingZeros(enemyRooks);
            long rookAttack = PrecomputedData.getRookAttacks(position, occupied);
            enemyRooks &= (enemyRooks - 1);

            if ((rookAttack & friendlyBitboards[Piece.KING]) != 0) {
                checkersMask |= (1L << position) | PrecomputedData.rayBetween[position][positionFriendlyKing];
                kingAttack |= PrecomputedData.getRookAttacks(position,occupied & ~friendlyKing);
                checkCount++;
            }
            attack |= rookAttack;
        }

        long enemyBishops = enemyBitboards[Piece.BISHOP] | enemyBitboards[Piece.QUEEN];
        while (enemyBishops != 0) {
            int position = Long.numberOfTrailingZeros(enemyBishops);
            long bishopAttack = PrecomputedData.getBishopAttacks(position, occupied);
            enemyBishops &= (enemyBishops - 1);
            if ((bishopAttack & friendlyBitboards[Piece.KING]) != 0) {
                checkersMask |= (1L << position) | PrecomputedData.rayBetween[position][positionFriendlyKing];
                kingAttack |= PrecomputedData.getBishopAttacks(position,occupied & ~friendlyKing);
                checkCount++;
            }
            attack |= bishopAttack;
        }
        switch (checkCount) {
            case 0:
                checkersMask = -1;
                break;
            case 1:
                check = true;
                break;
            default:
                check = true;
                doubleCheck = true;
        }

        //Pins
        if (positionFriendlyKing == 64) 
            System.out.print(board.toString());
        long pinBishop = PrecomputedData.xrayBishopAttacks(occupied, friendly, positionFriendlyKing)
                & (enemyBitboards[Piece.BISHOP]| enemyBitboards[Piece.QUEEN]);
        while (pinBishop != 0) {
            int position = Long.numberOfTrailingZeros(pinBishop);
            long ray = PrecomputedData.rayBetween[position][positionFriendlyKing];

            if (Long.bitCount(ray & friendly) == 1) {
                pinMask |= ray | 1L << position;
            }
            pinBishop &= (pinBishop - 1);
        }

        long pinRook = PrecomputedData.xrayRookAttacks(occupied, friendly, positionFriendlyKing)
                & (enemyBitboards[Piece.ROOK] | enemyBitboards[Piece.QUEEN]);

        while (pinRook != 0) {
            int position = Long.numberOfTrailingZeros(pinRook);
            long ray = PrecomputedData.rayBetween[position][positionFriendlyKing];

            if (Long.bitCount(ray & friendly) == 1) {
                pinMask |= ray | 1L << position;
            }
            pinRook &= (pinRook - 1);
        }

    }
    public MoveList getAllMoves(boolean quiescenceSearch){
        moves = new MoveList();
        init();
        getAttackMaps();
        this.quiescenceSearch = quiescenceSearch;
        quiescenceMask = quiescenceSearch ? enemy : 0xFFFFFFFFFFFFFFFFL;
        getMovesKing();

        if(!doubleCheck){
            getMovesPawn();
            getMovesKnight();
            getMovesSliding();
        }
        return moves;
    }

    public void getMovesPawn() {
        int piece = Piece.PAWN;
        long pawnBoard;
        long singlePushes;
        long doublePushes;
        int pushOffset;
        long capturesLeft;
        long capturesRight;

        if (white) {
            pawnBoard = board.whiteBitboards[piece];

            singlePushes = pawnBoard << 8 & empty
                    & checkersMask;

            doublePushes = ((pawnBoard & 0x000000000000ff00L) << 8 & empty)
                    << 8 & empty & checkersMask;

            capturesRight =  (pawnBoard << 7) & 0x7f7f7f7f7f7f7f7fL
                    & checkersMask & enemy;

            capturesLeft = (pawnBoard << 9) & 0xfefefefefefefefeL
                    & checkersMask & enemy;
            pushOffset = -8;

        } else {
            pawnBoard = board.blackBitboards[piece];
            singlePushes = (pawnBoard) >>> 8 & empty
                    & checkersMask;

            doublePushes = ((pawnBoard & 0x00ff000000000000L) >>> 8 & empty)
                    >>> 8 & empty & checkersMask;

            capturesRight = (pawnBoard >>> 9) & 0x7f7f7f7f7f7f7f7fL
                    & checkersMask & enemy;

            capturesLeft = (pawnBoard >>> 7) & 0xfefefefefefefefeL
                    & checkersMask & enemy;

            pushOffset = 8;
        }

        //captures
        while(capturesRight != 0){
            int to = Long.numberOfTrailingZeros(capturesRight);
            capturesRight &= (capturesRight - 1);
            int from = to + pushOffset + 1;
            if (isNotPinned(from) || stayOnRay(from,to)) {
                addMovesSingleCaptures(from,to,Piece.CAPTURE);
            }
        }
        while(capturesLeft != 0){
            int to = Long.numberOfTrailingZeros(capturesLeft);
            capturesLeft &= (capturesLeft - 1);
            int from = to +  pushOffset-1;
            if (isNotPinned(from) || stayOnRay(from,to)) {
                addMovesSingleCaptures(from,to,Piece.CAPTURE);
            }
        }
        //en passant
        if (board.enPassantSquare != -1) {
            long pawns;
            long epTarget = 1L << board.enPassantSquare;
            if (white) {
                pawns = ((epTarget >>> 7) & ~0x101010101010101L) | ((epTarget >>> 9) & ~0x8080808080808080L);
            } else {
                pawns = ((epTarget << 7) & ~0x8080808080808080L) | ((epTarget << 9) & ~0x101010101010101L);
            }
            pawns &= pawnBoard;
            while (pawns != 0) {
                int from = Long.numberOfTrailingZeros(pawns);
                if ((isNotPinned(from) || stayOnRay(from,board.enPassantSquare))
                        && !inCheckAfterEnPassant(from,board.enPassantSquare, board.enPassantSquare+pushOffset)) {
                    moves.add(MoveList.packMove(from, board.enPassantSquare, Piece.EN_PASSANT));
                }
                pawns &= (pawns-1);
            }
        }
        if(quiescenceSearch) return;
        //single pushes
        while (singlePushes != 0) {
            int to = Long.numberOfTrailingZeros(singlePushes);
            singlePushes &= (singlePushes - 1);
            int from = to + pushOffset;
            if (isNotPinned(from) || stayOnRay(from,to)) {
                addMovesSingleCaptures(from,to,Piece.NO_FLAG);
            }
        }

        //double pushes
        while (doublePushes != 0) {
            int to = Long.numberOfTrailingZeros(doublePushes);
            doublePushes &= (doublePushes - 1);
            int from = to + 2 * pushOffset;
            if (isNotPinned(from) || stayOnRay(from,to)) {
                moves.add(MoveList.packMove(from, to, Piece.DOUBLE_PUSH));
            }
        }

    }
    public void getMovesKnight(){

        long knights = friendlyBitboards[Piece.KNIGHT] & ~pinMask;

        while (knights != 0){
            int from = Long.numberOfTrailingZeros(knights);
            long possibleMoves = PrecomputedData.knightMoves[from] & checkersMask & emptyEnemy & quiescenceMask;
                while (possibleMoves != 0) {
                    int to = Long.numberOfTrailingZeros(possibleMoves);
                    int flag = ((enemy & 1L << to) == 0) ? Piece.NO_FLAG : Piece.CAPTURE;
                    moves.add(MoveList.packMove(from,to,flag));
                    possibleMoves &= (possibleMoves - 1);
                }

            knights &=(knights-1);
        }
    }
    public void getMovesKing(){
        long possibleMoves = PrecomputedData.kingMoves[positionFriendlyKing] & ~attack & emptyEnemy & ~kingAttack & quiescenceMask;
        while (possibleMoves != 0){
            int to = Long.numberOfTrailingZeros(possibleMoves);
            int flag = ((enemy & 1L << to) == 0) ? Piece.NO_FLAG : Piece.CAPTURE;
            moves.add(MoveList.packMove(positionFriendlyKing,to,flag));
            possibleMoves &= (possibleMoves-1);
        }
        //castling
        if(quiescenceSearch) return;


        int sideOffset = white ? 0 : 56;
        int castlingBits = board.castlingRights >> (white ? 0 : 2);

        if ((castlingBits & 1) != 0 && !check) {
            int kTarget = PrecomputedData.kingCastlingPositions[white ? 0 : 2];
            int rTarget = PrecomputedData.rookCastlingPositions[white ? 0 : 2];
            int rStart = board.castlingFiles[white ? 0 : 2] + sideOffset;

            
            long blockers = PrecomputedData.rayBetween[positionFriendlyKing][kTarget] | (1L << kTarget) |
                        PrecomputedData.rayBetween[rStart][rTarget] | (1L << rTarget);
            
            blockers &= ~(1L << positionFriendlyKing);
            blockers &= ~(1L << rStart);

            long actualOccupied = occupied & ~(1L << positionFriendlyKing) & ~(1L << rStart);
            if ((blockers & actualOccupied) == 0) {
                long transit = PrecomputedData.rayBetween[positionFriendlyKing][kTarget] | (1L << kTarget);
                if ((transit & attack) == 0) {
                    moves.add(MoveList.packMove(positionFriendlyKing, kTarget, Piece.KING_CASTLE));
                }
            }
        }

        if ((castlingBits & 2) != 0 && !check) {
            int kTarget = PrecomputedData.kingCastlingPositions[white ? 1 : 3];
            int rTarget = PrecomputedData.rookCastlingPositions[white ? 1 : 3];
            int rStart = board.castlingFiles[white ? 1 : 3] + sideOffset;

            long blockers = PrecomputedData.rayBetween[positionFriendlyKing][kTarget] | (1L << kTarget) |
                        PrecomputedData.rayBetween[rStart][rTarget] | (1L << rTarget);
            
            blockers &= ~(1L << positionFriendlyKing);
            blockers &= ~(1L << rStart);

            long actualOccupied = occupied & ~(1L << positionFriendlyKing) & ~(1L << rStart);
            if ((blockers & actualOccupied) == 0) {
                long transit = PrecomputedData.rayBetween[positionFriendlyKing][kTarget] | (1L << kTarget);
                if ((transit & attack) == 0) {
                    moves.add(MoveList.packMove(positionFriendlyKing, kTarget, Piece.QUEEN_CASTLE));
                }
            }
        }
    }
    public void getMovesSliding() {
        long rooks = friendlyBitboards[Piece.ROOK] | friendlyBitboards[Piece.QUEEN];
        long bishops = friendlyBitboards[Piece.BISHOP] | friendlyBitboards[Piece.QUEEN];
        long moveMask = emptyEnemy & checkersMask;

        while (rooks != 0) {
            int from = Long.numberOfTrailingZeros(rooks);
            long possibleMoves = PrecomputedData.getRookAttacks(from, occupied) & moveMask & quiescenceMask;
            if (!isNotPinned(from)){
                possibleMoves &= PrecomputedData.rayMask[positionFriendlyKing][from];
            }
            while (possibleMoves != 0) {
                int to = Long.numberOfTrailingZeros(possibleMoves);
                int flag = ((enemy & 1L << to) == 0) ? Piece.NO_FLAG : Piece.CAPTURE;
                moves.add(MoveList.packMove(from, to, flag));
                possibleMoves &= possibleMoves - 1;
            }
            rooks &= (rooks - 1);
        }


        while(bishops != 0){
            int from = Long.numberOfTrailingZeros(bishops);
            long possibleMoves = PrecomputedData.getBishopAttacks(from, occupied) & moveMask & quiescenceMask;
            if (!isNotPinned(from)){
                possibleMoves &= PrecomputedData.rayMask[positionFriendlyKing][from];
            }
            while (possibleMoves != 0) {
                int to = Long.numberOfTrailingZeros(possibleMoves);
                int flag = ((enemy & 1L << to) == 0) ? Piece.NO_FLAG : Piece.CAPTURE;
                moves.add(MoveList.packMove(from, to, flag));
                possibleMoves &= possibleMoves - 1;
            }
            bishops &= (bishops - 1);
        }
    }

    private boolean isNotPinned(int square) {
        return (pinMask & (1L << square)) == 0;
    }
    private boolean stayOnRay(int from,int to){

        return  PrecomputedData.rayMask[positionFriendlyKing][from] ==
                PrecomputedData.rayMask[positionFriendlyKing][to];
    }
   private boolean inCheckAfterEnPassant(int from, int to, int capturedPawn){
        long rooks = enemyBitboards[Piece.ROOK] | enemyBitboards[Piece.QUEEN];
       if (rooks != 0)
       {
           long maskedBlockers = (occupied ^ (1L << capturedPawn | 1L << from | 1L << to));
           long rookAttacks = PrecomputedData.getRookAttacks(positionFriendlyKing, maskedBlockers);
           return (rookAttacks & rooks) != 0;
       }

       return false;
   }
   private void addMovesSingleCaptures(int from, int to,int flag){
       if ((1L << to & 0xff000000000000ffL) == 0) {
           moves.add(MoveList.packMove(from, to, flag));
       }
       else {
           moves.add(MoveList.packMove(from, to, Piece.PROMOTION_QUEEN));
           moves.add(MoveList.packMove(from, to, Piece.PROMOTION_KNIGHT));
           moves.add(MoveList.packMove(from, to, Piece.PROMOTION_BISHOP));
           moves.add(MoveList.packMove(from, to, Piece.PROMOTION_ROOK));
       }
   }

}

