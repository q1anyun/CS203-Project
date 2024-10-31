import * as React from 'react';
import { styled } from '@mui/material/styles';
import { Typography, Avatar, Button, Dialog, DialogActions, DialogContent, DialogTitle, Box } from '@mui/material';
import styles from './TournamentRegistrationDetails.module.css';
import axios from 'axios';
import { useEffect, useState } from 'react';
import { useParams, Link, useLocation } from 'react-router-dom';
import PersonRemoveIcon from '@mui/icons-material/PersonRemove';

const baseURL = import.meta.env.VITE_TOURNAMENT_PLAYER_URL;

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

function createData(id, firstName, lastName, country) {
    return { id, firstName, lastName, country };
}

function TournamentRegistrationDetails() {
    const { id } = useParams();
    const [participants, setParticipants] = useState([]);
    const [open, setOpen] = useState(false);
    const [selectedParticipant, setSelectedParticipant] = useState(null);
    const location = useLocation(); // Get location from props
    const tournament = location.state?.tournament; // Access tournament from state

    useEffect(() => {
        const fetchParticipants = async () => {
            try {
                const response = await axios.get(`${baseURL}/${id}`);
                const data = response.data;
                const formattedData = data.map((participant) =>
                    createData(participant.id, participant.firstName, participant.lastName, participant.country)
                );
                setParticipants(formattedData);
            } catch (error) {
                console.error('Error fetching participants:', error);
            }
        };

        fetchParticipants();
    }, [id]);

    const deregisterParticipant = async () => {
        if (selectedParticipant) {
            try {
                await axios.delete(`${baseURL}/${selectedParticipant.id}/${id}`);
                setParticipants((prevParticipants) =>
                    prevParticipants.filter((participant) => participant.id !== selectedParticipant.id)
                );
                handleCloseDialog();
            } catch (error) {
                console.error('Error deregistering participant:', error);
            }
        }
    };

    const handleOpenDialog = (participant) => {
        setSelectedParticipant(participant);
        setOpen(true);
    };

    const handleCloseDialog = () => {
        setOpen(false);
        setSelectedParticipant(null);
    };

    return (
        <>
            <div className={styles.container}>
                <Typography variant="header1" component="h2">
                    Registered Participants
                </Typography>
                <DetailBoxContainer>
                    {participants.map((participant) => (
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
                                    <Typography variant="h6">
                                        {`${participant.firstName} ${participant.lastName}`}
                                    </Typography>
                                </Link>
                                <Typography variant="body2">{participant.country}</Typography>
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
                    ))}
                </DetailBoxContainer>
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