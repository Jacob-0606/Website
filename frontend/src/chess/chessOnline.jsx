import { useState, useEffect, useRef} from 'react';
import SockJS from 'sockjs-client';
import { Client } from '@stomp/stompjs';
import { fenParser, currentTurn, ChessResult} from "./chessCommon.jsx";

export const useOnlineChess = (uuid, joinWithID, playerColor, gameData, dispatch
                            , timeState, timeDispatch
) => {
    const {counterBlack, counterWhite} = timeState;
    const { gameState, joined, stop } = gameData;
    const [board, setBoard] = useState(() => fenParser(gameState.fen));
    const stompClientRef = useRef(null);

    useEffect(() => {
        const socket = new SockJS(`${import.meta.env.VITE_API}/ws`);
        const stompClient = new Client({
            webSocketFactory: () => socket,
            onConnect: () => {
                stompClient.subscribe(`/topic/${uuid}`, (response) => {
                    const body = JSON.parse(response.body);
                    dispatch({type: 'SET_GAME_STATE', payload: body});
                    setBoard(fenParser(body.fen));
                    dispatch({type: 'SET_LAST_MOVE', payload: body.lastMove});
                    dispatch({type: 'SET_JOINED', payload: true});
                    timeDispatch({type: 'SYNC_TIMERS',
                         payload: {black: body.blackTime / 10, white: body.whiteTime / 10}});
                    console.log("Received update", body);
                    
                    if (body.blackTime <= 0) dispatch({type: 'SET_STOP', payload: "White won on time! ⏱️"});
                    else if (body.whiteTime <= 0) dispatch({type: 'SET_STOP', payload: "Black won on time! ⏱️"});
                });
                if (joinWithID) {
                    stompClient.publish({ destination: `/app/${uuid}.join`, body: JSON.stringify({}) });
                }
            },
        });
        stompClient.activate();
        stompClientRef.current = stompClient;
        return () => stompClient.deactivate();
    }, [uuid]);

    const doMove = (move, counters) => {
        if (stompClientRef.current?.connected && playerColor === currentTurn(gameState) && joined) {
            stompClientRef.current.publish({
                destination: `/app/${uuid}.makeMove`,
                body: JSON.stringify({ move, blackTime: counters.black, whiteTime: counters.white }),
            });
            return true;
        }
        return false;
    };
  
    useEffect(() => {
        if ((counterBlack <= 0 || counterWhite <= 0) && stompClientRef.current?.connected && stop === ChessResult.NO_RESULT) {
                stompClientRef.current.publish({
                    destination: `/app/${uuid}.checkTimeout`,
                });
            }

    }, [counterBlack, counterWhite, stop]);

    return { gameState, board, doMove};
};