import React, { useState, useEffect } from 'react';
import { Typography, Box, Button, IconButton, Dialog, Select, MenuItem, DialogTitle, DialogContent, DialogActions, Chip, Divider } from '@mui/material';
import axios from 'axios';
import { useParams } from 'react-router-dom';
import TournamentDescription from './TournamentDescription';
import { useNavigate } from 'react-router-dom';
import Knockout from './Knockout';
import SwissBracket from './SwissBracket';

const baseURL = import.meta.env.VITE_TOURNAMENT_SERVICE_URL;
const baseURL2 = import.meta.env.VITE_MATCHMAKING_SERVICE_URL;


function TournamentDetails() {
    const { id } = useParams();
    const [tournament, setTournament] = useState({});
    const [rounds, setRounds] = useState([]);
    const [selectedTeams, setSelectedTeams] = useState([]); // Store selected match teams
    const[matches, setMatches] = useState([]); 




    const statusColorMap = {
        LIVE: 'success',
        UPCOMING: 'warning',
        EXPIRED: 'default',
    };

    const token = localStorage.getItem('token');
    const navigate = useNavigate();

    useEffect(() => {
        const fetchTournamentDetails = async () => {
            try {
                const response = await axios.get(`${baseURL}/${id}`, {
                    headers: { Authorization: `Bearer ${token}` },
                });
                setTournament(response.data);
                console.log(response.data);

                const matchesResponse = await axios.get(`${baseURL2}/tournament/${id}`, {
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
                console.error('Error fetching tournament details:', error);
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
                    { id: match.player1 ? match.player1.id : 0, name: match.player1 ? match.player1.firstName : "Pending" },  // Provide fallback for firstName
                    { id: match.player2 ? match.player2.id : 0, name: match.player2 ? match.player2.firstName : "Pending" }  // Provide fallback for firstName
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
export default TournamentDetails;
