import axios from 'axios';

const baseURL = import.meta.env.VITE_AUTHENTICATION_SERVICE_URL;

export const handleClickShowPassword = (setShowPassword) => {
    setShowPassword(prev => !prev);
};

export const handleDialogOpen = (setOpenDialog) => {
    setOpenDialog(true);
};

export const handleDialogClose = (setOpenDialog) => {
    setOpenDialog(false);
};

export const handleSubmit = async (e, username, password, navigate, setError) => {
    e.preventDefault();
    try {
        const response = await axios.post(`${baseURL}/api/auth/login`, { username, password });
        
        console.log('Response Data:', response.data);
        const { role, jwtResponse } = response.data;
        const { token, expiresIn } = jwtResponse;
    
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
            } else {
                navigate(`/error?statusCode=${statusCode}&errorMessage=${encodeURIComponent(errorMessage)}`);
            }
        } else if (err.request) {
            navigate(`/error?statusCode=0&errorMessage=${encodeURIComponent('No response from server')}`);
        } else {
            navigate(`/error?statusCode=500&errorMessage=${encodeURIComponent('Error: ' + err.message)}`);
        }
    }
};