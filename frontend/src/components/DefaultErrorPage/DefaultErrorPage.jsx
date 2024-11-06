import React, { useEffect } from 'react';
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
        <Container
            style={{
                display: 'flex',
                flexDirection: 'column',
                alignItems: 'center',
            }}>
            <Typography variant="header1" style={{ color: '#c1a01e' }}>Oops!</Typography>
            <Typography variant="header2">
                {statusCode ? `Error ${statusCode}: ${errorMessage}` : 'An unexpected error occurred.'}
            </Typography>
            <Typography variant="body1">Please try again later or contact support at chessmvppp@gmail.com</Typography>
        </Container>
    );
}

export default DefaultErrorPage;
