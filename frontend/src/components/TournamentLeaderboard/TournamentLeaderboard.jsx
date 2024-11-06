import React, { useEffect, useState } from "react";
import axios from "axios";
import useTournamentDetails from "../Hooks/useTournamentDetails";
import { useParams, Link } from "react-router-dom";
import Profile from '../Leaderboard/Profile';
import Container from '@mui/material/Container';
import { Grid, Typography, Box, Chip, Divider } from '@mui/material';
import { useNavigate } from 'react-router-dom';

const baseURL = import.meta.env.VITE_TOURNAMENT_PLAYER_URL;
const statusColorMap = {
    LIVE: 'success',
    UPCOMING: 'warning',
    EXPIRED: 'default',
};

function TournamentLeaderboard() {
    const { id } = useParams();
    const { tournament } = useTournamentDetails(id);
    const [profiles, setProfiles] = useState([]);
    const navigate = useNavigate();

    useEffect(() => {
        const fetchData = async () => {
            try {
                const response = await axios.get(`${baseURL}/${id}`);
                setProfiles(response.data);
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
        fetchData();
    }, [id]);

    return (
        <div>
            <Box sx={{ padding: 2 }}>
                <Typography variant="header1" >{tournament.name}</Typography>
                <Chip label={tournament.status} color={statusColorMap[tournament.status]} sx={{ marginLeft: '10px' }} />
                <Typography variant="playerProfile2" display={'block'} textAlign={'left'} marginLeft={'20px'}>{tournament.description}</Typography>
                <Divider sx={{ margin: '20px 0' }} />

                <Typography variant="header1" component="h2" align="center">
                    Leaderboard
                </Typography>
                <Container maxWidth="lg" sx={{ marginTop: 4, marginBottom: 10 }}>
                    <Grid container spacing={2}>
                        {profiles.length === 0 ? ( 
                            <Grid item xs={12}>
                                <Typography variant="body1" align="center">
                                    No participants registered.
                                </Typography>
                            </Grid>
                        ) : (
                            profiles.map((profile, index) => (
                                <Grid item xs={12} key={profile.id}>
                                    <Link
                                        to={`/profileview/${profile.id}`}
                                        style={{ textDecoration: 'none', color: 'inherit' }} 
                                    >
                                        <Profile
                                            rank={index + 1}
                                            firstName={profile.firstName}
                                            lastName={profile.lastName}
                                            eloRating={profile.eloRating}
                                            profilePhoto={profile.profilePicture}
                                        />
                                    </Link>
                                </Grid>
                            ))
                        )}
                    </Grid>
                </Container>
            </Box>
        </div>
    );
}

export default TournamentLeaderboard;