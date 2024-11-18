import { useState, useEffect } from 'react';

function useMatchGrouping(knockoutMatches) {
    const [groupedRounds, setGroupedRounds] = useState([]);

    useEffect(() => {
        const groupMatchesByRound = () => {
            const grouped = knockoutMatches.reduce((acc, match) => {
                const roundName = match.roundType?.roundName || 'Unknown';
                if (!acc[roundName]) acc[roundName] = [];
                acc[roundName].push(match);
                return acc;
            }, {});

            return Object.keys(grouped).map(round => ({
                title: round,
                seeds: grouped[round].map(match => ({
                    id: match.id,
                    winnerId: match.winnerId,
                    teams: [
                        { id: match.player1?.id || 0, name: match.player1 ? `${match.player1.firstName} ${match.player1.lastName}` : "Pending" },
                        { id: match.player2?.id || 0, name: match.player2 ? `${match.player2.firstName} ${match.player2.lastName}` : "Pending" }
                    ],
                })),
            }));
        };

        setGroupedRounds(groupMatchesByRound());
    }, [knockoutMatches]);

    return groupedRounds;
}; 
export default useMatchGrouping; 