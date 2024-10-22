import { React, useState } from 'react';
import { Container, Card, Grid2, CircularProgress, Link } from '@mui/material';
import { useNavigate } from 'react-router-dom';
import OtpInput from 'react-otp-input';
import axios from 'axios';
import styles from './AuthPage.module.css';
import logoImage from '../../assets/chess_logo.png';

const otpURL = import.meta.env.VITE_OTP_SERVICE_URL;
const authURL = import.meta.env.VITE_AUTH_SERVICE_URL;

function AuthPage() {
    const [otp, setOtp] = useState('');
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState('');
    const pendingRegistration = JSON.parse(sessionStorage.getItem('pendingRegistration'));
    const navigate = useNavigate();

    const handleVerify = async () => {
        try {
            if (!pendingRegistration) {
                console.error('No pending registration found in sessionStorage.');
                return;
            }

            const email = pendingRegistration.email;

            const otpResponse = await axios.post(`${otpURL}/validate`, { email, otp });
            if (otpResponse.status === 200) {
                const registerResponse = await axios.post(`${authURL}/register/player`, pendingRegistration);
                if (registerResponse.status === 200) {
                    setError('');
                    setLoading(true);

                    setTimeout(() => {
                        navigate('/login');
                    }, 2000);
                }
            }
        } catch (err) {
            if (err.response.status === 400) {
                setError('Invalid OTP or OTP expired.');
            }
        }
    };

    const handleResend = async () => {
        try {
            const otpResponse = await axios.post(`${otpURL}/resend`, {
                username: pendingRegistration.username,
                email: pendingRegistration.email
            });

            if (otpResponse.status === 200) {
                console.log('OTP resent successfully');
                alert('A new OTP has been sent to your email.');
            } else {
                console.error('Failed to resend OTP:', otpResponse.data);
                alert('Failed to resend OTP. Please try again.');
            }
        } catch (error) {
            if (error.response) {
                alert(error.response.data.message || 'An error occurred. Please try again.');
            } else {
                alert('An error occurred. Please check your network connection and try again.');
            }
        }
    };

    const email = pendingRegistration ? pendingRegistration.email : '';

    return (
        <div className={styles.authContainer}>
            <Container maxWidth="sm" className={styles.cardContainer}>
                <Card variant="outlined" className={styles.cardStyle}>
                    <img src={logoImage} alt="logo" className={styles.logoImage} />
                    <br></br>
                    <h1>Two Step Verification</h1>
                    <p>We sent a verification code to your email: <strong>{email}</strong></p>
                    <p>Type your 6 digit security code</p>
                    <Grid2 container spacing={3}>
                        <OtpInput
                            value={otp}
                            onChange={setOtp}
                            numInputs={6}
                            renderSeparator={<span>-</span>}
                            renderInput={(props) => (
                                <input
                                    {...props}
                                    style={{
                                        width: '50px',
                                        height: '50px',
                                        fontSize: '20px',
                                        textAlign: 'center'
                                    }}
                                />
                            )}

                        />
                        {error && <p className={styles.errorMessage}>{error}</p>}
                        <Grid2 size={12}>
                            <button type="submit" className={styles.gradientButton} onClick={handleVerify}>
                                Verify
                            </button>
                        </Grid2>
                        <p>Didn't get the code?
                            <Link
                                href="#"
                                onClick={handleResend}
                                style={{ marginLeft: '5px', color: 'blue', textDecoration: 'underline' }}
                            >
                                Resend
                            </Link></p>
                    </Grid2>
                    {loading && (
                        <Grid2 container justifyContent="center" style={{ marginTop: '20px' }}>
                            <CircularProgress />
                            <p>Redirecting to login page...</p>
                        </Grid2>
                    )}
                </Card>
            </Container>
        </div>
    );
}

export default AuthPage;
