import React, { useMemo, useState } from 'react';
import { getPieceSvg, useSquareInteractions } from "./chessCommon.jsx";

const Square = React.memo(({ index, piece, isLightSquare, isSelected, isTarget,
                             onSelect, onDragStart, onDrop, rotationDeg, isLastMove }) => {
    const isCapture = isTarget && !!piece;
    const pieceSvg = getPieceSvg(piece);
    let squareColor =  isLightSquare ?
                                     'bg-gray-300' : 'bg-gray-600';
    if (isSelected) 
        squareColor = 'bg-emerald-400';
    else if (isCapture) 
        squareColor = 'bg-rose-500';

    if(isLastMove)
        squareColor = 'bg-orange-300';
    return (
        <div 
            className={`flex items-center justify-center rounded-md ${squareColor} relative aspect-square`}
            onClick={() => onSelect(index)}
            onDragOver={(e) => e.preventDefault()} 
            onDrop={() => onDrop(index)}
        >
            {isTarget && !piece && (
                <div className="absolute w-1/4 h-1/4 rounded-full bg-black opacity-30"></div>
            )}
            {pieceSvg && (
                <img 
                    src={pieceSvg} 
                    alt={piece} 
                    draggable={true} 
                    onDragStart={() => onDragStart(index)}
                    className="w-full h-full object-contain"
                    style={{ transform: `rotate(${rotationDeg}deg)` }}
                />
            )}
        </div>
    );
});

export const ChessBoardUI = ({ board, uuid, onMove, rotation, lastMove }) => {
    const { selectedSquare, moves, handleSquareClick } = useSquareInteractions(uuid, board, onMove);

    const rotationDeg = useMemo(() => {
        return rotation;
    }, [rotation]);

    const targets = useMemo(() => moves.map(m => (m >> 6) & 0x3F), [moves]);
    
    const lastTo = useMemo(() => {
        if(lastMove === -1)
            return -1;
        return (lastMove & 0xfc0) >> 6} ,[lastMove]);
    const lastFrom = useMemo(() => {
        if(lastMove === -1)
            return -1;
        return lastMove & 0x3f} ,[lastMove]);

    const handleDragStart = (index) => {
        if (selectedSquare !== index) {
            handleSquareClick(index);
        }
    };
    const handleDrop = (index) => {
        handleSquareClick(index);
    };

    return (
        <div className="bg-gray-800 shadow-2xl rounded-lg overflow-hidden w-full max-w-lg aspect-square mx-auto">
            <div 
                className="grid grid-cols-8 grid-rows-8 w-full h-full"
                style={{ transform: `rotate(${rotationDeg}deg)` }}
            >
                {Array.from({ length: 64 }, (_, index) => {
                    const rowIndex = Math.floor(index / 8);
                    const colIndex = index % 8;
                    const flippedIndex = 63 - (rowIndex * 8 + colIndex);
                    
                    return (
                        <Square
                            key={index}
                            index={flippedIndex}
                            piece={board[flippedIndex]}
                            isLightSquare={(rowIndex + colIndex) % 2 === 0}
                            isSelected={selectedSquare === flippedIndex}
                            isTarget={targets.includes(flippedIndex)}
                            onSelect={handleSquareClick}
                            onDragStart={handleDragStart}
                            onDrop={handleDrop}
                            rotationDeg={rotationDeg}
                            isLastMove={(flippedIndex === lastTo) || (flippedIndex === lastFrom)}
                        />
                    );
                })}
            </div>
        </div>
    );
};
export const TimerCard = ({ time, player, isCurrentTurn }) => {
    const color = player === "White" ? "bg-gray-300" : "bg-gray-600";
    const totalSeconds = Math.max(time, 0) / 100;
    const displayTime = totalSeconds < 60 
        ? `${totalSeconds.toFixed(1)}s` 
        : `${Math.floor(totalSeconds / 60)}:${String(Math.floor(totalSeconds % 60)).padStart(2, '0')}`;

    return (
        <div className={`${isCurrentTurn ? "ring-4 ring-blue-500" : ""} ${color} rounded-xl p-4 w-32 text-center`}>
            <h3 className="text-sm font-bold opacity-70 uppercase">{player}</h3>
            <p className="text-2xl font-mono font-black">{displayTime}</p>
        </div>
    );
};

export const EmbedLink = ({ link, maxChars = 60, label = "" }) => {
  const [copied, setCopied] = useState(false);
  const displayLink = link.length > maxChars ? link.slice(0, maxChars) + "..." : link;

  const handleCopy = async () => {
    try {
      await navigator.clipboard.writeText(link);
      setCopied(true);
    } catch (err) {
      console.error("Failed to copy:", err);
    }
  };

  return (
    
  <div className="p-3 rounded-2xl bg-gray-200 max-w-md">
    {label && (
      <p className="text-sm font-medium text-gray-700 mb-2">{label}</p>
    )}

    <div className="flex items-center justify-between gap-3">
      <a
        href={link}
        target="_blank"
        rel="noopener noreferrer"
        title={link}
        className="text-gray-800 font-medium hover:underline flex-1 min-w-0 truncate"
      >
        {displayLink}
      </a>

      <button
        onClick={handleCopy}
        className={`px-3 py-1.5 text-sm font-semibold rounded-lg transition-colors duration-200 shrink-0 ${
          copied
            ? "bg-gray-300 text-white"
            : "bg-gray-800 text-white hover:bg-gray-700"
        }`}
      >
        Copy
      </button>
    </div>
  </div>
);
};


export const GameOverModal = ({ message }) => (
    <div className="absolute center bg-gray-200 p-8 rounded-xl text-center shadow-2xl">
            <h2 className="text-3xl font-bold text-gray-800 mb-4">{message}</h2>
                <button
                    onClick={() => window.location.reload()}
                    className="mt-4 px-6 py-3 bg-gray-600 text-white font-semibold rounded-lg hover:bg-gray-300 transition duration-200"
                >
            Restart Game
        </button>
    </div>
);
