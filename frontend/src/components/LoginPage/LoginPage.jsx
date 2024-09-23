import { React, useState } from 'react';
import { Container,TextField, Card, Link, Grid2, InputAdornment, IconButton } from '@mui/material';
import { useNavigate } from 'react-router-dom';
import axios from 'axios';

import styles from './LoginPage.module.css';
import logoImage from '../../assets/chess_logo.png';
import PersonIcon from '@mui/icons-material/Person';
import LockIcon from '@mui/icons-material/Lock';
import VisibilityIcon from '@mui/icons-material/Visibility';
import VisibilityOffIcon from '@mui/icons-material/VisibilityOff';
import ForgotPasswordDialog from './ForgotPasswordDialog';

const baseURL = import.meta.env.VITE_USER_SERVICE_URL;

function LoginPage() {
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [showPassword, setShowPassword] = useState(false);
  const [openDialog, setOpenDialog] = useState(false);
  const [error, setError] = useState('');
  const navigate = useNavigate();

  const handleClickShowPassword = () => {
    setShowPassword((prev) => !prev);
  };

  const handleDialogOpen = () => {
    setOpenDialog(true);
  };

  const handleDialogClose = () => {
    setOpenDialog(false);
  };

  const handleLogin = async () => {
    try {
      const response = await axios.post(`${baseURL}/api/auth/login`, { username, password });

      // Store the token and expiration time in local storage
      const { token, expiresIn, role } = response.data;
      localStorage.setItem('token', token);
      const expirationTime = Date.now() + expiresIn * 1000;
      localStorage.setItem('tokenExpiration', expirationTime);
      localStorage.setItem('role', role);

      // navigate to home page
      navigate('/home');

    } catch (err) {
      if (err.response) {
        if (err.response.status === 404 || err.response.status === 403) {
          setError('Invalid username or password');
        }else{
          navigate(`/error?statusCode=${statusCode}&errorMessage=${encodeURIComponent(errorMessage)}`);
        }
      } else if (err.request) {
        navigate(`/error?statusCode=0&errorMessage=${encodeURIComponent('No response from server')}`);
      } else {
        navigate(`/error?statusCode=500&errorMessage=${encodeURIComponent('Error: ' + err.message)}`);
      }
    }
  };

  return (
    <div className={styles.loginContainer}>
      <Container maxWidth="sm" className={styles.cardContainer}>
        <Card variant="outlined" className={styles.cardStyle}>
          <img src={logoImage} alt="logo" className={styles.logoImage} />
          <h1 className={styles.signInText}>SIGN IN</h1>

          <Grid2 container spacing={3}>
            <Grid2 size={12}>
              <TextField
                variant="outlined"
                fullWidth
                label="Username"
                autoComplete="username"
                placeholder="example@gmail.com"
                required
                value={username}
                onChange={(e) => setUsername(e.target.value)}
                InputProps={{
                  startAdornment: (
                    <InputAdornment position="start">
                      <PersonIcon />
                    </InputAdornment>
                  ),
                }}
              />
            </Grid2>

            <Grid2 size={12}>
              <TextField
                variant="outlined"
                fullWidth
                label="Password"
                type={showPassword ? 'text' : 'password'}
                autoComplete="current-password"
                placeholder="********"
                required
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                InputProps={{
                  startAdornment: (
                    <InputAdornment position="start">
                      <LockIcon />
                    </InputAdornment>
                  ),
                  endAdornment: (
                    <InputAdornment position="end">
                      <IconButton
                        onClick={handleClickShowPassword}
                        edge="end"
                      >
                        {showPassword ? <VisibilityIcon /> : <VisibilityOffIcon />}
                      </IconButton>
                    </InputAdornment>
                  ),
                }}
              />
            </Grid2>

            {error && (
              <Grid2 size={12}>
                <h6 className={styles.errorMessage}>
                  {error}
                </h6>
              </Grid2>
            )}

            {/*This is not functional yet*/}
            <Grid2 size={12} className={styles.rightContainer}>
              <Link onClick={handleDialogOpen} className={styles.forgotPasswordLinkStyle}>
                Forgot password?
              </Link>
            </Grid2>

            <Grid2 size={12}>
              <button type="submit" className={styles.gradientButton}
                onClick={handleLogin}>
                Sign In
              </button>
            </Grid2>

            <Grid2 size={12} className={styles.rightContainer}>
              <Link variant="body2" onClick={() => navigate('/signup')} className={styles.linkStyle}>
                Don't have an account? Sign Up
              </Link>
            </Grid2>
          </Grid2>
        </Card>
      </Container>

      <ForgotPasswordDialog open={openDialog} onClose={handleDialogClose} />
    </div>
  );
}

export default LoginPage;
