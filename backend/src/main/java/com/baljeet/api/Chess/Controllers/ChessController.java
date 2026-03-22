package com.baljeet.api.Chess.Controllers;


import com.baljeet.api.Chess.GameLayers.GameService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("api/chess")
public class ChessController {

    GameService gameService;
    ChessController(GameService gameService){
        this.gameService = gameService;
    }

    @PutMapping("/start")
    public ResponseEntity<ChessResponses.StartGame> startGame(
            @RequestBody ChessRequests.StartGame request){
       return gameService.startGame(request)
               .map(ResponseEntity::ok)
               .orElse(ResponseEntity.badRequest().build());
    }
    @GetMapping("/{gameID}/getMoves")
    public ResponseEntity<ChessResponses.getMovesResponse> getMoves(
            @PathVariable String gameID,
            @RequestParam String square){
        return gameService.getMovesSquare(gameID, Integer.parseInt(square))
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.badRequest().build());
    }

    @PostMapping("/{gameID}/makeMove")
    public ResponseEntity<ChessResponses.gameState> makeMove(
            @PathVariable String gameID,
            @RequestBody ChessRequests.makeMove request){
        return gameService.makeMove(gameID, request)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.badRequest().build());
    }
    @PostMapping("/{gameID}/makeEngineMove")
    public ResponseEntity<ChessResponses.gameState> makeEngineMove(
            @PathVariable String gameID,
            @RequestBody ChessRequests.engineMakeMove request){
        return gameService.makeEngineMove(gameID,request)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.badRequest().build());
    }
    @GetMapping("/{gameID}/getGameState")
    public ResponseEntity<ChessResponses.gameState> getGameState(
            @PathVariable String gameID){
        return gameService.getGameState(gameID)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.badRequest().build());
    }
    @GetMapping( "/{gameID}/getGameInfo")
    public ResponseEntity<ChessResponses.GameInfo> getGameInfo(
            @PathVariable String gameID){
        return gameService.getGameInfo(gameID)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.badRequest().build());
    }
    @DeleteMapping("/{gameID}/deleteGame")
    public ResponseEntity<Boolean> deleteGame(
            @PathVariable String gameID
    ){
    return gameService.deleteGame(gameID)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.badRequest().build());

    }

}
