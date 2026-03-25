package com.baljeet.api.Chess.Controllers;


import java.util.ArrayList;

public class ChessResponses {
    public static class StartGame {
        public String gameID;
    }
    public static class GameInfo {
        public String gameID;
        public long timeLeft;
        public long increment;
        public boolean white;
    }
    public static class getMovesResponse{
        public ArrayList<Integer> moves;
    }
    public static class gameState{
        public String fen;
        public boolean check;
        public boolean checkmate;
        public boolean draw;
        public int lastMove;

        public long blackTime;
        public long whiteTime;

       public GameResult result;
    }
}
