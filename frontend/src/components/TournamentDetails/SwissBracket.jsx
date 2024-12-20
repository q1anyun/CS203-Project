import React, { useState, useEffect } from 'react';
import { CardContent, Typography, Box, Divider, Grid, Avatar, Tab, Tabs } from '@mui/material';
import axios from 'axios';
import Knockout from './Knockout';
import defaultProfilePic from '../../assets/default_user.png';
import SwissStandings from './SwissStandings';
import useMatchGrouping from '../Hooks/useMatchGrouping';
import useSwissData from '../Hooks/useSwissData';

const baseURL = import.meta.env.VITE_TOURNAMENT_SWISSBRACKET_URL;
const playerURL = import.meta.env.VITE_PLAYER_SERVICE_URL;
const swissStandingURL = import.meta.env.VITE_TOURNAMENT_SWISSSTANDING_URL;


function SwissBracket({ matches, SwissBracketID }) {

    const [tabValue, setTabValue] = useState('swiss');
    const [swissMatches, setSwissMatches] = useState([]);
    const [knockoutMatches, setKnockoutMatches] = useState([{}]);
    const [playersWithPhotos, setPlayersWithPhotos] = useState({});
    const groupedRounds = useMatchGrouping(knockoutMatches); 
    const {swissStandings, swissRoundDetails} = useSwissData(SwissBracketID); 


    useEffect(() => {
        setSwissMatches(matches.filter(match => match.swissRoundNumber !== null));
        setKnockoutMatches(matches.filter(match => match.swissRoundNumber === null));
    }, [matches]);

    useEffect(() => {
        const fetchProfilePhotos = async (matches) => {
            const allPlayers = matches.reduce((acc, match) => {
                if (match.player1) acc.push(match.player1);
                if (match.player2) acc.push(match.player2);
                return acc;
            }, []);

            const playersWithPhotos = await attachProfilePhotos(allPlayers);
            setPlayersWithPhotos(playersWithPhotos.reduce((acc, player) => {
                acc[player.id] = player.profilePhoto;
                return acc;
            }, {}));
        };

        fetchProfilePhotos([...swissMatches, ...knockoutMatches]);
    }, [swissMatches, knockoutMatches]);

    const attachProfilePhotos = async (players) => {
        const token = localStorage.getItem('token');
        return await Promise.all(
            players.map(async (player) => {
                try {
                    const profilePictureResponse = await axios.get(`${playerURL}/photo/${player.id}`, {
                        headers: { Authorization: `Bearer ${token}` },
                        responseType: 'blob',
                    });
                    const imageUrl = URL.createObjectURL(profilePictureResponse.data);
                    return { ...player, profilePhoto: imageUrl };
                } catch {
                    // If photo fetch fails, add a default image or handle as needed
                    return { ...player, profilePhoto: defaultProfilePic };
                }
            })
        );
    };

    const matchesByRound = swissMatches.reduce((acc, match) => {
        const roundNumber = `Round ${match.swissRoundNumber || 'Unknown'}`;
        acc[roundNumber] = acc[roundNumber] || [];
        acc[roundNumber].push(match);
        return acc;
    }, {});

    return (
        <Box sx={{ padding: 2 }}>

            <Tabs value={tabValue} onChange={(e, newValue) => setTabValue(newValue)} textColor="primary" indicatorColor="primary">
                <Tab label="Swiss" value="swiss" />
                <Tab label="Swiss Standings" value="swiss standing" />
                <Tab label="Knockout" value="knockout" />
            </Tabs>

            <Divider sx={{ my: 1 }} />


            {tabValue === 'swiss' && Object.keys(matchesByRound).length > 0 ? (
                <>
                    <Typography variant='header2'>Total Rounds: {swissRoundDetails.numberOfRounds}</Typography>
                    {Object.entries(matchesByRound).slice().reverse().map(([roundName, roundMatches]) => (
                        <Box key={roundName} sx={{
                            marginBottom: 2,
                            border: roundName === `Round ${swissRoundDetails.currentRound}` ? '4px solid black' : '1px solid white',
                            borderRadius: '4px',
                            backgroundColor: roundName === `Round ${swissRoundDetails.currentRound}` ? '#f0f0f0' : 'white'
                        }}>
                            <CardContent>
                                <Typography variant="header3">
                                    {roundName === `Round ${swissRoundDetails.currentRound}`
                                        ? `${roundName} - Current Round`
                                        : roundName}
                                </Typography>

                                <Grid container spacing={2}>
                                    {roundMatches.map((match) => (
                                        <Grid item xs={12} sm={6} md={3} key={match.id}>
                                            <Box
                                                sx={{
                                                    display: 'flex',
                                                    flexDirection: 'column',
                                                    p: 2,
                                                    backgroundColor: 'background.paper',
                                                    border: '1px solid',
                                                    borderColor: 'divider',
                                                    borderRadius: 2,
                                                }}
                                            >
                                                <Box sx={{
                                                    display: 'flex', alignItems: 'center', mb: 4, backgroundColor: match.winnerId === match.player1.id ? 'green' : 'background.paper',
                                                    p: 1, borderRadius: 1
                                                }}>
                                                    <Avatar
                                                        alt={`${match.player1?.firstName}`}
                                                        src={playersWithPhotos[match.player1?.id] || defaultProfilePic}
                                                        sx={{ mr: 1 }}
                                                    />
                                                    <Typography variant="body4" style={{ color: match.winnerId === match.player1.id ? 'white' : 'black' }}>
                                                        {`${match.player1?.firstName} ${match.player1?.lastName}`}
                                                    </Typography>
                                                </Box>

                                                <Box sx={{
                                                    display: 'flex',
                                                    alignItems: 'center',
                                                    mb: 2,
                                                    backgroundColor: match.winnerId === match.player2.id ? 'green' : 'background.paper',
                                                    p: 1,
                                                    borderRadius: 1,
                                                }}>
                                                    <Avatar
                                                        alt={`${match.player2?.firstName}`}
                                                        src={playersWithPhotos[match.player2?.id] || defaultProfilePic}
                                                        sx={{ mr: 1 }}
                                                    />
                                                    <Typography variant="body4" style={{ color: match.winnerId === match.player2.id ? 'white' : 'black' }}>
                                                        {`${match.player2?.firstName} ${match.player2?.lastName}`}
                                                    </Typography>
                                                </Box>
                                            </Box>
                                        </Grid>
                                    ))}
                                </Grid>
                            </CardContent>
                        </Box>
                    ))}
                </>
            ) : null}

            {tabValue === 'knockout' && (
                <>
                    {knockoutMatches.length > 0 ? (
                        <Knockout
                            rounds={groupedRounds}
                        />
                    ) : (
                        <Typography variant="header3" align="center">Knockout rounds coming soon.</Typography>
                    )}
                </>
            )}


            {tabValue === 'swiss standing' && (
                <SwissStandings
                    swissStandings={swissStandings}
                    playersWithPhotos={playersWithPhotos}
                />
            )}
        </Box>
    );
}

export default SwissBracket;