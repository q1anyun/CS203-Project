import React, { useState } from 'react';
import { Container, TextField, Card, Grid2, InputAdornment, IconButton, Typography, Button } from '@mui/material';
import { useNavigate } from 'react-router-dom';

import styles from './Settings.module.css';
import LockIcon from '@mui/icons-material/Lock';
import VisibilityIcon from '@mui/icons-material/Visibility';
import VisibilityOffIcon from '@mui/icons-material/VisibilityOff';

import { handleSubmitChanges, handleClickShowPassword } from './SettingsFunctions';

function Settings() {
  const [oldPassword, setOldPassword] = useState('');
  const [newPassword, setNewPassword] = useState('');
  const [showOldPassword, setShowOldPassword] = useState(false);
  const [showNewPassword, setShowNewPassword] = useState(false);
  const [error, setError] = useState('');

  const navigate = useNavigate();

  return (
    <div>
      <Container maxWidth="sm" className={styles.cardContainer}>
        <Card variant="outlined" className={styles.cardStyle}>
          <Typography variant='header2' marginBottom={'20px'}>Change Password</Typography>

          <Grid2 container spacing={4}>
            <Grid2 size={12}>
              <TextField
                variant="outlined"
                fullWidth
                label="Old Password"
                type={showOldPassword ? 'text' : 'password'}
                autoComplete="old-password"
                placeholder="Enter old password"
                value={oldPassword}
                onChange={(e) => setOldPassword(e.target.value)}
                InputProps={{
                  startAdornment: (
                    <InputAdornment position="start">
                      <LockIcon />
                    </InputAdornment>
                  ),
                  endAdornment: (
                    <InputAdornment position="end">
                      <IconButton
                        onClick={() => handleClickShowPassword(setShowOldPassword)}
                        edge="end"
                      >
                        {showOldPassword ? <VisibilityIcon /> : <VisibilityOffIcon />}


                      </IconButton>
                    </InputAdornment>
                  ),
                }}
              />
            </Grid2>
            <Grid2 size={12}>
              <TextField
                variant="outlined"
                fullWidth
                label="New Password"
                type={showNewPassword ? 'text' : 'password'}
                autoComplete="new-password"
                placeholder="Enter new password"
                value={newPassword}
                onChange={(e) => setNewPassword(e.target.value)}
                InputProps={{
                  startAdornment: (
                    <InputAdornment position="start">
                      <LockIcon />
                    </InputAdornment>
                  ),
                  endAdornment: (
                    <InputAdornment position="end">
                      <IconButton
                        onClick={() => setShowNewPassword(prev => !prev)} // Toggle function
                        edge="end"
                      >
                        {showNewPassword ? <VisibilityIcon /> : <VisibilityOffIcon />}
                      </IconButton>
                    </InputAdornment>
                  ),
                }}
              />
            </Grid2>

            {error && (
              <Grid2 size={12}>
                <Typography variant="body4" className={styles.errorMessage}>
                  {error}
                </Typography>
              </Grid2>
            )}

            <Grid2 size={12}>
              <Button variant="contained" color="primary" className={styles.gradientButton}
                onClick={(e) => handleSubmitChanges(e, oldPassword, newPassword, setError, navigate)}>
                <Typography variant='homePage2'>Save Changes</Typography>
              </Button>
            </Grid2>
          </Grid2>
        </Card>
      </Container>
    </div>
  );
}

export default Settings;