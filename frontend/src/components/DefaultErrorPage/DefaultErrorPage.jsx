import React from 'react';
import { Container, Typography } from '@mui/material';
import { useLocation } from 'react-router-dom';

function DefaultErrorPage() {
    const location = useLocation();
    const query = new URLSearchParams(location.search);
    const statusCode = query.get('statusCode');
    const errorMessage = query.get('errorMessage') || 'Something went wrong.';

    return (
        <Container>
            <Typography variant="h2" color="error">Oops!</Typography>
            <Typography variant="h5">
                {statusCode ? `Error ${statusCode}: ${errorMessage}` : 'An unexpected error occurred.'}
            </Typography>
            <Typography variant="body1">Please try again later or contact support.</Typography>
            <br></br>
            <Typography>Temporary page to view and debug errors</Typography>
        </Container>
    );
}

export default DefaultErrorPage;
