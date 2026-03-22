package com.baljeet.api.Chess.Controllers;

import com.baljeet.api.Chess.GameLayers.GameService;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

@Controller
public class GameWebSocketController {

    private final GameService gameService;

    public GameWebSocketController(GameService gameService) {
        this.gameService = gameService;
    }


    @MessageMapping("/{gameID}.join")
    @SendTo("/topic/{gameID}")
    public ChessResponses.gameState joinGame(
            @DestinationVariable String gameID) {
        return gameService.getGameState(gameID, true)
                .orElseThrow(() -> new IllegalArgumentException("Bad join"));
    }

    @MessageMapping("/{gameID}.makeMove")
    @SendTo("/topic/{gameID}")
    public ChessResponses.gameState makeMove(
            @DestinationVariable String gameID,
            @Payload ChessRequests.makeMove request) {
        return gameService.makeMove(gameID, request)
                .orElseThrow(() -> new IllegalArgumentException("Bad move"));
    }
    @MessageMapping("/{gameID}.checkTimeout")
    @SendTo("/topic/{gameID}")
    public ChessResponses.gameState checkTimeout(
            @DestinationVariable String gameID) {
        return gameService.getGameState(gameID)
                .orElseThrow(() -> new IllegalArgumentException("Problem"));
    }
    
}
