import React from 'react';
import { Container, TextField, Button, Grid2, Typography, Card, Link } from '@mui/material';
import { useNavigate } from 'react-router-dom';
import styles from './SignUpPage.module.css';
import logoImage from '../../assets/chess_logo.png';

function SignUpPage() {
    const navigate = useNavigate();

    return (
        <div className={styles.signUpContainer}>
            <Container
                component="main"
                maxWidth="lg"
                sx={{ display: 'flex', flexDirection: 'row', justifyContent: 'center', height: '100vh' }}

            >
                <Grid2 container spacing={8}>
                    <Grid2 size={6} sx={{ display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
                        <Typography
                            variant="h4"
                            sx={{ textAlign: 'center', fontFamily: 'PT Sans, sans-serif', padding: 3, color: '#fff' }}
                        >
                            {/* Add your desired text here */}
                            <Typography variant="h2" sx={{ fontWeight: 'bold', fontFamily: 'PT Serif, serif' }}>
                                Welcome to <span style={{ color: '#c1a01e' }}>CHESS
                                    <img
                                        src={logoImage}
                                        alt="logo"
                                        style={{ margin: '0 10px', height: '40px' }}
                                    />MVP</span>
                            </Typography>
                            <Typography variant="body1" sx={{ marginTop: 2, fontFamily: 'PT sans, sans-serif', fontSize: 18 }}>
                                Join our community of chess enthusiasts. Create your account to start playing and competing.
                            </Typography>
                        </Typography>
                    </Grid2>

                    <Grid2 size={6} sx={{ display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
                        <Card variant="outlined" sx={{ padding: 3, width: '100%', borderRadius: '16px', backgroundColor: 'rgba(255, 255, 255, 0.8)', backdropFilter: 'blur(8px)' }}>
                            <Grid2>
                                <Typography
                                    variant="h3"
                                    component="div"
                                    sx={{ fontFamily: 'PT Serif, serif', color: '#c1a01e' }}
                                >
                                    <span style={{ fontWeight: 'bold' }}>CHESS</span>
                                    <img
                                        src={logoImage}
                                        alt="logo"
                                        style={{ margin: '0 10px', height: '40px' }}
                                    />
                                    <span style={{ fontWeight: 'normal' }}>MVP</span>
                                </Typography>
                            </Grid2>

                            <Typography
                                component="h1"
                                variant="h4"
                                sx={{ textAlign: 'center', marginBottom: 2, marginTop: 4, fontFamily: 'PT Sans, sans-serif' }}
                            >
                                SIGN UP
                            </Typography>
                            <Grid2 container spacing={3}>

                                <Grid2 size={12}>
                                    <TextField
                                        fullWidth
                                        label="Username"
                                        variant="outlined"
                                        placeholder="chesspro321"
                                        required
                                    />
                                </Grid2>

                                <Grid2 size={6}>
                                    <TextField
                                        fullWidth
                                        label="First Name"
                                        variant="outlined"
                                        placeholder="Bob"
                                        required
                                    />
                                </Grid2>

                                <Grid2 size={6}>
                                    <TextField
                                        fullWidth
                                        label="Last Name"
                                        variant="outlined"
                                        placeholder="Tan"
                                        required
                                    />
                                </Grid2>

                                <Grid2 size={12}>
                                    <TextField
                                        fullWidth
                                        label="Email"
                                        type="email"
                                        variant="outlined"
                                        placeholder="example@gmail.com"
                                        required
                                    />
                                </Grid2>

                                <Grid2 size={12}>
                                    <TextField
                                        fullWidth
                                        label="Password"
                                        type="password"
                                        variant="outlined"
                                        placeholder="********"
                                        required
                                    />
                                </Grid2>

                                <Grid2 size={12} sx={{ textAlign: 'right' }}>
                                    <Link
                                        variant="body2"
                                        sx={{ fontFamily: 'PT Sans, sans-serif', fontSize: 16 }}
                                        onClick={() => navigate('/login')}
                                        style={{ cursor: 'pointer' }}
                                    >
                                        Already have an account? Sign In
                                    </Link>
                                </Grid2>

                                <Grid2 size={12}>
                                    <Button
                                        type="submit"
                                        variant="contained"
                                        className={styles.gradientButton}
                                        fullWidth
                                        sx={{ fontFamily: 'PT Sans, sans-serif', fontSize: 20 }}
                                    >
                                        Sign Up
                                    </Button>
                                </Grid2>
                            </Grid2>
                        </Card>
                    </Grid2>
                </Grid2>
            </Container>
        </div>
    );
}

export default SignUpPage;
