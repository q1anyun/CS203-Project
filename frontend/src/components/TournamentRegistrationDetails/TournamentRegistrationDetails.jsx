import * as React from 'react';
import { styled } from '@mui/material/styles';
import Table from '@mui/material/Table';
import TableBody from '@mui/material/TableBody';
import TableCell, { tableCellClasses } from '@mui/material/TableCell';
import TableContainer from '@mui/material/TableContainer';
import TableHead from '@mui/material/TableHead';
import TableRow from '@mui/material/TableRow';
import Paper from '@mui/material/Paper';
import { Typography, Avatar, Button, Dialog, DialogActions, DialogContent, DialogTitle } from '@mui/material';
import styles from './TournamentRegistrationDetails.module.css';
import axios from 'axios';
import { useEffect, useState } from 'react';
import { useParams, Link } from 'react-router-dom';

const baseURL = import.meta.env.VITE_TOURNAMENT_PLAYER_URL;

const StyledTableCell = styled(TableCell)(({ theme }) => ({
    [`&.${tableCellClasses.head}`]: {
        backgroundColor: theme.palette.common.black,
        color: theme.palette.common.white,
    },
    [`&.${tableCellClasses.body}`]: {
        fontSize: 14,
    },
}));

const StyledTableRow = styled(TableRow)(({ theme }) => ({
    '&:nth-of-type(odd)': {
        backgroundColor: theme.palette.action.hover,
    },
    '&:last-child td, &:last-child th': {
        border: 0,
    },
}));

function createData(id, firstName, lastName, country, eloRating, totalMatches) {
    return { id, firstName, lastName, country, eloRating, totalMatches };
}

function TournamentRegistrationDetails() {
    const { id } = useParams();
    const [participants, setParticipants] = useState([]);
    const [open, setOpen] = useState(false);
    const [selectedParticipant, setSelectedParticipant] = useState(null); // Add state for the selected participant

    useEffect(() => {
        const fetchParticipants = async () => {
            try {
                const response = await axios.get(`${baseURL}/${id}`);
                const data = response.data;
                console.log(data);
                const formattedData = data.map((participant) =>
                    createData(participant.id, participant.firstName, participant.lastName, participant.country, participant.eloRating, participant.totalMatches)
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
        setSelectedParticipant(participant); // Set the participant to be deregistered
        setOpen(true); // Open the dialog
    };

    const handleCloseDialog = () => {
        setOpen(false); // Close the dialog
        setSelectedParticipant(null); // Reset the selected participant
    };

    return (
        <>
            <div className={styles.container}>
                <Typography variant="header1" component="h2">
                    Registered Participants
                </Typography>
                <TableContainer component={Paper}>
                    <Table sx={{ minWidth: 700 }} aria-label="customized table">
                        <TableHead>
                            <TableRow>
                                <StyledTableCell></StyledTableCell>
                                <StyledTableCell>User ID</StyledTableCell>
                                <StyledTableCell>First Name</StyledTableCell>
                                <StyledTableCell>Last Name</StyledTableCell>
                                <StyledTableCell>Country</StyledTableCell>
                                <StyledTableCell>Elo Rating</StyledTableCell>
                                <StyledTableCell>Total Matches</StyledTableCell>
                                <StyledTableCell>Actions</StyledTableCell>
                            </TableRow>
                        </TableHead>
                        <TableBody>
                            {participants.map((row) => (
                                <StyledTableRow key={row.id} hover>
                                    <Link
                                        to={`/profileview/${row.id}`}
                                        style={{ display: 'contents', textDecoration: 'none', color: 'inherit' }}
                                    >
                                        <StyledTableCell>{row.id}</StyledTableCell>
                                        <StyledTableCell>
                                            <Avatar alt="Profile" src={row.profilePhoto} sx={{ width: 56, height: 56, border: '1px solid' }} />
                                        </StyledTableCell>
                                        <StyledTableCell>{row.firstName}</StyledTableCell>
                                        <StyledTableCell>{row.lastName}</StyledTableCell>
                                        <StyledTableCell>{row.country}</StyledTableCell>
                                        <StyledTableCell>{row.eloRating}</StyledTableCell>
                                        <StyledTableCell>{row.totalMatches}</StyledTableCell>
                                    </Link>
                                    <StyledTableCell align="left">
                                        <Button
                                            variant="outlined"
                                            color="error"
                                            size="small"
                                            onClick={() => handleOpenDialog(row)}
                                        >
                                            Deregister
                                        </Button>
                                    </StyledTableCell>
                                </StyledTableRow>
                            ))}
                        </TableBody>
                    </Table>
                </TableContainer>
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