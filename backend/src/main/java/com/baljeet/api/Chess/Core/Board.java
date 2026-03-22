package com.baljeet.api.Chess.Core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import java.util.Stack;

public class Board {
    public long[] whiteBitboards;
    public long[] blackBitboards;

    public int[] currentPosition;
    public long[][] zobristRandomNumbers;
    public long zobristHash;

    public boolean whiteToMove;
    public byte castlingRights;

    public int enPassantSquare = -1; // -1 if no square
    public int halfMoveClock;
    public int fullMoveNumber;

    public Stack<GameState> gameStates;
    public final ArrayList<Long> repetitionTable;
    public final boolean chess960;
    public int[] castlingFiles = {0, 7, 0, 7};
    private int whiteKingFile = 3;
    private int blackKingFile = 3;
    private final int blackRankOffset = 56;

    static final byte[] CASTLING_MASK = new byte[64];


     public static class GameState {
        public int capturedPiece;
        public int halfMoveClock;
        public int enPassantSquare;
        public byte castlingRights;
        public long zobristHash;
    }
    

    public Board(String FEN, boolean chess960){
         this.chess960 = chess960;
         gameStates = new Stack<>();
         setBoard(FEN);
         initializeCastlingMask();
         initializeZobristHash();
         repetitionTable = new ArrayList<>(100);
         repetitionTable.add(zobristHash);
    }
    private void initializeCastlingMask (){
        Arrays.fill(CASTLING_MASK, (byte) 0b1111);
        // White
        CASTLING_MASK[whiteKingFile] &= 0b1100;
        CASTLING_MASK[castlingFiles[0]] &= 0b1110; // disable WK
        CASTLING_MASK[castlingFiles[1]] &= 0b1101; // disable WQ
        // Black
        CASTLING_MASK[blackKingFile + blackRankOffset] &= 0b0011;
        CASTLING_MASK[castlingFiles[2] + blackRankOffset] &= 0b1011; // disable BK
        CASTLING_MASK[castlingFiles[3] + blackRankOffset] &= 0b0111; // disable BQ
    }
    private void initializeZobristHash(){
        MersenneTwister mersenneTwister = new MersenneTwister(8);
        zobristRandomNumbers = new long[67][];

        //pieces on 64 squares
        for (int i = 0; i < 64; i++) {
            long[] random = new long[13];
            for (int j = 0; j < 13; j++) {
                random[j] = mersenneTwister.nextLong();
            }
            zobristRandomNumbers[i] = random;
        }
        //white to move
        zobristRandomNumbers[64] = new long[]{mersenneTwister.nextLong()};

        //castling
        long[] randomCastle = new long[16];
        for (int i = 0; i < 16; i++) {
            randomCastle[i] = mersenneTwister.nextLong();
        }
        zobristRandomNumbers[65] = randomCastle;

        //en passant
        long[] randomEnPassant = new long[8];
        for (int i = 0; i < 8; i++) {
            randomEnPassant[i] = mersenneTwister.nextLong();
        }
        zobristRandomNumbers[66] = randomEnPassant;

        for (int i = 0; i < 64; i++) {
            if(currentPosition[i] == 0) continue;
            int offset = ((1L << i) & whiteBitboards[currentPosition[i]]) != 0 ? 0 : 6;
            zobristHash ^= zobristRandomNumbers[i][(currentPosition[i] + offset)];
        }
        if(!whiteToMove) zobristHash ^= zobristRandomNumbers[64][0];


        zobristHash ^= zobristRandomNumbers[65][castlingRights];

        if(enPassantSquare != -1) zobristHash^= zobristRandomNumbers[66][enPassantSquare % 8];

    }
    private void setBoard(String FEN) {
        whiteBitboards = new long[7];
        blackBitboards = new long[7];

        currentPosition = new int[64];
        String[] parts = FEN.split(" ");
        String[] rows = parts[0].split("/");

        int squareIndex = 63; // a8 (top-left)
        //Board
        for (String row : rows) {
            for (char c : row.toCharArray()) {
                if (Character.isDigit(c)) {
                    squareIndex -= (c - '0');
                } else {
                    long bit = 1L << squareIndex;
                    switch (c) {
                        //white
                        case 'P' -> {
                            whiteBitboards[1] |= bit;
                            currentPosition[squareIndex] = Piece.PAWN;
                        }
                        case 'N' -> {
                            whiteBitboards[2] |= bit;
                            currentPosition[squareIndex] = Piece.KNIGHT;
                        }
                        case 'B' -> {
                            whiteBitboards[3] |= bit;
                            currentPosition[squareIndex] = Piece.BISHOP;
                        }
                        case 'R' -> {
                            whiteBitboards[4] |= bit;
                            currentPosition[squareIndex] = Piece.ROOK;
                        }
                        case 'Q' -> {
                            whiteBitboards[5] |= bit;
                            currentPosition[squareIndex] = Piece.QUEEN;
                        }
                        case 'K' -> {
                            whiteBitboards[6] |= bit;
                            currentPosition[squareIndex] = Piece.KING;
                        }
                        case 'p' -> {
                            blackBitboards[1] |= bit;
                            currentPosition[squareIndex] = Piece.PAWN;
                        }
                        case 'n' -> {
                            blackBitboards[2] |= bit;
                            currentPosition[squareIndex] = Piece.KNIGHT;
                        }
                        case 'b' -> {
                            blackBitboards[3] |= bit;
                            currentPosition[squareIndex] = Piece.BISHOP;
                        }
                        case 'r' -> {
                            blackBitboards[4] |= bit;
                            currentPosition[squareIndex] = Piece.ROOK;
                        }
                        case 'q' -> {
                            blackBitboards[5] |= bit;
                            currentPosition[squareIndex] = Piece.QUEEN;
                        }
                        case 'k' -> {
                            blackBitboards[6] |= bit;
                            currentPosition[squareIndex] = Piece.KING;
                        }
                    }
                    squareIndex--;
                }
            }
        }
        for (int i = 0; i < 64; i++) {
            if (currentPosition[i] == Piece.KING) {
                if ((whiteBitboards[Piece.KING] & (1L << i)) != 0) {
                    whiteKingFile = i % 8;
                } else {
                    blackKingFile = i % 8;
                }
            }
        }
        //turn
        whiteToMove = parts[1].equals("w");

        String castling = parts[2];
        if(chess960) {
            String whiteRights = castling.replaceAll("[a-z-]", "");
            String blackRights = castling.replaceAll("[A-Z-]", "");

            castlingRights |= getChess960Castling(whiteRights, 0);
            castlingRights |= getChess960Castling(blackRights, 2);
        }
        else {
            castlingRights |= (byte) (castling.contains("K")? 0b0001:0b0000);
            castlingRights |= (byte) (castling.contains("Q")? 0b0010:0b0000);
            castlingRights |= (byte) (castling.contains("k")? 0b0100:0b0000);
            castlingRights |= (byte) (castling.contains("q")? 0b1000:0b0000);
        }
        // 4. En passant
        String enPassant = parts[3];
        enPassantSquare = enPassant.equals("-") ? -1 : squareToIndex(enPassant);

        // 5. Half move and Full move
        halfMoveClock = Integer.parseInt(parts[4]);
        fullMoveNumber = Integer.parseInt(parts[5]);

    }
    private byte getChess960Castling(String rights, int offset){
        byte castlingRights = 0;
        int kingFile = (offset == 0) ? whiteKingFile : blackKingFile;
        if (!rights.isEmpty()) {
            char r1 = rights.charAt(0);
            if (rights.length() == 1) {
                castlingRights |= (byte)(kingFile > fileToIndex(r1) ?
                                        0b0001 : 0b0010);
            } 
            else {
                char r2 = rights.charAt(1);
                char kingsideRook = (r1 > r2) ? r1 : r2;
                char queensideRook = (r1 > r2) ? r2 : r1;
                
                castlingRights |= 0b0001; 
                castlingRights |= 0b0010; 
                
                castlingFiles[offset] = fileToIndex(kingsideRook); // kingside rook
                castlingFiles[offset + 1] = fileToIndex(queensideRook); // queenside rook;
            }
        }
        return (byte) (castlingRights << offset); 
    }
    public void makeMove(int move){
        GameState info = new GameState();
     
        int from = MoveList.getFrom(move);
        int to = MoveList.getTo(move);

        int piece = currentPosition[from];
        int flag = MoveList.getFlag(move);

        long[] enemyPieces = whiteToMove ? blackBitboards : whiteBitboards;
        long[] friendlyPieces = whiteToMove ? whiteBitboards : blackBitboards;
        int friendlyOffset = whiteToMove ? 0 : 6;
        int enemyOffset = whiteToMove ? 6 : 0;

        info.enPassantSquare = enPassantSquare;
        info.castlingRights = castlingRights;
        info.capturedPiece = Piece.EMPTY;
        info.zobristHash = zobristHash;
   
        // Remove piece from from-square
        friendlyPieces[piece] &= ~(1L << from);
        currentPosition[from] = Piece.EMPTY;
        zobristHash ^= zobristRandomNumbers[from][piece + friendlyOffset];

        // Handle en passant capture
        if (flag == Piece.EN_PASSANT) {
            int capturedPawnSquare = whiteToMove ? to - 8 : to + 8;
            enemyPieces[Piece.PAWN] &= ~(1L << capturedPawnSquare);
            currentPosition[capturedPawnSquare] = Piece.EMPTY;
            info.capturedPiece = Piece.PAWN;

            zobristHash ^= zobristRandomNumbers[capturedPawnSquare][Piece.PAWN + enemyOffset];
        }

        // Handle regular captures
        else if (flag == Piece.CAPTURE) {
            info.capturedPiece = currentPosition[to];
            zobristHash ^= zobristRandomNumbers[to][currentPosition[to] + enemyOffset];
            enemyPieces[currentPosition[to]] &= ~(1L << to);

        }//Handle Castling
        else if (flag == Piece.KING_CASTLE) {
            int sideOffset = whiteToMove ? 0 : blackRankOffset;
            int rookFromSquare = castlingFiles[whiteToMove ? 0 : 2] + sideOffset;

            currentPosition[rookFromSquare] = Piece.EMPTY;
            friendlyPieces[Piece.ROOK] &= ~(1L << rookFromSquare);
            zobristHash ^= zobristRandomNumbers[rookFromSquare][Piece.ROOK + friendlyOffset];

            currentPosition[to + 1] = Piece.ROOK;
            friendlyPieces[Piece.ROOK] |= (1L << (to + 1));
            zobristHash ^= zobristRandomNumbers[to + 1][Piece.ROOK + friendlyOffset];
        }
        else if (flag == Piece.QUEEN_CASTLE) {
            int sideOffset = whiteToMove ? 0 : blackRankOffset;
            int rookFromSquare = castlingFiles[whiteToMove ? 1 : 3] + sideOffset;

            currentPosition[rookFromSquare] = Piece.EMPTY;
            friendlyPieces[Piece.ROOK] &= ~(1L << rookFromSquare);
            zobristHash ^= zobristRandomNumbers[rookFromSquare][Piece.ROOK + friendlyOffset];

            currentPosition[to - 1] = Piece.ROOK;
            friendlyPieces[Piece.ROOK] |= (1L << (to - 1));
            zobristHash ^= zobristRandomNumbers[to - 1][Piece.ROOK + friendlyOffset];
        }

        // Place moved piece
        if (flag >= Piece.PROMOTION_KNIGHT && flag <= Piece.PROMOTION_QUEEN) {
            if (currentPosition[to] != 0) {
                info.capturedPiece = currentPosition[to];
                zobristHash ^= zobristRandomNumbers[to][currentPosition[to] + enemyOffset];
                enemyPieces[currentPosition[to]] &= ~(1L << to);
            }
            friendlyPieces[flag] |= (1L << to);
            currentPosition[to] = flag;
            zobristHash ^= zobristRandomNumbers[to][flag + friendlyOffset];
        } else {
            friendlyPieces[piece] |= (1L << to);
            currentPosition[to]= piece;
            zobristHash ^= zobristRandomNumbers[to][piece+friendlyOffset];
        }
        if(info.enPassantSquare != -1) zobristHash ^= zobristRandomNumbers[66][enPassantSquare%8];
        // Update en passant square
        if (flag == Piece.DOUBLE_PUSH) {
            enPassantSquare = whiteToMove ? from + 8 : from - 8;
            zobristHash ^= zobristRandomNumbers[66][enPassantSquare % 8];
        } else {
            enPassantSquare = -1;
        }
        // Update castling rights
        zobristHash ^= zobristRandomNumbers[65][castlingRights];
        castlingRights &= CASTLING_MASK[from];
        castlingRights &= CASTLING_MASK[to];
        zobristHash ^= zobristRandomNumbers[65][castlingRights];

        // 50 move rule
        info.halfMoveClock = halfMoveClock;
        if (piece == Piece.PAWN || flag == Piece.CAPTURE) {
            halfMoveClock = 0;
        } else {
            halfMoveClock++;
        }
        //move counter
        if(!whiteToMove){
            fullMoveNumber++;
        }
        whiteToMove = !whiteToMove;
        zobristHash ^= zobristRandomNumbers[64][0];
        gameStates.push(info);
        repetitionTable.add(zobristHash);
    }

    public void undoMove(int move) {
        GameState info = gameStates.pop();
        repetitionTable.remove(repetitionTable.size() - 1);

        int from = MoveList.getFrom(move);
        int to = MoveList.getTo(move);
        int flag = MoveList.getFlag(move);

        // Switch turn back
        whiteToMove = !whiteToMove;

        long[] enemyPieces = whiteToMove ? blackBitboards : whiteBitboards;
        long[] friendlyPieces = whiteToMove ? whiteBitboards : blackBitboards;

        int movedPiece = currentPosition[to];

        // Undo promotion
        if (flag >= Piece.PROMOTION_KNIGHT && flag <= Piece.PROMOTION_QUEEN) {
            // Remove promoted piece
            friendlyPieces[movedPiece] &= ~(1L << to);
            // Add pawn back
            friendlyPieces[Piece.PAWN] |= (1L << from);
            currentPosition[from] = Piece.PAWN;
            currentPosition[to] = info.capturedPiece;

            if (info.capturedPiece != Piece.EMPTY) {
                enemyPieces[info.capturedPiece] |= (1L << to);
            }
        }
        // Undo castling
        else if (flag == Piece.KING_CASTLE) {
            int sideOffset = whiteToMove ? 0 : blackRankOffset;
            int rookOriginalSquare = castlingFiles[whiteToMove ? 0 : 2] + sideOffset;

            friendlyPieces[Piece.KING] &= ~(1L << to);
            currentPosition[to] = Piece.EMPTY;

            friendlyPieces[Piece.ROOK] &= ~(1L << (to + 1));
            currentPosition[to + 1] = Piece.EMPTY;

            friendlyPieces[Piece.KING] |= (1L << from);
            currentPosition[from] = Piece.KING;

            friendlyPieces[Piece.ROOK] |= (1L << rookOriginalSquare);
            currentPosition[rookOriginalSquare] = Piece.ROOK;

        } else if (flag == Piece.QUEEN_CASTLE) {
            int sideOffset = whiteToMove ? 0 : blackRankOffset;
            int rookOriginalSquare = castlingFiles[whiteToMove ? 1 : 3] + sideOffset;

            friendlyPieces[Piece.KING] &= ~(1L << to);
            currentPosition[to] = Piece.EMPTY;

            friendlyPieces[Piece.ROOK] &= ~(1L << (to - 1));
            currentPosition[to - 1] = Piece.EMPTY;

            friendlyPieces[Piece.KING] |= (1L << from);
            currentPosition[from] = Piece.KING;

            friendlyPieces[Piece.ROOK] |= (1L << rookOriginalSquare);
            currentPosition[rookOriginalSquare] = Piece.ROOK;
        }
        // Undo en passant
        else if (flag == Piece.EN_PASSANT) {
            friendlyPieces[Piece.PAWN] &= ~(1L << to);
            friendlyPieces[Piece.PAWN] |= (1L << from);
            currentPosition[from] = Piece.PAWN;
            currentPosition[to] = Piece.EMPTY;

            int capturedPawnSquare = whiteToMove ? to - 8 : to + 8;
            enemyPieces[Piece.PAWN] |= (1L << capturedPawnSquare);
            currentPosition[capturedPawnSquare] = Piece.PAWN;
        }
        // Undo normal move or capture
        else {
            friendlyPieces[movedPiece] &= ~(1L << to);
            friendlyPieces[movedPiece] |= (1L << from);
            currentPosition[from] = movedPiece;

            if (info.capturedPiece != Piece.EMPTY) {
                enemyPieces[info.capturedPiece] |= (1L << to);
                currentPosition[to] = info.capturedPiece;
            } else {
                currentPosition[to] = Piece.EMPTY;
            }
        }

        // Restore game state
        enPassantSquare = info.enPassantSquare;
        castlingRights = info.castlingRights;
        halfMoveClock = info.halfMoveClock;
        zobristHash = info.zobristHash;

        if (!whiteToMove) {
            fullMoveNumber--;
        }
    }

    public static int squareToIndex(String square) {
        int file = 'h' - square.charAt(0);
        int rank = square.charAt(1) - '1';
        return rank * 8 + file;
    }
    public static int fileToIndex(char file) {return 'h' - Character.toLowerCase(file);}
    public static char indexToFile(int index) {return (char)('a' + index);}
    public static String indexToSquare(int index){
        int rank = index / 8;
        int file = index % 8;
        char fileChar = (char) ('h' - file);
        char rankChar = (char) ('1' + rank);
        return "" + fileChar + rankChar;
    }
    @Override
    public String toString() {
        StringBuilder fen = new StringBuilder();

        // 1. Piece placement
        for (int rank = 7; rank >= 0; rank--) {
            int emptyCount = 0;
            for (int file = 7; file >=0 ; file--) {
                int square = rank * 8 + file;
                char pieceChar = getPieceChar(square);
                if (pieceChar == '.') {
                    emptyCount++;
                } else {
                    if (emptyCount > 0) {
                        fen.append(emptyCount);
                        emptyCount = 0;
                    }
                    fen.append(pieceChar);
                }
            }
            if (emptyCount > 0) fen.append(emptyCount);
            if (rank > 0) fen.append('/');
        }

        // 2. Turn
        fen.append(whiteToMove ? " w " : " b ");

        // 3. Castling rights
        StringBuilder castling = new StringBuilder();
        if ((castlingRights & 0b0001) != 0) castling.append("K");
        if ((castlingRights & 0b0010) != 0) castling.append("Q");
        if ((castlingRights & 0b0100) != 0) castling.append("k");
        if ((castlingRights & 0b1000) != 0) castling.append("q");
        fen.append(!castling.isEmpty() ? castling : "-").append(" ");

        // 4. En passant
        fen.append(enPassantSquare == -1 ? "-" : indexToSquare(enPassantSquare)).append(" ");

        // 5. Half move and full move
        fen.append(halfMoveClock).append(" ").append(fullMoveNumber);

        return fen.toString();
    }
    private char getPieceChar(int square) {
        long bit = 1L << square;

        // White
        if ((whiteBitboards[Piece.PAWN] & bit) != 0) return 'P';
        if ((whiteBitboards[Piece.KNIGHT] & bit) != 0) return 'N';
        if ((whiteBitboards[Piece.BISHOP] & bit) != 0) return 'B';
        if ((whiteBitboards[Piece.ROOK] & bit) != 0) return 'R';
        if ((whiteBitboards[Piece.QUEEN] & bit) != 0) return 'Q';
        if ((whiteBitboards[Piece.KING] & bit) != 0) return 'K';

        // Black
        if ((blackBitboards[Piece.PAWN] & bit) != 0) return 'p';
        if ((blackBitboards[Piece.KNIGHT] & bit) != 0) return 'n';
        if ((blackBitboards[Piece.BISHOP] & bit) != 0) return 'b';
        if ((blackBitboards[Piece.ROOK] & bit) != 0) return 'r';
        if ((blackBitboards[Piece.QUEEN] & bit) != 0) return 'q';
        if ((blackBitboards[Piece.KING] & bit) != 0) return 'k';

        return '.';
    }

    public boolean isInCheck() {
        long[] enemy = whiteToMove ? blackBitboards : whiteBitboards;
        long[] friendly = whiteToMove ? whiteBitboards : blackBitboards;

        long occupied = friendly[Piece.PAWN] | friendly[Piece.KNIGHT] |
                        friendly[Piece.BISHOP] | friendly[Piece.ROOK] |
                        friendly[Piece.QUEEN] | friendly[Piece.KING] |
                        enemy[Piece.PAWN] | enemy[Piece.KNIGHT] |
                        enemy[Piece.BISHOP] | enemy[Piece.ROOK] |
                        enemy[Piece.QUEEN] | enemy[Piece.KING];

        long king = friendly[Piece.KING];
        if (king == 0) return false;
        int kingSq = Long.numberOfTrailingZeros(king);

        long pawnAttackers = whiteToMove
                ? PrecomputedData.whitePawnAttacks[kingSq]
                : PrecomputedData.blackPawnAttacks[kingSq];

        if ((enemy[Piece.PAWN] & pawnAttackers) != 0) return true;
        if ((enemy[Piece.KNIGHT] & PrecomputedData.knightMoves[kingSq]) != 0) return true;

        long bishopAttacks = PrecomputedData.getBishopAttacks(kingSq, occupied);
        if ((bishopAttacks & (enemy[Piece.BISHOP] | enemy[Piece.QUEEN])) != 0) return true;

        long rookAttacks = PrecomputedData.getRookAttacks(kingSq, occupied);
        return (rookAttacks & (enemy[Piece.ROOK] | enemy[Piece.QUEEN])) != 0;

    }
    public static String generateChess960Shredder(){
         StringBuilder fen = new StringBuilder();

        char[] blackPieces = new char[8];
        Random rand = new Random();

        int lightSquareBishop = rand.nextInt(4) * 2; // 0, 2, 4, 6
        int darkSquareBishop = rand.nextInt(4) * 2 + 1; // 1, 3, 5, 7
        blackPieces[lightSquareBishop] = 'b';
        blackPieces[darkSquareBishop] = 'b';

        int knight1 = rand.nextInt(6);
        placeNextFreePiece(blackPieces, 'n', knight1);
        int knight2 = rand.nextInt(5);
        placeNextFreePiece(blackPieces, 'n', knight2);

        int queen = rand.nextInt(4);
        placeNextFreePiece(blackPieces, 'q', queen);

        int file1 = placeNextFreePiece(blackPieces, 'r', 0);
        placeNextFreePiece(blackPieces, 'k', 0);
        int file2 = placeNextFreePiece(blackPieces, 'r', 0);

        String black = String.valueOf(blackPieces);
        String blackPawns = "pppppppp";

        fen.append(black).append('/').append(blackPawns)
                .append("/8/8/8/8/")
        .append(blackPawns.toUpperCase()).append('/').append(black.toUpperCase());

        String rookFiles = "" + indexToFile(file1) + indexToFile(file2);
        fen.append(" w ").append(rookFiles.toUpperCase())
        .append(rookFiles).append(" - 0 1");

        return fen.toString();
    }
    private static int placeNextFreePiece(char[] pieces, char piece, int position){
         int finalPosition = 0;
         for(int i = 0; i < pieces.length; i++){
             if(finalPosition == position && pieces[i] == 0){
                 pieces[i] = piece;
                 return i;
             }
             else if(pieces[i] == 0) {
                 finalPosition++;
             }
         }
         return -1;
    }
    public static void printBoard(long bitboard) {
        long mask = 1L << 63;

        for (int i = 1; i <= 64; i++) {
            int bit = ((mask & bitboard) == 0) ? 0 : 1;

            if (i % 8 != 0) {
                System.out.print(bit + "  ");
            } else {
                System.out.println(bit);
            }
            mask = mask >>> 1;
        }
    }


}
