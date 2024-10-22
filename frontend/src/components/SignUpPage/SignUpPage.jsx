import { React, useState, useMemo } from 'react';
import { useNavigate } from 'react-router-dom';
import { Container, TextField, Grid2, Card, Link, InputAdornment, IconButton, Alert, AlertTitle, Select, MenuItem, FormControl, InputLabel, Box, Button } from '@mui/material';
import countryList from 'react-select-country-list'

// Icons
import EmailIcon from '@mui/icons-material/Email';
import PersonIcon from '@mui/icons-material/Person';
import LockIcon from '@mui/icons-material/Lock';
import BadgeIcon from '@mui/icons-material/Badge';
import VisibilityIcon from '@mui/icons-material/Visibility';
import VisibilityOffIcon from '@mui/icons-material/VisibilityOff';

// Local assets and styles
import styles from './SignUpPage.module.css';
import logoImage from '../../assets/chess_logo.png';
import profilePic from '../../assets/default_user.png';

// js
import { handleClickShowPassword, handleChange, handleLoginClick, handleSubmit } from './SignUpFunctions';

function SignUpPage() {
    const [showPassword, setShowPassword] = useState(false); // password toggle visibility
    const [errors, setErrors] = useState({}); // display error messages
    const [showAlert, setShowAlert] = useState(false); // successful sign up
    const options = useMemo(() => countryList().getData(), [])
    const [formData, setFormData] = useState({ // sign up form
        username: '',
        firstName: '',
        lastName: '',
        country: 'SG',
        email: '',
        password: '',
        profilePicture: profilePic
    });

    const navigate = useNavigate(); // to navigate to other pages

    return (
        <div className={styles.signUpBgContainer}>
            <Container maxWidth="xl" className={styles.signUpContainer}>
                <Grid2 container spacing={10}>
                    <Grid2 size={6} className={styles.container}>
                        <Container maxWidth="sm" className={styles.welcomeContainer}>
                            <h1 className={styles.greetings}>
                                Welcome to <span className={styles.brand}>CHESS
                                    <img src={logoImage} alt="logo" className={styles.logoImage} />MVP</span>
                            </h1>
                            <p className={styles.description}>
                                Join our community of chess enthusiasts.<br></br>Create your account to start playing and competing.
                            </p>
                        </Container>
                    </Grid2>

                    <Grid2 size={6} className={styles.container}>
                        <Container maxWidth="sm" className={styles.cardContainer}>
                            <Card variant="outlined" className={styles.card}>
                                <img src={logoImage} alt="logo" className={styles.logoImage} />
                                <h1 className={styles.signUpText}>SIGN UP</h1>

                                <Grid2 container spacing={3}>
                                    {showAlert && ( // successful login alert
                                        <Grid2 size={12} className={styles.alertContainer}>
                                            <Alert severity="success">
                                                <AlertTitle>Account Created</AlertTitle>
                                                Thank you for signing up! Please proceed to{' '}
                                                <Link
                                                    onClick={(e) => {
                                                        e.preventDefault();
                                                        handleLoginClick(navigate); // Navigate to the login page
                                                    }}
                                                    className={styles.loginLink}
                                                >
                                                    login
                                                </Link>.
                                            </Alert>
                                        </Grid2>
                                    )}

                                    {/*Username field*/}
                                    <Grid2 size={12}>
                                        <TextField
                                            fullWidth
                                            label="Username"
                                            variant="outlined"
                                            placeholder="chesspro321"
                                            required
                                            name="username"
                                            value={formData.username}
                                            onChange={(e) => handleChange(e, setFormData)}
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

                                    {/*First Name field*/}
                                    <Grid2 size={6}>
                                        <TextField
                                            fullWidth
                                            label="First Name"
                                            variant="outlined"
                                            placeholder="Bob"
                                            required
                                            name="firstName"
                                            value={formData.firstName}
                                            onChange={(e) => handleChange(e, setFormData)}
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

                                    {/*Last Name field*/}
                                    <Grid2 size={6}>
                                        <TextField
                                            fullWidth
                                            label="Last Name"
                                            variant="outlined"
                                            placeholder="Tan"
                                            required
                                            name="lastName"
                                            value={formData.lastName}
                                            onChange={(e) => handleChange(e, setFormData)}
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

                                    {/*Country field*/}
                                    <Grid2 size={12}>
                                        <FormControl fullWidth>
                                            <InputLabel>Country</InputLabel>
                                            <Select
                                                name="country"
                                                value={formData.country}
                                                label="Country"
                                                sx={{ textAlign: 'left' }}
                                                MenuProps={{
                                                    PaperProps: {
                                                        style: {
                                                            maxHeight: 200,
                                                            width: 'auto',
                                                        },
                                                    },
                                                }}
                                                onChange={(e) => handleChange(e, setFormData)}
                                            >
                                                {options.map((country) => (
                                                    <MenuItem key={country.value} value={country.value}>
                                                        {country.label}
                                                    </MenuItem>
                                                ))}
                                            </Select>
                                        </FormControl>
                                    </Grid2>

                                    {/*Email field*/}
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
                                            onChange={(e) => handleChange(e, setFormData)}
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

                                    {/*Password field*/}
                                    <Grid2 size={12}>
                                        <TextField
                                            fullWidth
                                            label="Password"
                                            type={showPassword ? 'text' : 'password'}
                                            variant="outlined"
                                            placeholder="********"
                                            required
                                            name="password"
                                            value={formData.password}
                                            onChange={(e) => handleChange(e, setFormData)}
                                            error={Boolean(errors.password)}
                                            helperText={errors.password}
                                            InputProps={{
                                                startAdornment: (
                                                    <InputAdornment position="start">
                                                        <LockIcon />
                                                    </InputAdornment>
                                                ),
                                                endAdornment: (
                                                    <InputAdornment position="end">
                                                        <IconButton
                                                            onClick={() => handleClickShowPassword(setShowPassword)}
                                                            edge="end"
                                                        >
                                                            {showPassword ? <VisibilityIcon /> : <VisibilityOffIcon />}
                                                        </IconButton>
                                                    </InputAdornment>
                                                ),
                                            }}
                                        />
                                    </Grid2>

                                    <Grid2 size={12}>
                                        <button type="submit" className={styles.gradientButton}
                                            onClick={(e) => handleSubmit(e, formData, setFormData, setErrors, setShowAlert, navigate)}>
                                            Sign Up
                                        </button>
                                    </Grid2>

                                    <Grid2 size={12} className={styles.toLoginLink}>
                                        <Link onClick={() => navigate('/login')} className={styles.link}>
                                            Already have an account? Sign In
                                        </Link>
                                    </Grid2>
                                </Grid2>
                            </Card>
                        </Container>
                    </Grid2>
                </Grid2>
            </Container>
        </div>
    );
}

export default SignUpPage;