import React, { useEffect, useState } from "react";
import axios from "axios";
import useTournamentDetails from "../Hooks/useTournamentDetails";
import { useParams, Link } from "react-router-dom";
import Profile from '../Leaderboard/Profile';
import Container from '@mui/material/Container';
import { Grid, Typography, Box, Chip, Divider } from '@mui/material';

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
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    useEffect(() => {
        const fetchData = async () => {
            try {
                const response = await axios.get(`${baseURL}/${id}`);
                setProfiles(response.data);
                setLoading(false);
            } catch (err) {
                setError('Failed to load data');
                setLoading(false);
            }
        };

        fetchData();
    }, [id]);

    if (loading) {
        return <div>Loading...</div>;
    }

    if (error) {
        return <div>{error}</div>;
    }

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
                        {profiles.length === 0 ? ( // Check if profiles array is empty
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
                                        style={{ textDecoration: 'none', color: 'inherit' }} // Optional styling for the link
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