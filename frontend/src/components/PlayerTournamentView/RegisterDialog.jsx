import React from 'react';
import { Dialog, DialogActions, DialogContent, DialogContentText, DialogTitle, Checkbox, FormControlLabel, Typography, Button, Box, Grid } from '@mui/material';

function RegisterDialog({ handleRegister, agreedToTerms, setAgreedToTerms, openRegisterDialog, setOpenRegisterDialog }) {

    const handleRegisterDialogClose = () => {
        setOpenRegisterDialog(false);
        setAgreedToTerms(false);
    };

    const handleAgreeChange = (event) => {
        setAgreedToTerms(event.target.checked);
    };

    return (
        <Dialog open={openRegisterDialog} onClose={handleRegisterDialogClose}>
            <DialogTitle variant='header4' align="center">Registration for Chess Tournament</DialogTitle>
            <DialogContent>
                <DialogContentText align="center" variant='body4'>
                    Please agree to the following terms to join the tournament:
                </DialogContentText>
                <Box sx={{ margin: '16px 0' }}>
                    <Typography variant="body4" gutterBottom display='block'>
                        • No use of chess bots or external assistance during matches.
                    </Typography>
                    <Typography variant="body4" gutterBottom display='block'>
                        • All participants must maintain good sportsmanship.
                    </Typography>
                    <Typography variant="body4" gutterBottom display='block'>
                        • Adherence to tournament rules is mandatory.
                    </Typography>
                    <Typography variant="body4" gutterBottom display='block'>
                        • Failure to comply may result in disqualification.
                    </Typography>
                </Box>
                <Box sx={{ display: 'flex', justifyContent: 'center' }}>
                    <FormControlLabel
                        control={<Checkbox checked={agreedToTerms} onChange={handleAgreeChange} />}
                        label={
                            <Typography variant="body4">
                                I agree to the terms and conditions
                            </Typography>
                        }
                    />
                </Box>
            </DialogContent>
            <DialogActions sx={{ justifyContent: 'center', pb: 2 }}>
                <Grid container justifyContent="center" spacing={2}>
                    <Grid item>
                        <Button onClick={handleRegisterDialogClose} variant="outlined">
                            Cancel
                        </Button>
                    </Grid>
                    <Grid item>
                        <Button
                            onClick={handleRegister}
                            variant="contained"
                            disabled={!agreedToTerms}
                        >
                            Register
                        </Button>
                    </Grid>
                </Grid>
            </DialogActions>
        </Dialog>
    );
}

export default RegisterDialog;
