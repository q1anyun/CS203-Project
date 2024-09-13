import { React, useState } from 'react';
import { Container, TextField, Button, Grid2, Typography, Card, Link, InputAdornment } from '@mui/material';
import { useNavigate } from 'react-router-dom';
// import axios from 'axios';
import styles from './SignUpPage.module.css';
import logoImage from '../../assets/chess_logo.png';
import EmailIcon from '@mui/icons-material/Email';
import PersonIcon from '@mui/icons-material/Person';
import LockIcon from '@mui/icons-material/Lock';
import BadgeIcon from '@mui/icons-material/Badge';

function SignUpPage() {
    const navigate = useNavigate();

    const [formData, setFormData] = useState({
        username: '',
        firstName: '',
        lastName: '',
        email: '',
        password: ''
    });

    const [errors, setErrors] = useState({});

    const handleChange = (e) => {
        const { name, value } = e.target;
        setFormData({ ...formData, [name]: value });
    };

    const handleSubmit = async (e) => {
        e.preventDefault();

        let formErrors = {};
        if (!formData.username) formErrors.username = 'Username is required';
        if (!formData.firstName) formErrors.firstName = 'First name is required';
        if (!formData.lastName) formErrors.lastName = 'Last name is required';
        if (!formData.password) formErrors.password = 'Password is required';

        const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
        if (!formData.email) {
            formErrors.email = 'Email is required';
        } else if (!emailRegex.test(formData.email)) {
            formErrors.email = 'Invalid email address';
        }

        setErrors(formErrors);

        if (Object.keys(formErrors).length > 0) return;

        try {
            const response = await axios.post('/api/signup', formData);
            // Handle success
            navigate('/login');
        } catch (error) {
            if (error.response && error.response.data) {
                const backendErrors = error.response.data.errors || {};
                setErrors({
                    ...formErrors,
                    ...backendErrors,
                    general: 'Failed to sign up. Please try again.'
                });
            } else {
                setErrors({ ...formErrors, general: 'Failed to sign up. Please try again.' });
            }
        }
    };

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
                                        name="username"
                                        value={formData.username}
                                        onChange={handleChange}
                                        error={Boolean(errors.username)}
                                        helperText={errors.username}
                                        InputProps={{
                                            startAdornment: (
                                                <InputAdornment position="start">
                                                    <PersonIcon />
                                                </InputAdornment>
                                            ),
                                        }}
                                    />
                                </Grid2>

                                <Grid2 size={6}>
                                    <TextField
                                        fullWidth
                                        label="First Name"
                                        variant="outlined"
                                        placeholder="Bob"
                                        required
                                        name="firstName"
                                        value={formData.firstName}
                                        onChange={handleChange}
                                        error={Boolean(errors.firstName)}
                                        helperText={errors.firstName}
                                        InputProps={{
                                            startAdornment: (
                                                <InputAdornment position="start">
                                                    <BadgeIcon />
                                                </InputAdornment>
                                            ),
                                        }}
                                    />
                                </Grid2>

                                <Grid2 size={6}>
                                    <TextField
                                        fullWidth
                                        label="Last Name"
                                        variant="outlined"
                                        placeholder="Tan"
                                        required
                                        name="lastName"
                                        value={formData.lastName}
                                        onChange={handleChange}
                                        error={Boolean(errors.lastName)}
                                        helperText={errors.lastName}
                                        InputProps={{
                                            startAdornment: (
                                                <InputAdornment position="start">
                                                    <BadgeIcon />
                                                </InputAdornment>
                                            ),
                                        }}
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
                                        name="email"
                                        value={formData.email}
                                        onChange={handleChange}
                                        error={Boolean(errors.email)}
                                        helperText={errors.email}
                                        InputProps={{
                                            startAdornment: (
                                                <InputAdornment position="start">
                                                    <EmailIcon />
                                                </InputAdornment>
                                            ),
                                        }}
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
                                        name="password"
                                        value={formData.password}
                                        onChange={handleChange}
                                        error={Boolean(errors.password)}
                                        helperText={errors.password}
                                        InputProps={{
                                            startAdornment: (
                                                <InputAdornment position="start">
                                                    <LockIcon />
                                                </InputAdornment>
                                            ),
                                        }}
                                    />
                                </Grid2>

                                {errors.general && (
                                    <Grid size={12}>
                                        <Typography color="error" variant="body2" align="center">
                                            {errors.general}
                                        </Typography>
                                    </Grid>
                                )}

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
                                        onClick={handleSubmit}
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