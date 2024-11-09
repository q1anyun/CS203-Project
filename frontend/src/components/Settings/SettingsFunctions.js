import axios from 'axios';

const baseURL = import.meta.env.VITE_USER_SERVICE_URL; // Base URL for API calls

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
        if (error.response) {
            const statusCode = error.response.status;
            const errorMessage = error.response.data?.message || 'An unexpected error occurred';
            alert(`Error: ${errorMessage}`);
        } else if (error.request) {
            navigate(`/error?statusCode=0&errorMessage=${encodeURIComponent('No response from server')}`);
        } else {
            navigate(`/error?statusCode=500&errorMessage=${encodeURIComponent('Error: ' + error.message)}`);
        }
    }
};
