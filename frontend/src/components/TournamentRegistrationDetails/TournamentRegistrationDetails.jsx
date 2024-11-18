import * as React from 'react';
import { styled } from '@mui/material/styles';
import { Typography, Avatar, Button, Dialog, DialogActions, DialogContent, DialogTitle, Box, TextField } from '@mui/material';
import styles from './TournamentRegistrationDetails.module.css';
import axios from 'axios';
import { useEffect, useState } from 'react';
import { useParams, Link, useLocation } from 'react-router-dom';
import PersonRemoveIcon from '@mui/icons-material/PersonRemove';
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

const DetailBoxContainer = styled(Box)({
    display: 'grid',
    gridTemplateColumns: 'repeat(3, 1fr)',
    gap: '25px',
    marginTop: '20px',
});

function TournamentRegistrationDetails() {
    const { id } = useParams();
    const [open, setOpen] = useState(false);
    const [selectedParticipant, setSelectedParticipant] = useState(null);
    const [searchTerm, setSearchTerm] = useState('');
    const [page, setPage] = useState(0);
    const [rowsPerPage, setRowsPerPage] = useState(9);
    const location = useLocation();
    const tournament = location.state?.tournament;
    const handleError = useHandleError();
    const { participants, loading } = useTournamentParticipants(id);

    const deregisterParticipant = async () => {
        if (selectedParticipant) {
            try {
                await axios.delete(`${tournamentPlayerURL}/${selectedParticipant.id}/${id}`);
                setParticipants((prevParticipants) =>
                    prevParticipants.filter((participant) => participant.id !== selectedParticipant.id)
                );

                handleCloseDialog();
                alert('Player is successfully deregistered');
            } catch (error) {
                handleError(error);
            }
        }
    };

    // Handle search
    const handleSearchChange = (event) => {
        setSearchTerm(event.target.value);
        setPage(0);
    };

    // Handle page change
    const handleChangePage = (newPage) => {
        setPage(newPage);
    };


    const handleOpenDialog = (participant) => {
        setSelectedParticipant(participant);
        setOpen(true);
    };

    const handleCloseDialog = () => {
        setOpen(false);
        setSelectedParticipant(null);
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
            <div className={styles.container}>
                <Typography variant="header1" component="h2">
                    Registered Participants
                </Typography>
                <TextField
                    variant="outlined"
                    placeholder="Search by  Name"
                    size="small"
                    value={searchTerm}
                    onChange={handleSearchChange}
                    style={{ marginTop: '20px', width: '95vw' }} // Consider using percentage or theme-based spacing for responsiveness
                />
                <DetailBoxContainer>
                    {loading ? (
                        <Typography align="center">Loading...</Typography> // Consistency in typography for loading
                    ) : participants.length === 0 ? (
                        <Typography variant="playerProfile2" align="center">
                            No participants registered.
                        </Typography>
                    ) : (
                        paginatedParticipants.map((participant) => (
                            <DetailBox key={participant.id}>
                                <Avatar
                                    alt={`${participant.firstName} ${participant.lastName}`}
                                    src={participant.profilePhoto}
                                    sx={{ width: 56, height: 56, marginRight: '16px' }}
                                />
                                <Box sx={{ flexGrow: 1 }}>
                                    <Link
                                        to={`/profileview/${participant.id}`}
                                        style={{ textDecoration: 'none', color: 'inherit' }}
                                    >
                                        <Typography variant="playerProfile2">
                                            {`${participant.firstName} ${participant.lastName}`}
                                        </Typography>
                                    </Link>
                                    <ReactCountryFlag
                                        countryCode={participant.country}
                                        svg
                                        style={{
                                            width: '2em',
                                            height: '2em',
                                            marginLeft: '10px',
                                        }}
                                        title={participant.country}
                                    />
                                </Box>
                                <PersonRemoveIcon
                                    color="primary"
                                    sx={{
                                        cursor: (tournament?.status === 'LIVE' || tournament?.status === 'COMPLETED') ? 'not-allowed' : 'pointer'
                                    }}
                                    onClick={tournament?.status === 'LIVE' || tournament?.status === 'COMPLETED'
                                        ? null
                                        : () => handleOpenDialog(participant)
                                    }
                                />
                            </DetailBox>
                        ))
                    )}
                </DetailBoxContainer>
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
            </div>

            {/* Confirmation Dialog */}
            <Dialog
                open={open}
                onClose={handleCloseDialog}
                aria-labelledby="confirm-dialog-title"
                aria-describedby="confirm-dialog-description"
            >
                <DialogTitle id="confirm-dialog-title">Confirm Deregistration</DialogTitle>
                <DialogContent>
                    Are you sure you want to deregister{' '}
                    {selectedParticipant?.firstName} {selectedParticipant?.lastName}?
                </DialogContent>
                <DialogActions>
                    <Button onClick={handleCloseDialog} color="primary">
                        Cancel
                    </Button>
                    <Button onClick={deregisterParticipant} color="error">
                        Confirm
                    </Button>
                </DialogActions>
            </Dialog>
        </>
    );
}

export default TournamentRegistrationDetails;