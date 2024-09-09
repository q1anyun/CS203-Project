import React from 'react';
import { Link } from 'react-router-dom';
import Container from 'react-bootstrap/Container';
import Nav from 'react-bootstrap/Nav';
import Navbar from 'react-bootstrap/Navbar';
import NavDropdown from 'react-bootstrap/NavDropdown';
import styles from './NavBar.module.css';

function NavBar() {
  return (
    <Navbar expand="lg" className={styles.navbar}>
      <Container>
        <Navbar.Brand as={Link} to="/home" className={styles.navbarBrand}>Chess MVP</Navbar.Brand>
        <Navbar.Toggle aria-controls="basic-navbar-nav" />
        <Navbar.Collapse id="basic-navbar-nav">
          <Nav className="me-auto">
          <Nav.Link as = {Link} to = "/home">Home</Nav.Link>
            <Nav.Link as = {Link} to = "/player/tournaments">Tournaments</Nav.Link>
            <Nav.Link as = {Link} to = "/leaderboard">Leaderboard</Nav.Link>
            <NavDropdown title="Settings" id="basic-nav-dropdown">
              <NavDropdown.Item as = {Link} to = "/player/profile">View Profile</NavDropdown.Item>
              <NavDropdown.Item as = {Link} to = "/admin/profile">View Admin Profile (tmp)</NavDropdown.Item>
              <NavDropdown.Item as = {Link} to = "/admin/tournaments">View Admin Tournament view (tmp)</NavDropdown.Item>
              <NavDropdown.Divider />
              <NavDropdown.Item as = {Link} to = "/login">
                Logout
              </NavDropdown.Item>
            </NavDropdown>
          </Nav>
        </Navbar.Collapse>
      </Container>
    </Navbar>
  );
}

export default NavBar;
