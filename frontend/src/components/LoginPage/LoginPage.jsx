import { React, useState } from 'react';
import { Container, Typography, TextField, Button, Card, Link, Grid2, InputAdornment } from '@mui/material';
import { useNavigate } from 'react-router-dom';
import styles from './LoginPage.module.css';
// import axios from 'axios';
import logoImage from '../../assets/chess_logo.png';
import PersonIcon from '@mui/icons-material/Person';
import LockIcon from '@mui/icons-material/Lock';

function LoginPage() {
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const navigate = useNavigate();

  const handleLogin = async () => {
    try {
      const response = await axios.post('/api/login', { username, password });
      const { token } = response.data;
      localStorage.setItem('token', token);
      navigate('/home');
    } catch (err) {
      console.log("ERROR");
      setError('Invalid username or password');
      console.error(err);
    }
  };

  return (
    <div className={styles.loginContainer}>
      <Container
        component="main"
        maxWidth="sm"
        sx={{ display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center', height: '100vh' }}
      >
        <Card variant="outlined"

          sx={{
            padding: 3,
            width: '100%',
            borderRadius: '16px',
            backgroundColor: 'rgba(255, 255, 255, 0.8)',
            backdropFilter: 'blur(8px)',
            height: '60%',
          }}
        >
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
            SIGN IN
          </Typography>
          <Grid2 container direction="column" spacing={2}>
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
                sx={{ marginBottom: 2 }}
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
                type="password"
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
                }}
              />
            </Grid2>
            {error && (
              <Grid2 item xs={12}>
                <Typography color="error" sx={{ textAlign: 'center' }}>
                  {error}
                </Typography>
              </Grid2>
            )}
            <Grid2 sx={{ textAlign: 'right' }}>
              <Link href="#" variant="body2" sx={{ fontFamily: 'PT Sans, sans-serif', fontSize: 16 }} onClick={() => navigate('/forgot-password')}>
                Forgot password?
              </Link>
            </Grid2>
            <Grid2 item xs={12}>
              <Button
                type="submit"
                variant="contained"
                className={styles.gradientButton}
                fullWidth
                sx={{ fontFamily: 'PT Sans, sans-serif', fontSize: 20 }}
                onClick={handleLogin}
              >
                Sign In
              </Button>
            </Grid2>

            <Grid2 size={12} sx={{ marginTop: 2, textAlign: 'right' }}>
              <Link
                variant="body2"
                sx={{ fontFamily: 'PT Sans, sans-serif', fontSize: 16 }}
                onClick={() => navigate('/signup')}
                style={{ cursor: 'pointer' }}
              >
                Don't have an account? Sign Up
              </Link>
            </Grid2>
          </Grid2>
        </Card>
      </Container >
    </div>
  );
}

export default LoginPage;