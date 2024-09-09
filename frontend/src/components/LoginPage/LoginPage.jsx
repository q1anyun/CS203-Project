import React from 'react';
import { Link } from 'react-router-dom';
import Button from 'react-bootstrap/Button'; 

function LoginPage() {
  return (
    <>
      <Link to="/home">
        <Button>Login</Button>
      </Link>

      <p>This login page is working in progress</p>

      <Link to="/signup">
        <Button>Sign Up</Button>
      </Link>
    </>
  );
}

export default LoginPage;
