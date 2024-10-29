// AdminTournamentDetails.js
import React, { useState, useEffect } from 'react';
import { Typography, Box, Button, Divider, Tabs, Tab } from '@mui/material';
import axios from 'axios';
import { useParams } from 'react-router-dom';
import TournamentDescription from './TournamentDescription';
import { useNavigate } from 'react-router-dom';
import Knockout from './Knockout'; // Update the import statement to reflect the new component name
import SwissBracket from './SwissBracket';


const baseURL = import.meta.env.VITE_TOURNAMENT_SERVICE_URL;
const baseURL2 = import.meta.env.VITE_MATCHMAKING_SERVICE_URL;

function AdminTournamentDetails() {
    const { id } = useParams();
    const [tournament, setTournament] = useState({});
    const [matches, setMatches] = useState([]);
    const [rounds, setRounds] = useState([]);
    const [selectedMatchId, setSelectedMatchId] = useState(null);
    const [selectedTeams, setSelectedTeams] = useState([]); // Store selected match teams
    const [winner, setWinner] = useState('');
    const [open, setOpen] = useState(false);


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
                if (response.data.type === 'KNOCKOUT') {
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
                    { id: match.player1 ? match.player1.id : 0, name: match.player1 ? match.player1.firstName : "Pending" },
                    { id: match.player2 ? match.player2.id : 0, name: match.player2 ? match.player2.firstName : "Pending" }
                ],
            });

            return acc;
        }, {});

        return Object.keys(groupedMatches).map((round) => ({
            title: round,
            seeds: groupedMatches[round],
        }));
    };

    const handleEditWinner = (matchId, teams) => {
        setSelectedMatchId(matchId);
        setSelectedTeams(teams);
        setOpen(true);
    };

    const handleCloseEdit = () => {
        setOpen(false);
    };

    const handleSaveWinner = async () => {
        try {
            await axios.put(`${baseURL2}/${selectedMatchId}/winner/${winner}`, {
                headers: { Authorization: `Bearer ${token}` },
            });
            setOpen(false);
            window.location.reload();
        } catch (error) {
            console.error('Error updating the winner:', error);
        }
    };

    const handleStart = async () => {
        if (tournament.currentPlayers < 2) {
            alert("Not enough players to start the tournament. Minimum required: 2");
            return;
        }

        try {
            const response = await axios.post(`${baseURL}/start/${tournament.id}`, null, {
                headers: { Authorization: `Bearer ${token}` },
            });

            if (response.status === 200) {
                alert("Tournament started successfully!");
                window.location.reload();
            }
        } catch (error) {
            console.error("Error starting the tournament:", error);
            alert("Failed to start the tournament.");
        }
    };

    const handleViewRegisteredPlayers = () => {
        navigate(`${window.location.pathname}/registeredplayers`);
    };



    return (
        <Box sx={{ padding: 2 }}>
            <TournamentDescription tournament={tournament} handleStart={handleStart} handleViewRegisteredPlayers={handleViewRegisteredPlayers} />

            <Typography variant="header2" marginLeft={'20px'}>Tournament Bracket</Typography>
            <Button variant="contained" color="primary" sx={{ marginLeft: '10px' }} onClick={() => navigate(`/admin/tournaments/leaderboard/${tournament.id}`)}>
                <Typography variant="body4">Check Leaderboard</Typography>
            </Button>
            <Divider sx={{ width: '80%', margin: '20px 0' }} />

            {/* Conditional Rendering Based on Tournament Type */}
            {tournament?.tournamentType?.id === 1 && (
                <>
        {console.log("Rendering Knockout bracket")}
                <Knockout
                    rounds={rounds}
                    handleEditWinner={handleEditWinner}
                    winner={winner}
                    setWinner={setWinner}
                    selectedTeams={selectedTeams}
                    open={open}
                    handleCloseEdit={handleCloseEdit}
                    handleSaveWinner={handleSaveWinner}
                />
                    </>
            )}

            {tournament?.tournamentType?.id === 2 && (
                  <>
        {console.log("Rendering Swiss bracket")}
                <SwissBracket 
                matches={matches}
                handleEditWinner={handleEditWinner}
                winner={winner}
                selectedTeams={selectedTeams}
                setWinner={setWinner}
                open={open}
                handleCloseEdit={handleCloseEdit}
                handleSaveWinner={handleSaveWinner} />
                </>
            )}
        </Box>
    );
}

export default AdminTournamentDetails;
