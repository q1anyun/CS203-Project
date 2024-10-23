import React, { useEffect, useState } from "react";
import axios from "axios";
import TournamentDescription from "../AdminTournamentDetails/TournamentDescription";
import useTournamentDetails from "../Hooks/useTournamentDetails";
import { useParams } from "react-router-dom";
import Profile from '../Leaderboard/Profile';
import Container from '@mui/material/Container';
import {Grid, Typography} from '@mui/material'; // Assuming you're using MUI Grid

const baseURL = import.meta.env.VITE_TOURNAMENT_PLAYER_URL;

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
        return <div>Loading...</div>; // Simple loading message
    }

    if (error) {
        return <div>{error}</div>; // Display error message
    }

    return (
        <div>
            <TournamentDescription tournament={tournament} />
            <Typography variant="header1" component="h2" align="center"> 
                    Leaderboard
            </Typography>
            <Container maxWidth="lg" sx={{ marginTop: 4, marginBottom: 10 }}>
                <Grid container spacing={2}>
                    {profiles.map((profile, index) => (
                        <Grid item xs={12} key={profile.userId}>
                            <Profile
                                rank={index + 1} 
                                firstName={profile.firstName}
                                lastName={profile.lastName}
                                eloRating={profile.eloRating}
                                profilePhoto={profile.profilePicture}
                            />
                        </Grid>
                    ))}
                </Grid>
                {/* To create specific leaderboard position for player */}
            </Container>
        </div>
    );
}

// Sample data for testing (you can remove this in production)
const sampleProfiles = [
    { userId: 1, firstName: "Alice", lastName: "Smith", eloRating: 1500, profilePicture: "/path/to/photo1.jpg" },
    { userId: 2, firstName: "Bob", lastName: "Johnson", eloRating: 1420, profilePicture: "/path/to/photo2.jpg" },
    { userId: 3, firstName: "Charlie", lastName: "Brown", eloRating: 1600, profilePicture: "/path/to/photo3.jpg" },
];

// Simulate fetching data
const fetchSampleData = () => {
    return new Promise((resolve) => {
        setTimeout(() => {
            resolve(sampleProfiles);
        }, 1000);
    });
};

export default TournamentLeaderboard;
