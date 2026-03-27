import { useState, useCallback, useMemo, useEffect, useRef, useReducer } from 'react';
import { getMoves } from "./chessAPI.js";

export const fenParser = (fen) => {
    const [fenString] = fen.toString().split(" ");
    const rows = fenString.split("/");
    const originalBoard = new Array(64).fill("");
    let originalSquare = 63;

    for (const row of rows) {
        for (const char of Array.from(row)) {
            if (/\d/.test(char)) {
                originalSquare -= parseInt(char);
            } else {
                originalBoard[originalSquare] = char;
                originalSquare--;
            }
        }
    }
    return originalBoard;
};

export const getPieceColor = (piece) => piece && (piece[0] === piece[0].toUpperCase() ? 'w' : 'b');

export const getPieceSvg = (piece) => {
    if (!piece) return null;
    const color = getPieceColor(piece);
    const pieceMap = { 'r': 'rook', 'n': 'knight', 'b': 'bishop', 'q': 'queen', 'k': 'king', 'p': 'pawn' };
    const pieceName = pieceMap[piece.toLowerCase()];
    return pieceName ? `chess/${pieceName}-${color}.svg` : null;
};

export const isMoveValid = (from, to, moves) => {
    const packedMove = (from | (to << 6));
    for (const move of moves) {
        if ((move & 0xFFF) === packedMove) return move;
    }
    return null;
};

export const useSquareInteractions = (uuid, board, onMove) => {
    const [selectedSquare, setSelectedSquare] = useState(null);
    const [moves, setMoves] = useState([]);

    const fetchMoves = useCallback(async (square) => {
        if (square === null) return;
        try {
            const response = await getMoves(square, uuid);
            const data = await response.json();
            setMoves(data.moves);
        } catch (error) {
            setMoves([]);
        }
    }, [uuid]);

    const handleSquareClick = async (index) => {
        if (selectedSquare === null) {
            setSelectedSquare(index);
            await fetchMoves(index);
        } else {
            const move = isMoveValid(selectedSquare, index, moves);
            if (move != null) {
                const success = await onMove(move);
                if (success) {
                    setSelectedSquare(null);
                    setMoves([]);
                }
            } else {
                setSelectedSquare(index);
                await fetchMoves(index);
            }
        }
    };

    return {
        selectedSquare,
        moves,
        handleSquareClick,
        setMoves,
        setSelectedSquare
    };
};

export const useChessTimer = (timeControl, gameData, dispatch, isOnline) => {
    const {gameState, joined, stop} = gameData;

    const parts = useMemo(() => timeControl.split(',').map(Number), [timeControl]);
    const [timeInSeconds, bonusTime = 0] = parts;
    const initialTime = timeInSeconds * 6000;
	const bonusTimeCenti = bonusTime * 100;

    const isFirstRender = useRef(true);

    const [timeState, timeDispatch] = useReducer(timeReducer, {         
        counterBlack: initialTime,
        counterWhite: initialTime });

    const { counterBlack, counterWhite } = timeState;
    useEffect(() => {
        if (isOnline) return;
        if (counterBlack <= 0) dispatch({type: 'SET_STOP', payload: "White won on time! ⏱️"});
        else if (counterWhite <= 0) dispatch({type: 'SET_STOP', payload: "Black won on time! ⏱️"});
    }, [counterBlack, counterWhite]);
    
    useEffect(() => {
        if (stop !== ChessResult.NO_RESULT || !joined) return;
        const turn = isWhite(gameState);
        let lastUpdate = Date.now();

        const interval = setInterval(() => {

            const now = Date.now();
            const delta = (now - lastUpdate) / 10; 
            lastUpdate = now;

            if (turn) {
                timeDispatch({type: 'SET_COUNTER_WHITE', payload: delta});
            } else {
                timeDispatch({type: 'SET_COUNTER_BLACK', payload: delta});
            }
        }, 10);

        return () => clearInterval(interval);
    }, [stop, gameState, joined]);

    useEffect(() => {
        if (isFirstRender.current) {
            isFirstRender.current = false;
            return;
        }
        if (isOnline) return;
        const turn = isWhite(gameState);
        if (turn) timeDispatch({type: 'ADD_BONUS_BLACK', payload: bonusTimeCenti});
        else timeDispatch({type: 'ADD_BONUS_WHITE', payload: bonusTimeCenti});
    }, [gameState])

   
    return {
       timeState,
       timeDispatch
    };
};

export const isWhite = (state) => {
    return state.fen.split(" ")[1] === 'w';
}
export const currentTurn = (state) => {
    return state.fen.split(" ")[1];
}
const sounds = {
  move: new Audio('/chess_sounds/move-self.mp3'),
  capture: new Audio('/chess_sounds/capture.mp3'),
  check: new Audio('/chess_sounds/move-check.mp3'),
};
const getFlag = (move) => {return (move & 0xf000) >> 12;}

const playChessSound = (move, check) => {
  if(move === -1) return;
  const flag = getFlag(move);
  let audio;
  
  if (flag === 10) {
    audio = sounds.capture;
  } else {
    audio = sounds.move;
  }
  if(check)
    audio = sounds.check;

  if (audio) {
    audio.currentTime = 0;
    audio.play();
  }
};
export const updateStatesAfterMove = (dispatch, setBoard, gameState) => {
    setBoard(fenParser(gameState.fen));
    dispatch({type: 'SET_GAME_STATE', payload: gameState});
    dispatch({type: 'SET_LAST_MOVE', payload: gameState.lastMove});
    playChessSound(gameState.lastMove, gameState.check);
}

export const ChessMode = Object.freeze({
  LOCAL: 'LOCAL',
  ENGINE: 'ENGINE',
  ONLINE: 'ONLINE'
});
export const ChessVariation = Object.freeze({
  STANDARD: 'STANDARD',
  CHESS960: 'CHESS960'
});
export const ChessResult = Object.freeze({
  NO_RESULT: 'NO_RESULT',
  DRAW: 'DRAW',
  WINNER_WHITE: 'WINNER_WHITE',
  WINNER_BLACK: 'WINNER_BLACK'
});
export const gameReducer = (state, action) => {
    switch (action.type) {
        case 'SET_GAME_STATE':
            return { ...state, gameState: action.payload };
        case 'SET_JOINED':
            return { ...state, joined: action.payload };
        case 'SET_STOP':
            return { ...state, stop: action.payload };
        case 'SET_LAST_MOVE':
            return { ...state, lastMove: action.payload };
        default:
            return state;
    }
};
export const timeReducer = (state, action) => {
    switch (action.type) {
        case 'SET_COUNTER_BLACK':
            return { ...state, counterBlack: state.counterBlack - action.payload };
        case 'SET_COUNTER_WHITE':
            return { ...state, counterWhite: state.counterWhite - action.payload };
        case 'ADD_BONUS_BLACK': 
            return { ...state, counterBlack: state.counterBlack + action.payload };
        case 'ADD_BONUS_WHITE':
            return { ...state, counterWhite: state.counterWhite + action.payload };
        case 'SYNC_TIMERS':
            return { ...state, counterWhite: action.payload.white,
                               counterBlack: action.payload.black };
        default:
            return state;
    }
}