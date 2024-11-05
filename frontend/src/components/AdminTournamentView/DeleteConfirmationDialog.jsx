import React from 'react';
import { Dialog, DialogTitle, DialogActions, Button } from '@mui/material';
import axios from 'axios';
import { useNavigate } from 'react-router-dom';

const DeleteConfirmationDialog = ({
    open,
    baseURL,
    token,
    tournamentToDelete,
    setTournaments,
    setDeleteDialogOpen,
    setTournamentToDelete,
    tournaments,
}) => {
    const navigate = useNavigate();
    
    const handleConfirm = async () => {
        try {
            const response = await axios.delete(`${baseURL}/${tournamentToDelete}`, {
                headers: {
                    'Authorization': `Bearer ${token}`,
                }
            });

            if (response.status === 200) {
                const updatedTournaments = tournaments.filter(t => t.tournamentId !== tournamentToDelete);
                setTournaments(updatedTournaments);
                setDeleteDialogOpen(false);
                setTournamentToDelete(null);
                console.log('Tournament deleted successfully.');
                window.location.reload();
            } 
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

    const handleCancel = () => {
        setDeleteDialogOpen(false);
        setTournamentToDelete(null);
    };

    return (
        <Dialog open={open} onClose={handleCancel}>
            <DialogTitle>Confirm Delete</DialogTitle>
            <DialogActions>
                <Button onClick={handleCancel} color="secondary">Cancel</Button>
                <Button onClick={handleConfirm} color="primary">Delete</Button>
            </DialogActions>
        </Dialog>
    );
};

export default DeleteConfirmationDialog;
