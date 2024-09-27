import { React, useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import Container from 'react-bootstrap/Container';
import Nav from 'react-bootstrap/Nav';
import Navbar from 'react-bootstrap/Navbar';
import NavDropdown from 'react-bootstrap/NavDropdown';
import styles from './NavBar.module.css';
import logoImage from '../../assets/chess_logo.png';
import Image from 'react-bootstrap/Image'

function NavBar({ profilePic }) {
  const [userRole, setUserRole] = useState(null);

  useEffect(() => {
    const role = localStorage.getItem('role');
    setUserRole(role);
  }, []);

  return (

    <Navbar expand="lg" className={styles.navbar}>
      <Container>
        <Navbar.Brand as={Link} to="/home" className={styles.navbarBrand}>
          <span style={{ fontWeight: 'bold' }}>CHESS</span>
          <img
            src={logoImage}
            alt="logo"
            style={{ margin: '0 10px', height: '40px' }}
          />
          <span style={{ fontWeight: 'normal' }}>MVP</span>
        </Navbar.Brand>
        <Navbar.Toggle />

        <Nav className="me-auto">
          <Nav.Link as={Link} to="/home">Home</Nav.Link>

          {/*THIS COMMENTED CODE IS THE CORRECT CODE TO BE IMPLEMENTED*/}
          {/* {userRole === 'ADMIN' && (
            <>
              <Nav.Link as={Link} to="/admin/tournaments">Tournaments</Nav.Link>
            </>
          )}

          {userRole === 'PLAYER' && (
            <>
              <Nav.Link as={Link} to="/player/tournaments">Tournaments</Nav.Link>
            </>
          )} */}

          <Nav.Link as={Link} to="/player/tournaments">Tournaments</Nav.Link>
          <Nav.Link as={Link} to="/leaderboard">Leaderboard</Nav.Link>
        </Nav>

        <Navbar.Collapse className="justify-content-end">
          <NavDropdown
            align="end"
            title={<Image src={profilePic} alt="Profile" className={styles.profilePic} />}
            id="basic-nav-dropdown"
          >
            {/*THIS COMMENTED CODE IS THE CORRECT CODE TO BE IMPLEMENTED*/}
            {/* {userRole === 'ADMIN' && (
              <>
                <NavDropdown.Item as={Link} to="/admin/profile">View Profile</NavDropdown.Item>
              </>
            )}

            {userRole === 'PLAYER' && (
              <>
                <NavDropdown.Item as={Link} to="/player/profile">View Profile</NavDropdown.Item>
              </>
            )} */}

            <NavDropdown.Item as={Link} to="/player/profile">View Profile</NavDropdown.Item>
            <NavDropdown.Item as={Link} to="/admin/profile">View Admin Profile (tmp)</NavDropdown.Item>
            <NavDropdown.Item as={Link} to="/admin/tournaments">View Admin Tournament view (tmp)</NavDropdown.Item>
            <NavDropdown.Item as={Link} to="/admin/matches">View Admin Matches view (tmp)</NavDropdown.Item>
            <NavDropdown.Divider />
            <NavDropdown.Item as={Link} to="/login">Logout</NavDropdown.Item>
          </NavDropdown>
        </Navbar.Collapse>
      </Container>
    </Navbar>
  );
}

export default NavBar;

