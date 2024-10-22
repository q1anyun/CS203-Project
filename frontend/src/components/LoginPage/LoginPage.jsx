import { React, useState } from 'react';
import { Container, TextField, Card, Link, Grid2, InputAdornment, IconButton } from '@mui/material';
import { useNavigate } from 'react-router-dom';

import styles from './LoginPage.module.css';
import logoImage from '../../assets/chess_logo.png';
import PersonIcon from '@mui/icons-material/Person';
import LockIcon from '@mui/icons-material/Lock';
import VisibilityIcon from '@mui/icons-material/Visibility';
import VisibilityOffIcon from '@mui/icons-material/VisibilityOff';
import ForgotPasswordDialog from './ForgotPasswordDialog';

// js
import { handleClickShowPassword, handleDialogClose, handleSubmit } from './LoginFunctions';

const baseURL = import.meta.env.VITE_USER_SERVICE_URL;

function LoginPage() {
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [showPassword, setShowPassword] = useState(false);
  const [openDialog, setOpenDialog] = useState(false);
  const [error, setError] = useState('');

  const navigate = useNavigate();

  return (
    <div className={styles.loginContainer}>
      <Container maxWidth="sm" className={styles.cardContainer}>
        <Card variant="outlined" className={styles.cardStyle}>
          <img src={logoImage} alt="logo" className={styles.logoImage} />
          <h1 className={styles.signInText}>SIGN IN</h1>

          <Grid2 container spacing={4}>
            <Grid2 size={12}>
              <TextField
                variant="outlined"
                fullWidth
                label="Username"
                autoComplete="username"
                placeholder="chesspro321"
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

            {error && (
              <Grid2 size={12}>
                <h6 className={styles.errorMessage}>
                  {error}
                </h6>
              </Grid2>
            )}

            <Grid2 size={12}>
              <button type="submit" className={styles.gradientButton}
                onClick={(e) => handleSubmit(e, username, password, navigate, setError)}>
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

      <ForgotPasswordDialog open={openDialog} onClose={() => handleDialogClose(setOpenDialog)} />
    </div>
  );
}

export default LoginPage;
