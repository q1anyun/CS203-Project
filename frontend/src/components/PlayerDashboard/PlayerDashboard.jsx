import React, { useState, useEffect } from 'react';
import axios from 'axios';
import { Box, Typography, Grid, Card, CardContent, Avatar, Divider } from '@mui/material';
import { PieChart, LineChart } from '@mui/x-charts';
import { useNavigate } from 'react-router-dom';
import TournamentItem from "../TournamentItem/TournamentItem";

const playerURL = import.meta.env.VITE_PLAYER_SERVICE_URL;
const tournamentURL = import.meta.env.VITE_TOURNAMENT_SERVICE_URL;
const eloURL = import.meta.env.VITE_ELO_SERVICE_URL;
const matchmakingURL = import.meta.env.VITE_MATCHMAKING_SERVICE_URL;

function PlayerDashboard() {
    const [playerDetails, setPlayerDetails] = useState({});
    const [recentMatches, setRecentMatches] = useState([]);
    const [liveTournaments, setLiveTournaments] = useState([]);
    const [recommendedTournaments, setRecommendedTournaments] = useState([]);
    const [uData, setUData] = useState([]);
    const [xLabels, setXLabels] = useState([]);
    const navigate = useNavigate();


    useEffect(() => {
        const fetchPlayerAndMatchData = async () => {
            const token = localStorage.getItem('token');
            if (!token) {
                navigate('/login');
                return;
            }

            try {
                // Fetch player details
                const playerResponse = await axios.get(`${playerURL}/currentPlayerById`, {
                    headers: { Authorization: `Bearer ${token}` },
                });
                setPlayerDetails(playerResponse.data || {});
                console.log(playerResponse.data);

                // Fetch chart data
                const chartResponse = await axios.get(`${eloURL}/chart/current`, {
                    headers: { Authorization: `Bearer ${token}` },
                });
                setUData(chartResponse.data.map((data) => data.elo));
                setXLabels(chartResponse.data.map((data) => data.date));

                // Fetch recent matches
                const matchResponse = await axios.get(`${matchmakingURL}/player/current/recent`, {
                    headers: { Authorization: `Bearer ${token}` },
                });
                setRecentMatches(matchResponse.data || []);

                // Fetch live tournaments
                const tournamentResponse = await axios.get(`${tournamentURL}/live/current`, {
                    headers: { Authorization: `Bearer ${token}` },
                });
                setLiveTournaments(tournamentResponse.data || []);

                //fetch reccomended Tournaments 
                const recommendedTournamentResponse = await axios.get(`${tournamentURL}/recommended`, {
                    headers: { Authorization: `Bearer ${token}` },
                });
                setRecommendedTournaments(recommendedTournamentResponse.data);
                console.log(recommendedTournaments);
            } catch (error) {
                if (error.response) {
                    const statusCode = error.response.status;
                    const errorMessage = error.response.data?.message || 'An unexpected error occurred';
                    navigate(`/error?statusCode=${statusCode}&errorMessage=${encodeURIComponent(errorMessage)}`);
                } else if (error.request) {
                    navigate(`/error?statusCode=0&errorMessage=${encodeURIComponent('No response from server')}`);
                } else {
                    navigate(`/error?statusCode=500&errorMessage=${encodeURIComponent('Error: ' + error.message)}`);
                }
            }
        };

        fetchPlayerAndMatchData();
    }, []);


    return (
        <Box p={2}>
            <Typography variant="header1" gutterBottom>Welcome back, {playerDetails.firstName}</Typography>
            <Typography variant="playerProfile2" sx={{ mb: 2, ml: 3, textAlign: "left" }} display={'block'}>
                What do you want to do today?
            </Typography>
            <Box p={3}>
                <Grid container spacing={3}>
                    <Grid item xs={12}>

                        <Card elevation={3} sx={{ p: 2, minHeight: 140 }}>
                            <Typography variant="header2">Your Statistics</Typography>
                            <Box sx={{ display: 'grid', gridTemplateColumns: '1fr 1fr', alignItems: 'center', justifyItems: 'center', height: '400px', marginTop: '-50px' }}>
                                <PieChart
                                    series={[
                                        {
                                            data: [
                                                { id: 0, value: playerDetails.totalWins, label: 'Wins', color: 'orange' },
                                                { id: 1, value: playerDetails.totalLosses, label: 'Losses', color: 'grey' },
                                            ]
                                        },
                                    ]}
                                    width={400}
                                    height={200}
                                    justifyContent='center'
                                    alignItems='center'
                                />
                                <LineChart
                                    width={500}
                                    height={300}
                                    series={[

                                        { data: uData, label: 'Elo Rating' },
                                    ]}
                                    xAxis={[{ scaleType: 'point', data: xLabels, ticks: false }]}
                                />
                            </Box>
                        </Card>
                    </Grid>

                    <Grid item xs={12}>

                        <Card elevation={3} sx={{ p: 2, minHeight: 140 }}>
                            <Typography variant="header2">Recommended Tournaments</Typography>
                            <Grid container spacing={2}>
                            {recommendedTournaments.map((tournament) => (
                                <Grid item xs={12} sm={6} md={4} key={tournament.id}>

                                    <Card sx={{ display: 'flex', flexDirection: 'column', height: '100%' }}
                                        onClick={() => navigate(`/player/tournaments/${tournament.id}`)} >

                                        <TournamentItem key={tournament.id} tournament={tournament} />
                                    </Card>

                                </Grid>))}
                                </Grid>


                        </Card>
                    </Grid>

                    <Grid item xs={12} md={6}>
                        <Card elevation={3} sx={{ p: 2, minHeight: 140}}>
                            <Typography variant="header2">Ongoing Tournaments</Typography>


                            <Box sx={{ p: 2, height: '100%' }}>

                                <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2 }}>
                                    {liveTournaments.length > 0 ? (
                                        liveTournaments.map((tournament, index) => (
                                            <Box
                                                key={index}
                                                sx={{
                                                    display: 'flex',
                                                    flexDirection: 'row',
                                                    alignItems: 'center',
                                                    padding: 2,
                                                    border: '1px solid #ddd',
                                                    borderRadius: 2,
                                                }}
                                                onClick={() => navigate(`/player/tournaments/${tournament.id}`)} // Navigate to tournament details
                                            >
                                                <CardContent>
                                                    <Typography variant="header2">{tournament.name}</Typography>
                                                    <Typography variant="body4" display='block'>Click here to view details</Typography>
                                                </CardContent>
                                            </Box>
                                        ))
                                    ) : (
                                        <Typography variant="playerProfile2" textAlign={'left'}>No ongoing tournaments found.</Typography>
                                    )}
                                </Box>
                            </Box>
                        </Card>
                    </Grid>

                    <Grid item xs={12} md={6}>
                        <Card elevation={3} sx={{ p: 2, minHeight: 140, }}>
                            <Typography variant="header2">Recent Matches</Typography>
                            {/* List recent matches */}
                            <Box sx={{ display: 'flex', flexDirection: 'row', flexWrap: 'wrap', gap: 2, p: 2, justifyContent: 'center', }}>
                                {recentMatches.length > 0 ? (
                                    recentMatches.map((match, index) => (
                                        <Card
                                            key={match.id}
                                            sx={{
                                                display: 'flex',
                                                flexDirection: 'column',
                                                p: 2,
                                                borderRadius: 2,
                                                flexGrow: 1,
                                                alignItems: 'center'
                                                
                                            }}
                                        >
                                            <Typography variant="header3">{match.tournament.name}</Typography>

                                            {/* Flexbox for Players and Winner */}
                                            <CardContent sx={{ display: 'flex', alignItems: 'flex-start',  }}>
                                                {/* Left Column for Players */}
                                                <Box sx={{ textAlign: 'left', alignItems: 'flex-start' }}>
                                                    {/* Player 1 */}
                                                    <Box sx={{ display: 'flex', alignItems: 'center', mb: 4 }}>
                                                        <Avatar
                                                            alt={`Player ${match.winnerId}`}
                                                            src={`../../../backend/player-service/profile-picture/player_${match.winnerId}.jpg`}
                                                            sx={{ mr: 1, backgroundColor: '#FDB068'}}
                                                        />
                                                        <Typography variant="header3">Player {match.winnerId}</Typography>
                                                    </Box>

                                                    {/* Player 2 */}
                                                    <Box sx={{ display: 'flex', alignItems: 'center', mb: 2 }}>
                                                        <Avatar
                                                            alt={`Player ${match.loserId}`}
                                                            src={`../../../backend/player-service/profile-picture/player_${match.loserId}.jpg`}
                                                            sx={{ mr: 1,  backgroundColor: '#FDB068'}}
                                                        />
                                                        <Typography variant="header3">Player {match.loserId}</Typography>
                                                    </Box>
                                                </Box>

                                                {/* Divider */}
                                                <Divider orientation="vertical" sx={{ height: '100px', ml: 5, mr: 8 }} />

                                                {/* Right Column for Winner */}
                                                <Box sx={{ flexShrink: 0, alignItems: 'center' }}>
                                                    <Typography variant="header3">Winner:</Typography>
                                                    <Box sx={{ mb: 2 }}>
                                                        <Avatar
                                                            alt={`Winner ${match.winnerId}`}
                                                            src={
                                                                match.winnerId
                                                                    ? `../../../backend/player-service/profile-picture/player_${match.winnerId}.jpg`
                                                                    : '/path/to/default-avatar.jpg'
                                                            }
                                                            sx={{
                                                                width: 56,
                                                                height: 56,
                                                                justifyContent: 'center',
                                                                alignContent: 'center',
                                                                backgroundColor: '#FCD34D', // gray-200
                                                            }}
                                                        />
                                                    </Box>
                                                    <Typography variant="header3">
                                                        {match.winnerId ? `Player ${match.winnerId}` : 'Pending'}
                                                    </Typography>
                                                </Box>
                                            </CardContent>
                                        </Card>
                                    ))
                                ) : (
                                    <Typography variant="playerProfile2" align="center">
                                        No recent matches available.
                                    </Typography>
                                )}
                            </Box>
                        </Card>
                    </Grid>
                </Grid>
            </Box>
        </Box>
    );
}

export default PlayerDashboard;

