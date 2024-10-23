import React from 'react';
import { Container, Typography, Button } from '@mui/material';
import { useNavigate } from 'react-router-dom';

function PageNotFound() {
    const navigate = useNavigate();

    const handleGoHome = () => {
        navigate('/home'); // Redirect to homepage
    };

    return (
        <Container maxWidth="sm" sx={{ textAlign: 'center', padding: '50px 0' }}>
            <Typography variant="h2" component="h1" gutterBottom>
                Page Not Found
            </Typography>
            <Typography variant="h5" component="h2" gutterBottom>
                Error: Page Not Found
            </Typography>
            <Typography variant="body1" gutterBottom>
                The page you are looking for cannot be found.
            </Typography>
            <Typography variant="body2" color="textSecondary" gutterBottom>
                If you believe you reached this page in error, and the resource you are trying to access should exist, contact technical support.
            </Typography>
            <Button variant="contained" color="primary" onClick={handleGoHome} sx={{ marginTop: '20px' }}>
                Go to Homepage
            </Button>
        </Container>
    );
}

export default PageNotFound;
