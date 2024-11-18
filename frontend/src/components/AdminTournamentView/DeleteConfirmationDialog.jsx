import React from 'react';
import { Dialog, DialogTitle, DialogActions, Button } from '@mui/material';
import axios from 'axios';
import useHandleError from '../Hooks/useHandleError';

const DeleteConfirmationDialog = ({
    open,
    tournamentURL,
    token,
    tournamentToDelete,
    setTournaments,
    setDeleteDialogOpen,
    setTournamentToDelete,
    tournaments,
}) => {
    const handleError = useHandleError();
    
    const handleConfirm = async () => {
        try {
            const response = await axios.delete(`${tournamentURL}/${tournamentToDelete}`, {
                headers: {
                    'Authorization': `Bearer ${token}`,
                }
            });

            if (response.status === 200) {
                const updatedTournaments = tournaments.filter(t => t.tournamentId !== tournamentToDelete);
                setTournaments(updatedTournaments);
                setDeleteDialogOpen(false);
                setTournamentToDelete(null);
                window.location.reload();
            } 
        } catch (error) {
            handleError(error);
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
