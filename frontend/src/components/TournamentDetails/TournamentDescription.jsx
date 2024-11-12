import React, { useState, useEffect } from 'react';
import { Box, Typography, Chip, Button, Divider, Grid } from '@mui/material';
import { styled } from '@mui/system';
import { fetchTournamentPic } from '../Hooks/fetchTournamentPic';
import { useNavigate } from 'react-router-dom';
import LocationLink from '../Hooks/getLocationLink';
import RegisterDialog from '../PlayerTournamentView/RegisterDialog';
import WithdrawDialog from '../PlayerTournamentView/WithdrawDialog';
import axios from 'axios';

const tournamentPlayerURL = import.meta.env.VITE_TOURNAMENT_PLAYER_URL;
const playerServiceURL = import.meta.env.VITE_PLAYER_SERVICE_URL;
const tournamentURL = import.meta.env.VITE_TOURNAMENT_SERVICE_URL;

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



function TournamentDescription({ tournament }) {
    const [localTournamentPic, setLocalTournamentPic] = useState(null);
    const navigate = useNavigate();
    const [joinedTournaments, setJoinedTournaments] = useState([]);
    const [openRegisterDialog, setOpenRegisterDialog] = useState(false);
    const [openWithdrawDialog, setOpenWithdrawDialog] = useState(false);
    const [agreedToTerms, setAgreedToTerms] = useState(false);
    const token = localStorage.getItem('token');
    const [elo, setElo] = useState('');
    useEffect(() => {
        const fetchPlayerData = async () => {
            if (!token) return;

            try {
                // Fetch joined tournaments
                const registeredResponse = await axios.get(`${tournamentURL}/registered/current`, {
                    headers: { 'Authorization': `Bearer ${token}` },
                });
                setJoinedTournaments(registeredResponse.data);

                // Fetch player Elo
                const eloResponse = await axios.get(`${playerServiceURL}/currentPlayerById`, {
                    headers: { 'Authorization': `Bearer ${token}` },
                });
                setElo(eloResponse.data.eloRating);
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

        fetchPlayerData();
    }, [token, navigate]);

    const isJoined = (tournamentId) => joinedTournaments.some(tournament => tournament.id === tournamentId);

    const handleJoin = () => {
        setOpenRegisterDialog(true);
    };

    const handleWithdraw = () => {
        setOpenWithdrawDialog(true);
    }
    {/* DOESNT ACCOUNT min max elo*/ }
    const handleRegister = async () => {
        try {
            const response = await axios.post(`${tournamentPlayerURL}/register/current/${tournament.id}`, null, {
                headers: { 'Authorization': `Bearer ${token}` },
            });

            setJoinedTournaments(prev => [...prev, tournament]);
            setOpenRegisterDialog(false);
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
        window.location.reload();

    };

    {/*WAITING FOR API TO WITHDRAW */ }
    const handleWithdrawConfirmation = async () => {
        try {
            const response = await axios.delete(`${tournamentPlayerURL}/current/${tournament.id}`, {
                headers: { 'Authorization': `Bearer ${token}` },
            });

            setJoinedTournaments(prev => prev.filter(specificTournament => specificTournament.id !== tournament.id));
            setOpenWithdrawDialog(false);
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
        window.location.reload();
    };

    useEffect(() => {
        const getTournamentImage = async () => {
            const imageUrl = await fetchTournamentPic(tournament.id);
            setLocalTournamentPic(imageUrl);
        };

        getTournamentImage();
    }, [tournament.id]);

    const handleViewRegisteredPlayers = () => {
        navigate(`${window.location.pathname}/registeredplayers`);
    };

    return (


        <>
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
                        src={localTournamentPic}
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



                <Typography variant="header1" >{tournament.name}</Typography>
                <Chip label={tournament.status} color={statusColorMap[tournament.status]} sx={{ marginLeft: '10px' }} />

                <Button
                    variant="contained"
                    color={isJoined(tournament.id) ? 'secondary' : 'primary'}
                    onClick={() => isJoined(tournament.id) ? handleWithdraw() : handleJoin()}
                    disabled={
                        tournament.status === 'COMPLETED' ||
                        elo < tournament.minElo ||
                        elo > tournament.maxElo ||
                        (!isJoined(tournament.id) && tournament.currentPlayers >= tournament.maxPlayers)
                    }
                    sx={{ marginLeft: '10px' }}
                >
                    {
                        tournament.status === 'COMPLETED'
                            ? 'OVER'
                            : isJoined(tournament.id)
                                ? 'Withdraw'
                                : tournament.currentPlayers >= tournament.maxPlayers
                                    ? 'FULL'
                                    : 'Join'
                    }
                </Button>


                <Typography variant="playerProfile2" display={'block'} textAlign={'left'} marginLeft={'20px'}>{tournament.description}</Typography>
                <Typography variant="playerProfile2" display={'block'} textAlign={'left'} marginLeft={'20px'}>
                    {tournament.format !== "ONLINE" && (
                        <LocationLink
                            address={tournament.locationAddress}
                            latitude={tournament.locationLatitude}
                            longitude={tournament.locationLongitude}
                        />
                    )}
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

                    <Button variant='contained' onClick={handleViewRegisteredPlayers} color='outlined'>
                        <Typography variant="body4">Click to view registered players</Typography>
                    </Button>
                </Grid>

            </Box>
            <RegisterDialog
                handleRegister={handleRegister}
                agreedToTerms={agreedToTerms}
                setAgreedToTerms={setAgreedToTerms}
                openRegisterDialog={openRegisterDialog}
                setOpenRegisterDialog={setOpenRegisterDialog}
            />
            <WithdrawDialog
                openWithdrawDialog={openWithdrawDialog}
                setOpenWithdrawDialog={setOpenWithdrawDialog}
                handleWithdrawConfirmation={handleWithdrawConfirmation}
            />
        </>
    );
}

export default TournamentDescription;
