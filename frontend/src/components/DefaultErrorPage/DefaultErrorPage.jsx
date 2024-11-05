import React, {useEffect} from 'react';
import { Container, Typography } from '@mui/material';
import { useLocation, useNavigate } from 'react-router-dom';

function DefaultErrorPage() {
    const location = useLocation();
    const query = new URLSearchParams(location.search);
    const statusCode = query.get('statusCode');
    const errorMessage = query.get('errorMessage') || 'Something went wrong.';
    const navigate = useNavigate();
    
    useEffect(() => {
        if (statusCode === '403') {
            navigate('/login');
        }
    }, [statusCode, navigate]);

    return (
        <Container>
            <Typography variant="h2" color="error">Oops!</Typography>
            <Typography variant="h5">
                {statusCode ? `Error ${statusCode}: ${errorMessage}` : 'An unexpected error occurred.'}
            </Typography>
            <Typography variant="body1">Please try again later or contact support.</Typography>
        </Container>
    );
}

export default DefaultErrorPage;
