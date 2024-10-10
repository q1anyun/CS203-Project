import { React, useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import Container from 'react-bootstrap/Container';
import Nav from 'react-bootstrap/Nav';
import Navbar from 'react-bootstrap/Navbar';
import NavDropdown from 'react-bootstrap/NavDropdown';
import styles from './NavBar.module.css';
import logoImage from '../../assets/chess_logo.png';
import Image from 'react-bootstrap/Image';
import useProfilePic from '../ProfilePicture/UseProfilePicture';
import { Typography } from '@mui/material';

function NavBar({profilePic}) {
  const [userRole, setUserRole] = useState(null);
  profilePic = useProfilePic(); 
  

  useEffect(() => {
    const role = localStorage.getItem('role');
    setUserRole(role);
  }, []);

  return (

    <Navbar expand="lg" className={styles.navbar}>
      <Container>
        <Navbar.Brand as={Link} to="/home">
          <span style={{ fontWeight: 'bold' }}>CHESS</span>
          <img src={logoImage} alt="chessMVPlogo" className={styles.logoImage}/>
          <span style={{ fontWeight: 'bold' }}>MVP</span>
        </Navbar.Brand>
        <Navbar.Toggle />

        <Nav className="me-auto">
          <Nav.Link as={Link} to="/home">
          <Typography variant="navBar">Home</Typography></Nav.Link>

          {userRole === 'ADMIN' && (
            <>
              <Nav.Link as={Link} to="/admin/tournaments">Tournaments</Nav.Link>
            </>
          )}

          {userRole === 'PLAYER' && (
            <>
              <Nav.Link as={Link} to="/player/tournaments">Tournaments</Nav.Link>
            </>
          )}

          <Nav.Link as={Link} to="/leaderboard">
          <Typography variant="navBar">Leaderboard</Typography></Nav.Link>
        </Nav>

        <Navbar.Collapse className="justify-content-end">
          <NavDropdown
            align="end"
            title={<Image src={profilePic} alt="Profile" className={styles.profilePic} />}
            id="basic-nav-dropdown"
          >

            {userRole === 'ADMIN' && (
              <>
                <NavDropdown.Item as={Link} to="/admin/profile">View Profile</NavDropdown.Item>
              </>
            )}

            {userRole === 'PLAYER' && (
              <>
                <NavDropdown.Item as={Link} to="/player/profile">View Profile</NavDropdown.Item>
              </>
            )}
     
            <NavDropdown.Divider />
            <NavDropdown.Item as={Link} to="/login">
            <Typography variant="navBar">Logout</Typography>
            </NavDropdown.Item>
          </NavDropdown>
        </Navbar.Collapse>
      </Container>
    </Navbar>
  );
}

export default NavBar;

