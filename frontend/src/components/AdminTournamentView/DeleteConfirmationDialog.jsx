import React from 'react';
import { Dialog, DialogTitle, DialogActions, Button } from '@mui/material';
import axios from 'axios';

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
            } else {
                console.warn('Delete request did not return a success status:', response.status);
            }
        } catch (error) {
            console.error('Error deleting tournament:', error);
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
