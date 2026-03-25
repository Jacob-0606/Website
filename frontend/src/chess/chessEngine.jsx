import { useState, useCallback, useEffect, useRef, useMemo } from 'react';
import { makeMove, engineMakeMove } from "./chessAPI.js";
import { fenParser, currentTurn } from "./chessCommon.jsx";

export const useLocalChess = (uuid, isEngineMode, playerColor
                            , bonusTime, gameData, dispatch, timeData, timeDispatch
) => {
    const { gameState, joined, stop } = gameData;
    const {counterBlack, counterWhite} = timeData;
    const [board, setBoard] = useState(() => fenParser(gameState.fen));
    const isCalculating = useRef(false); 


    const doMove = useCallback(async (move, counters) => {
        try {
            const response = await makeMove(move, uuid);
            const newState = await response.json();
            setBoard(fenParser(newState.fen));
            dispatch({type: 'SET_GAME_STATE', payload: newState});
            dispatch({type: 'SET_LAST_MOVE', payload: newState.lastMove});
            return true;
        } catch (error) {
            console.error("Move failed", error);
            return false;
        }
    }, [uuid]);

    useEffect(() => {
        const engineColor = playerColor === "w" ? "b" : "w";
        const nextTurn = currentTurn(gameState);

        if (isEngineMode && nextTurn === engineColor && !gameState.checkmate && !gameState.draw && !isCalculating.current) {
            isCalculating.current = true;
            const time = engineColor === "w" ? counterWhite : counterBlack;
            engineMakeMove(time * 10, bonusTime * 1000, uuid).then(async (res) => {
                const newState = await res.json();
                setBoard(fenParser(newState.fen));
                dispatch({type: 'SET_GAME_STATE', payload: newState});
                dispatch({type: 'SET_LAST_MOVE', payload: newState.lastMove});
                isCalculating.current = false;
            }).catch(() => {
                isCalculating.current = false;
            });
        }
    }, [gameState, isEngineMode, playerColor, uuid]);

    return { gameState, board, doMove};
};