import { useNavigate } from 'react-router-dom';

const useHandleError = () => {
  const navigate = useNavigate();

  const handleError = (error) => {
    if (error.response) {
      const statusCode = error.response.status;
      const errorMessage = error.response.data?.message || 'An unexpected error occurred';
      navigate(`/error?statusCode=${statusCode}&errorMessage=${encodeURIComponent(errorMessage)}`);
    } else if (error.request) {
      navigate(`/error?statusCode=0&errorMessage=${encodeURIComponent('No response from server')}`);
    } else {
      navigate(`/error?statusCode=500&errorMessage=${encodeURIComponent('Error: ' + error.message)}`);
    }
  };

  return handleError;
};

export default useHandleError;