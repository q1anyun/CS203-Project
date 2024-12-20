import React, { useState, useEffect } from 'react';
import { Typography, Box, Divider } from '@mui/material';
import axios from 'axios';
import { useParams } from 'react-router-dom';
import TournamentDescription from './TournamentDescription';
import { useNavigate } from 'react-router-dom';
import Knockout from './Knockout';
import SwissBracket from './SwissBracket';
import useHandleError from '../Hooks/useHandleError';

const tournamentURL = import.meta.env.VITE_TOURNAMENT_SERVICE_URL;
const matchmakingURL = import.meta.env.VITE_MATCHMAKING_SERVICE_URL;

function AdminTournamentDetails() {
    const { id } = useParams();
    const [tournament, setTournament] = useState({});
    const [matches, setMatches] = useState([]);
    const [rounds, setRounds] = useState([]);

    const token = localStorage.getItem('token');
    const navigate = useNavigate();
    const handleError = useHandleError();

    useEffect(() => {
        const fetchTournamentDetails = async () => {
            try {
                const response = await axios.get(`${tournamentURL}/${id}`, {
                    headers: { Authorization: `Bearer ${token}` },
                });
                setTournament(response.data);

                const matchesResponse = await axios.get(`${matchmakingURL}/tournament/${id}`, {
                    headers: { Authorization: `Bearer ${token}` },
                }); 
                // Format rounds only if the tournament type is 'KNOCKOUT'
                if (response.data.tournamentType.typeName === 'Knockout') {
                   const formattedRounds = formatRounds(matchesResponse.data);
                    setRounds(formattedRounds); 
                    setMatches(matchesResponse.data);
                    
                }else {
                    setMatches(matchesResponse.data);
                }

            } catch (error) {
                handleError(error);
            }
        };

        fetchTournamentDetails();
    }, [id]);

    const formatRounds = (matches) => {
        const groupedMatches = matches.reduce((acc, match) => {
            if (!match || !match.roundType) return acc;

            const round = match.roundType.roundName;
            if (!acc[round]) {
                acc[round] = [];
            }

            acc[round].push({
                id: match.id,
                winnerId: match.winnerId,
                teams: [
                    { id: match.player1 ? match.player1.id : 0, name: match.player1 ? match.player1.firstName + " " + match.player1.lastName : "Pending" },
                    { id: match.player2 ? match.player2.id : 0, name: match.player2 ? match.player2.firstName + " " + match.player2.lastName: "Pending" }
                ],
            });

            return acc;
        }, {});

        return Object.keys(groupedMatches).map((round) => ({
            title: round,
            seeds: groupedMatches[round],
        }));
    };



    const handleStart = async () => {
        if (tournament.tournamentType.id === 2 && tournament.currentPlayers !== tournament.maxPlayers) {
            alert("Swiss tournament must be full before it can be started.");
            return;
        }

        if (tournament.currentPlayers < 2) {
            alert("Not enough players to start the tournament. Minimum required: 2");
            return;
        }

        try {
            const response = await axios.post(`${tournamentURL}/start/${tournament.id}`, null, {
                headers: { Authorization: `Bearer ${token}` },
            });

            if (response.status === 200) {
                alert("Tournament started successfully!");
                window.location.reload();
            }
        } catch (error) {
            alert("Failed to start the tournament.");
        }
    };

    const handleViewRegisteredPlayers = () => {
        navigate(`${window.location.pathname}/registeredplayers`, { state: { tournament } });
    };

    return (
        <Box sx={{ padding: 2 }}>
            <TournamentDescription tournament={tournament} handleStart={handleStart} handleViewRegisteredPlayers={handleViewRegisteredPlayers} />

            <Typography variant="header2" marginLeft={'20px'} >Tournament Bracket</Typography>

            <Divider sx={{ width: '80%', margin: '10px 0' }} />

            {/* Conditional Rendering Based on Tournament Type */}
            {tournament?.tournamentType?.id === 1 ? (
                <Knockout
                    rounds={rounds}
                />
            ) : tournament?.tournamentType?.id === 2 && tournament?.swissBracketId ? (
                <SwissBracket
                    matches={matches}
                    SwissBracketID={tournament.swissBracketId}
                />
            ) : (
                <Typography variant='playerProfile2'>No matches to display — Tournament has not started.</Typography>
            )}
        </Box>
    );
}

export default AdminTournamentDetails;
