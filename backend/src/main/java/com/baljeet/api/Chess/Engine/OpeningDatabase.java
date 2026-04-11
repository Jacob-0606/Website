package com.baljeet.api.Chess.Engine;

import com.baljeet.api.Chess.Core.Board;
import com.baljeet.api.Chess.Core.MoveList;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;


@Component
public class OpeningDatabase {
    private static final HashMap<String, ArrayList<Integer>> openings = new HashMap<>();
    OpeningDatabase(){
        try{
            init();
        }
        catch (IOException ignored){
        }
    }
    public static void init() throws IOException {
        Resource resource = new ClassPathResource("opening/Book.txt");
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
            String fen = null;
            ArrayList<Integer> moves = null;
            String line;
            while ((line = reader.readLine()) != null) {
                if(Objects.equals(line.split(" ")[0], "pos")){
                    if(moves != null){
                        openings.put(fen,moves);
                        moves = null;
                    }
                    String[] elements = line.split(" ");
                    fen = elements[1]+ " " + elements[2]+ " " +elements[3];
                }
                else{
                    if(moves == null) moves = new ArrayList<>();
                    int from = Board.squareToIndex(line.substring(0, 2));
                    int to   = Board.squareToIndex(line.substring(2, 4));
                    moves.add(MoveList.packMove(from,to,0));

                }
            }
        }
    }
    public static int lookupPosition(MoveList possibleMoves, String FEN){
        String[] elements = FEN.split(" ");
        String fen = elements[0]+ " " + elements[1]+ " " +elements[2];

        if (!openings.containsKey(fen)) return  0;
        ArrayList<Integer> moves = openings.get(fen);
        int index = (int)(Math.random() * moves.size());

        int move = moves.get(index);

        for(int i = 0; i< possibleMoves.size();i++){
            if(MoveList.getFrom(possibleMoves.get(i)) == MoveList.getFrom(move)
                    && MoveList.getTo(possibleMoves.get(i)) == MoveList.getTo(move)) return possibleMoves.get(i);
        }
        return 0;
    }
}
