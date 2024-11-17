import axios from 'axios';
import useHandleError from '../Hooks/useHandleError';

const baseURL = import.meta.env.VITE_USER_SERVICE_URL;

// Function to handle toggling the visibility of the password
export const handleClickShowPassword = (setShowPassword) => {
    setShowPassword(prevShowPassword => !prevShowPassword);
};

// Function to handle submitting the updated password
export const handleSubmitChanges = async (event, oldPassword, newPassword, setError, navigate) => {
    event.preventDefault();

    const token = localStorage.getItem('token'); // Retrieve JWT from local storage

    if (!token) {
        setError('Authentication token is missing. Please log in again.');
        return;
    }

    const headers = {
        'Authorization': `Bearer ${token}`,
        'Content-Type': 'application/json'
    };

    const body = {
        oldPassword,
        newPassword,
    };

    try {
        const response = await axios.put(`${baseURL}/current`, body, { headers });
        if (response.status === 200) {
            console.log("Successfully updated credentials");
            alert("Credentials updated successfully!");
            navigate('/home');
        }
    } catch (error) {
        useHandleError(error);
    }
};
