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
            // Successfully updated credentials
            console.log("Successfully updated credentials");
            alert("Credentials updated successfully!"); // Notify user
            navigate('/home'); // Redirect to a dashboard or appropriate page
        } else {
            // Handle unexpected response
            setError('Failed to update credentials. Please try again.');
        }
    } catch (error) {
        if (error.response) {
            // Check if error response has specific message
            setError(error.response.data.message || 'Error updating credentials');
        } else {
            setError('Network error, please check your connection and try again.');
        }
        console.error('Error updating credentials:', error);
    }
};
