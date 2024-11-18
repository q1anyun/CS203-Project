import React, { useState, useEffect } from 'react';
import { styled } from '@mui/material/styles';
import { Typography, Avatar, Box, Grid, Button, TextField } from '@mui/material';
import axios from 'axios';
import { useParams, Link, useNavigate } from 'react-router-dom';
import ReactCountryFlag from 'react-country-flag';
import defaultProfilePic from '../../assets/default_user.png';
import useHandleError from '../Hooks/useHandleError';
import useTournamentParticipants from '../Hooks/useTournamentParticipants';

const tournamentPlayerURL = import.meta.env.VITE_TOURNAMENT_PLAYER_URL;
const playerURL = import.meta.env.VITE_PLAYER_SERVICE_URL

const DetailBox = styled(Box)({
    backgroundColor: '#fff',
    borderRadius: '8px',
    padding: '16px',
    boxShadow: '0 2px 4px rgba(0, 0, 0, 0.1)',
    display: 'flex',
    alignItems: 'center',
});

function createData(id, firstName, lastName, country, eloRating) {
    return { id, firstName, lastName, country, eloRating };
}

function TournamentRegistrationPlayerDetails() {
    const { id } = useParams();
    const [searchTerm, setSearchTerm] = useState('');
    const [page, setPage] = useState(0);
    const [rowsPerPage, setRowsPerPage] = useState(10);
    const handleError = useHandleError();
    const { participants, loading} = useTournamentParticipants(id);

    // Handle search
    const handleSearchChange = (event) => {
        setSearchTerm(event.target.value);
        setPage(0); // Reset to first page on search
    };

    // Handle page change
    const handleChangePage = (newPage) => {
        setPage(newPage);
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
                            <TextField
                                variant="outlined"
                                placeholder="Search by Name"
                                size="small"
                                value={searchTerm}
                                onChange={handleSearchChange}
                                style={{ marginTop: '20px', width: '95vw' }} // Consider using percentage or theme-based spacing for responsiveness
                            />
                            {loading ? (
                                <Typography variant="playerProfile2" align="center">Loading...</Typography> // Consistency in typography for loading
                            ) : (
                                participants.length === 0 ? (
                                    <Typography variant="playerProfile2" align="center">
                                        Be the first to register!
                                    </Typography>
                                ) : (
                                    paginatedParticipants.map((participant) => (
                                        <DetailBox
                                            key={participant.id}
                                            display="flex"
                                            py={2}
                                            px={2}
                                            borderBottom="1px solid #ddd"
                                        >
                                            <Avatar
                                                alt={`${participant.firstName} ${participant.lastName}`}
                                                src={participant.profilePhoto}
                                                sx={{ width: 56, height: 56, marginRight: '16px' }}
                                            />
                                            <Box>
                                                <Link
                                                    to={`/profileview/${participant.id}`}
                                                    style={{ textDecoration: 'none', color: 'inherit' }}
                                                >
                                                    <Typography variant="header3">{`${participant.firstName} ${participant.lastName}`}</Typography>
                                                </Link>
                                                <ReactCountryFlag
                                                    countryCode={participant.country}
                                                    svg
                                                    style={{
                                                        width: '2em',
                                                        height: '2em',
                                                        marginLeft: '10px'
                                                    }}
                                                    title={participant.country}
                                                />
                                            </Box>
                                            <Box
                                                sx={{
                                                    marginLeft: 'auto',
                                                    alignSelf: 'center' // Correct alignment to 'center' instead of 'right' which is not valid
                                                }}
                                            >
                                                <Typography variant="header3">{participant.eloRating}</Typography>
                                            </Box>
                                        </DetailBox>
                                    ))
                                )
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