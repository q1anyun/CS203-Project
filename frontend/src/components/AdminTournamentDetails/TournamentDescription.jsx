import React, { useState, useEffect } from 'react';
import { Box, Typography, Chip, Button, Divider, Grid } from '@mui/material';
import { styled } from '@mui/system';
import defaultbackgroundImage from '../../assets/playerbg.jpg';
import { fetchTournamentPic } from '../Hooks/fetchTournamentPic';
import { useNavigate } from 'react-router-dom';

const DetailBox = styled(Box)({
    backgroundColor: '#fff',
    borderRadius: '8px',
    padding: '16px',
    marginBottom: '10px',
    boxShadow: '0 2px 4px rgba(0, 0, 0, 0.1)',
});

const statusColorMap = {
    LIVE: 'success',
    UPCOMING: 'warning',
    EXPIRED: 'default',
};

function TournamentDescription({ tournament, handleStart, handleViewRegisteredPlayers }) {
    const [localTournamentPic, setLocalTournamentPic] = useState(null);
    const navigate = useNavigate();

    useEffect(() => {
        const getTournamentImage = async () => {
            const imageUrl = await fetchTournamentPic(tournament.id);
            setLocalTournamentPic(imageUrl);
        };

        getTournamentImage();
    }, [tournament.id]);

    return (
        <Box sx={{ padding: 2 }}>
            <Box
                sx={{
                    width: '100vw',
                    height: '200px',
                    position: 'relative',
                }}
            >
                <img
                    alt="Tournament"
                    src={localTournamentPic || defaultbackgroundImage}
                    style={{
                        width: '100%',
                        height: '100%',
                        objectFit: 'cover',
                        position: 'absolute',
                        top: -30,
                        left: -32,
                    }}
                />
            </Box>

            <Typography variant="header1">{tournament.name}</Typography>
            <Chip label={tournament.status} color={statusColorMap[tournament.status]} sx={{ marginLeft: '10px' }} />
            <Button
                variant="contained"
                color="primary"
                onClick={handleStart}
                disabled={tournament.status !== 'UPCOMING'}
                sx={{ marginLeft: '10px' }}
            >
                Start Tournament
            </Button>

            <Typography variant="playerProfile2" display={'block'} textAlign={'left'} marginLeft={'20px'}>
                {tournament.description}
            </Typography>

            <Divider sx={{ margin: '20px 0' }} />

            {/* Tournament Details */}
            <Grid container spacing={2}>
                <Grid item xs={12} sm={3}>
                    <DetailBox>
                        <Typography variant="playerProfile2"><strong>Format</strong></Typography>
                        <Typography variant="body2">{tournament.format}</Typography>
                    </DetailBox>
                </Grid>
                <Grid item xs={12} sm={3}>
                    <DetailBox>
                        <Typography variant="playerProfile2"><strong>Current Players</strong></Typography>
                        <Typography variant="body2">{tournament.currentPlayers} / {tournament.maxPlayers}</Typography>
                    </DetailBox>
                </Grid>
                <Grid item xs={12} sm={3}>
                    <DetailBox>
                        <Typography variant="playerProfile2"><strong>Start Date</strong></Typography>
                        <Typography variant="body2">{new Date(tournament.startDate).toLocaleDateString()}</Typography>
                    </DetailBox>
                </Grid>
                <Grid item xs={12} sm={3}>
                    <DetailBox>
                        <Typography variant="playerProfile2"><strong>End Date</strong></Typography>
                        <Typography variant="body2">{new Date(tournament.endDate).toLocaleDateString()}</Typography>
                    </DetailBox>
                </Grid>
                <Grid item xs={12} sm={3}>
                    <DetailBox>
                        <Typography variant="playerProfile2"><strong>Minimum Elo</strong></Typography>
                        <Typography variant="body2">{tournament.minElo}</Typography>
                    </DetailBox>
                </Grid>
                <Grid item xs={12} sm={3}>
                    <DetailBox>
                        <Typography variant="playerProfile2"><strong>Maximum Elo</strong></Typography>
                        <Typography variant="body2">{tournament.maxElo}</Typography>
                    </DetailBox>
                </Grid>
                <Grid item xs={12} sm={3}>
                    <DetailBox>
                        <Typography variant="playerProfile2"><strong>Time Control</strong></Typography>
                        <Typography variant="body2">{tournament.timeControl?.name || 'N/A'}</Typography>
                    </DetailBox>
                </Grid>
                <Grid item xs={12} sm={3}>
                    <DetailBox>
                        <Typography variant="playerProfile2"><strong>Tournament Type</strong></Typography>
                        <Typography variant="body2">{tournament.tournamentType?.typeName || 'N/A'}</Typography> {/* Fixed rendering the tournament type */}
                    </DetailBox>
                </Grid>
            </Grid>
            <Button variant='contained' onClick={handleViewRegisteredPlayers} color='outlined'>
                <Typography variant="body4">Click to view registered players</Typography>
            </Button>
            <Button variant="contained" color="outlined" sx={{ marginLeft: '10px' }} onClick={() => navigate(`/tournaments/leaderboard/${tournament.id}`)}>
                <Typography variant="body4">Click to view Leaderboard</Typography>
            </Button>
        </Box>
    );
}

export default TournamentDescription;
