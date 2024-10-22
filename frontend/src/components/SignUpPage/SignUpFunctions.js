import axios from 'axios';

const baseURL = import.meta.env.VITE_OTP_SERVICE_URL;

export const handleClickShowPassword = (setShowPassword) => {
    setShowPassword(prev => !prev);
};

export const handleChange = (e, setFormData) => {
    const { name, value } = e.target;
    setFormData(prevData => ({
        ...prevData,
        [name]: value
    }));
};

export const handleLoginClick = (navigate) => {
    navigate('/login');
};

export const handleSubmit = async (e, formData, setFormData, setErrors, setShowAlert, navigate) => {
    e.preventDefault();

    let formErrors = {};

    if (!formData.username) formErrors.username = 'Username is required';
    if (!formData.firstName) formErrors.firstName = 'First name is required';
    if (!formData.lastName) formErrors.lastName = 'Last name is required';
    if (!formData.email) formErrors.email = 'Email is required';
    if (!formData.password) formErrors.password = 'Password is required';

    const emailRegex = /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/;
    if (formData.email && !emailRegex.test(formData.email)) {
        formErrors.email = 'Please enter a valid email address';
    }

    const passwordRegex = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\W).{8,}$/;
    if (formData.password && !passwordRegex.test(formData.password)) {
        formErrors.password = 'Password must be at least 8 characters long, contain an uppercase letter, a lowercase letter, and a special character.';
    }

    setErrors(formErrors);

    if (Object.keys(formErrors).length > 0) return;

    try {
        const otpResponse = await axios.post(`${baseURL}/send`, { username: formData.username, email: formData.email });
        if (otpResponse.status === 200) {
            navigate('/verification');
            sessionStorage.setItem('pendingRegistration', JSON.stringify(formData));
        }
    } catch (err) {
        if (err.response) {
            const statusCode = err.response.status;
            const errorMessage = err.response.data.message;

            if (statusCode === 409) {
                if (errorMessage.includes('Username')) {
                    setErrors(prevErrors => ({
                        ...prevErrors,
                        username: errorMessage
                    }));
                }

                if (errorMessage.includes('Email')) {
                    setErrors(prevErrors => ({
                        ...prevErrors,
                        email: errorMessage
                    }));
                }
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
