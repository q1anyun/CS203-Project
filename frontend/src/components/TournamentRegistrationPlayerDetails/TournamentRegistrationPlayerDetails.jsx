import React, { useState, useEffect } from 'react';
import { styled } from '@mui/material/styles';
import { Typography, Avatar, Box, Grid, Button, TextField } from '@mui/material';
import axios from 'axios';
import { useParams, Link, useNavigate } from 'react-router-dom';
import { useEffect, useState } from 'react';
import { useParams, Link, useNavigate } from 'react-router-dom';

const baseURL = import.meta.env.VITE_TOURNAMENT_PLAYER_URL;

const DetailBox = styled(Box)({
    backgroundColor: '#fff',
    borderRadius: '8px',
    padding: '16px',
    boxShadow: '0 2px 4px rgba(0, 0, 0, 0.1)',
    display: 'flex',
    alignItems: 'center',
});


function createData(id, firstName, lastName, country, score) {
    return { id, firstName, lastName, country, score };
}

function TournamentRegistrationPlayerDetails() {
    const { id } = useParams();
    const [participants, setParticipants] = useState([]);
    const navigate = useNavigate();
    const [searchTerm, setSearchTerm] = useState('');
    const [page, setPage] = useState(0);
    const [rowsPerPage, setRowsPerPage] = useState(10);
    const navigate = useNavigate();

    useEffect(() => {
        const fetchParticipants = async () => {
            try {
                const response = await axios.get(`${baseURL}/${id}`);
                const data = response.data;
                console.log(data);
                const formattedData = data.map((participant) =>
                    createData(participant.id, participant.firstName, participant.lastName, participant.country, participant.score)
                );
                setParticipants(formattedData);

                // Create leaderboard data
                const sortedData = formattedData.sort((a, b) => b.score - a.score);
                setLeaderboard(sortedData);
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

        fetchParticipants();
    }, [id]);


    // Handle search
    const handleSearchChange = (event) => {
        setSearchTerm(event.target.value);
        setPage(0); // Reset to first page on search
    };

    // Handle page change
    const handleChangePage = (newPage) => {
        setPage(newPage);
    };

    const handleBackClick = () => {
        navigate(`/player/tournaments/${id}`);
    };


    // Filter participants based on search term
    const filteredParticipants = participants.filter((participant) =>
        `${participant.firstName.toLowerCase()} ${participant.lastName.toLowerCase()}`.includes(searchTerm.toLowerCase())
    );

    // Calculate total pages
    const totalPages = Math.ceil(filteredParticipants.length / rowsPerPage);

    // Paginate participants
    const paginatedParticipants = filteredParticipants.slice(page * rowsPerPage, page * rowsPerPage + rowsPerPage);

    return (
        <>
            <Box padding={3}>
                <Grid container spacing={4}>
                    <Grid item xs={12}>
                        <Box>

                            <Typography variant="header1">
                                Registered Participants
                            </Typography>
                            <Button variant="outlined" onClick={handleBackClick} sx={{ marginLeft: '10px' }}>
                                Back
                            </Button>

                            <TextField
                                variant="outlined"
                                placeholder="Search by Username"
                                size='small'
                                value={searchTerm}
                                onChange={handleSearchChange}
                                style={{ marginTop: '20px', width: '95vw' }}
                            />
                            {participants.length === 0 ? (
                                <Typography variant="playerProfile2" align="center">
                                    Be the first to register!
                                </Typography>
                            ) : (
                                paginatedParticipants.map((participant) => (
                                    <DetailBox key={participant.id} marginTop={2}>
                                        <Avatar
                                            alt={`${participant.firstName} ${participant.lastName}`}
                                            src={participant.profilePhoto}
                                            sx={{ width: 56, height: 56, marginRight: '16px' }}
                                        />
                                        <Box >
                                            <Link
                                                to={`/profileview/${participant.id}`}
                                                style={{ textDecoration: 'none', color: 'inherit' }}
                                            >
                                                <Typography variant="header3">{`${participant.firstName} ${participant.lastName}`}</Typography>
                                            </Link>
                                            <Typography variant="body1">{participant.country}</Typography>
                                        </Box>
                                    </DetailBox>
                                ))
                            )}
                        </Box>
                    </Grid>
                </Grid>
                <Box sx={{ display: 'flex', justifyContent: 'center', alignItems: 'center', mt: 2 }}>
                    <Button
                        onClick={() => handleChangePage(page - 1)}
                        disabled={page === 0}
                        variant="contained"
                        sx={{ mr: 2 }}
                    >
                        Previous
                    </Button>
                    <Typography variant="body1">
                        Page {page + 1} of {totalPages}
                    </Typography>
                    <Button
                        onClick={() => handleChangePage(page + 1)}
                        disabled={page >= totalPages - 1}
                        variant="contained"
                        sx={{ ml: 2 }}
                    >
                        Next
                    </Button>
                </Box>
            </Box>
        </>
    );
}

export default TournamentRegistrationPlayerDetails;