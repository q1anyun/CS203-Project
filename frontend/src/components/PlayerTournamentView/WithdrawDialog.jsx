import React, { useState } from 'react';
import { Dialog, DialogActions, DialogContent, DialogContentText, DialogTitle, Checkbox, FormControlLabel, Typography, Button, Box, Grid } from '@mui/material';

function WithdrawDialog({ openWithdrawDialog, setOpenWithdrawDialog, handleWithdrawConfirmation }) {
    const [agreedToTerms, setAgreedToTerms] = useState(false);

    const handleWithdrawDialogClose = () => {
        setOpenWithdrawDialog(false);
        setAgreedToTerms(false);
    };

    const handleAgreeChange = (event) => {
        setAgreedToTerms(event.target.checked);
    };

    return (
        <Dialog open={openWithdrawDialog} onClose={handleWithdrawDialogClose}>
            <DialogTitle variant='header4' align="center">Withdraw from Chess Tournament</DialogTitle>
            <DialogContent>
                <DialogContentText align="center" variant='body4'>
                    Are you sure you want to withdraw from the tournament?
                </DialogContentText>
                <Box sx={{ margin: '16px 0' }}>
                    <Typography variant="body4" gutterBottom display='block'>
                        • Withdrawal will be final and you might not be able to rejoin this tournament.
                    </Typography>
                    <Typography variant="body4" gutterBottom display='block'>
                        • Depending on the tournament rules, frequent withdrawals may affect your ability to participate in future tournaments.
                    </Typography>
                    <Typography variant="body4" gutterBottom display='block'>
                        • Please confirm your decision carefully.
                    </Typography>
                </Box>
                <Box sx={{ display: 'flex', justifyContent: 'center' }}>
                    <FormControlLabel
                        control={<Checkbox checked={agreedToTerms} onChange={handleAgreeChange} />}
                        label={
                            <Typography variant="body4">
                                I confirm my withdrawal from the tournament
                            </Typography>
                        }
                    />
                </Box>
            </DialogContent>
            <DialogActions sx={{ justifyContent: 'center', pb: 2 }}>
                <Grid container justifyContent="center" spacing={2}>
                    <Grid item>
                        <Button onClick={handleWithdrawDialogClose} variant="outlined">
                            Cancel
                        </Button>
                    </Grid>
                    <Grid item>
                        <Button
                            onClick={handleWithdrawConfirmation}
                            variant="contained"
                            disabled={!agreedToTerms}
                        >
                            Withdraw
                        </Button>
                    </Grid>
                </Grid>
            </DialogActions>
        </Dialog>
    );
}

export default WithdrawDialog;
