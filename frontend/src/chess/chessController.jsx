import React, { useState, useMemo, useEffect, useReducer } from 'react';
import { useLocalChess } from './chessEngine.jsx';
import { useOnlineChess } from './chessOnline.jsx';
import {TimerCard, ChessBoardUI, GameOverModal, EmbedLink} from './chessUI.jsx';
import { useChessTimer, isWhite, ChessMode, gameReducer, ChessResult } from './chessCommon.jsx';

const ChessGame = ({ initialState, timeControl, gameMode, uuid, playerColor, joinWithID }) => {
    const [timeInSeconds, bonusTime] = useMemo(() => timeControl.split(',').map(Number), [timeControl]);
        
    const [rotation, setRotation] = useState(playerColor === "w" ? 0 : 180);
    const [rotationMode, setRotationMode] = useState(gameMode === ChessMode.LOCAL ? "rotate" : "fixed");
    
    const isOnline = gameMode === ChessMode.ONLINE;

   const [gameData, dispatch] = useReducer(gameReducer, {
        gameState: initialState,
        joined: gameMode !== ChessMode.ONLINE,
        stop: ChessResult.NO_RESULT,
        lastMove: -1
        });
    const { gameState, joined, stop, lastMove } = gameData;

    const { timeState, timeDispatch
     } = useChessTimer(timeControl, gameData, dispatch, isOnline);
    const {counterBlack, counterWhite} = timeState;

    const game = isOnline ? useOnlineChess(uuid, joinWithID, playerColor
                                            , gameData, dispatch, timeState, timeDispatch) : 
                            useLocalChess(uuid, gameMode === ChessMode.ENGINE, playerColor
                                            , bonusTime, gameData, dispatch, timeState, timeDispatch);


    useEffect(() => {
        if (gameState.result === ChessResult.NO_RESULT) return;
        if (gameState.checkmate) dispatch({type: 'SET_STOP', payload: isWhite(gameState) ? "Black Won!" : "White Won!"});
        if (gameState.draw) dispatch({type: 'SET_STOP', payload: "Draw!"});
    }, [gameState]);

    useEffect(() => {
        if (rotationMode === "rotate") 
            setRotation(isWhite(gameState) ? 0 : 180);
        else 
            setRotation(playerColor === "w" ? 0 : 180);
    }, [gameState, rotationMode, playerColor]);

    return (
        <div className="chess-layout">
            <div className="relative flex items-center justify-center">
            <div className="flex flex-col items-center gap-4">
               <div className={`flex ${rotation === 180 ? 'flex-col-reverse' : 'flex-col'} items-center mr-8`}> 
                    <TimerCard 
                        time={counterBlack} 
                        player="Black"
                        isCurrentTurn={!isWhite(gameState)} 
                    />
                    <div className="h-4"/>
                    <TimerCard 
                        time={counterWhite} 
                        player="White"
                        isCurrentTurn={isWhite(gameState)} 
                    />
            </div>
            {!joined && (
                <div className="mt-4"> 
                <EmbedLink link={`${import.meta.env.VITE_DOMAIN}/chess?gameID=${uuid}`}
                           maxChars={8} 
                           label={'Invite a friend:'}
                           />
                           </div>)}
            </div>
            
            <ChessBoardUI
                uuid={uuid} 
                board={game.board}
                onMove={(move) => game.doMove(move, { black: counterBlack, white: counterWhite })}
                rotation={rotation}
                lastMove={lastMove}
            />
        </div>
            {stop !== ChessResult.NO_RESULT && 
            <div className = "absolute inset-0 bg-opacity-50 flex items-center justify-center z-10">
                <GameOverModal message={stop} />
            </div>}
            </div>
    );
};

export default ChessGame;