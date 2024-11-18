import axios from 'axios';
import useHandleError from '../Hooks/useHandleError';

const baseURL = import.meta.env.VITE_AUTH_SERVICE_URL;
//const handleError = useHandleError();


export const handleClickShowPassword = (setShowPassword) => {
    setShowPassword(prev => !prev);
};

export const handleSubmit = async (e, username, password, navigate, setError, handleError) => {
    e.preventDefault();
    
    try {
        const response = await axios.post(`${baseURL}/session`, { username, password });

        const { role, jwtResponse } = response.data;
        const { token, expiresIn } = jwtResponse;

        localStorage.setItem('token', token);
        const expirationTime = Date.now() + expiresIn * 1000;
        localStorage.setItem('tokenExpiration', expirationTime);
        localStorage.setItem('role', role);

        navigate('/home');

    } catch (err) {
        setError(err); 
        handleError(err); 
    }
};