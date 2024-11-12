import React, { useState, useEffect } from 'react';
import { Typography, Box, Divider } from '@mui/material';
import axios from 'axios';
import { useParams } from 'react-router-dom';
import TournamentDescription from './TournamentDescription';
import Knockout from './Knockout';
import SwissBracket from './SwissBracket';
import { useNavigate } from 'react-router-dom';

const tournamentURL = import.meta.env.VITE_TOURNAMENT_SERVICE_URL;
const matchmakingURL = import.meta.env.VITE_MATCHMAKING_SERVICE_URL;

function TournamentDetails() {
    const { id } = useParams();
    const [tournament, setTournament] = useState({});
    const [rounds, setRounds] = useState([]);
    const [matches, setMatches] = useState([]);
    const navigate = useNavigate();
    const token = localStorage.getItem('token');

    useEffect(() => {
        const fetchTournamentDetails = async () => {
            try {
                const response = await axios.get(`${tournamentURL}/${id}`, {
                    headers: { Authorization: `Bearer ${token}` },
                });
                setTournament(response.data);
                console.log(response.data);

                const matchesResponse = await axios.get(`${matchmakingURL}/tournament/${id}`, {
                    headers: { Authorization: `Bearer ${token}` },
                });
                console.log(matchesResponse.data);

                // Format rounds only if the tournament type is 'KNOCKOUT'
                if (response.data.tournamentType.typeName === 'Knockout') {
                    const formattedRounds = formatRounds(matchesResponse.data);
                    setRounds(formattedRounds);
                } else {
                    setMatches(matchesResponse.data);
                }

            } catch (error) {
                if (error.response) {
                    const statusCode = error.response.status;
                    const errorMessage = error.response.data?.message || 'An unexpected error occurred';
                    navigate(`/error?statusCode=${statusCode}&errorMessage=${encodeURIComponent(errorMessage)}`);
                } else if (err.request) {
                    navigate(`/error?statusCode=0&errorMessage=${encodeURIComponent('No response from server')}`);
                } else {
                    navigate(`/error?statusCode=500&errorMessage=${encodeURIComponent('Error: ' + err.message)}`);
                }
            }
        };

        fetchTournamentDetails();
    }, [id]);


    const formatRounds = (matches) => {

        const groupedMatches = matches.reduce((acc, match, index) => {

            // Check if match and roundType are not null before accessing roundName
            if (!match || !match.roundType) {
                return acc;  // Skip this match if roundType is undefined or null
            }

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

        const formattedRounds = Object.keys(groupedMatches).map((round) => ({
            title: round,
            seeds: groupedMatches[round],
        }));
        return formattedRounds;
    };

    

    return (
        <Box sx={{ padding: 2 }}>
            <TournamentDescription tournament={tournament} />
            {/* Divider added here */}
            <Typography variant="header2" marginLeft={'20px'} >Tournament Bracket</Typography>
            <Divider sx={{ width: '80%', margin: '10px 0' }} />
            {/* Conditional Rendering Based on Tournament Type */}
            {tournament?.status === 'UPCOMING' ? (
                <Typography variant='playerProfile2' marginLeft={'20px'}>No matches to display — Tournament has not started.</Typography>
            ) : (
                <>
                    {tournament?.tournamentType?.id === 1 ? (
                        <Knockout rounds={rounds} />
                    ) : tournament?.tournamentType?.id === 2 && tournament?.swissBracketId ? (
                        <SwissBracket matches={matches} SwissBracketID={tournament.swissBracketId} />
                    ) : null}
                </>
            )}
        </Box>
    );
}

export default TournamentDetails;
