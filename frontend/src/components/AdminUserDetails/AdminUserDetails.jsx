import React, { useEffect, useState } from 'react';
import { styled } from '@mui/material/styles';
import { Table, TableBody, TableCell, TableContainer, TableHead, tableCellClasses, TableRow, Paper, Typography, TextField, Box, Button } from '@mui/material';
import axios from 'axios';
import { useParams, useNavigate } from 'react-router-dom';
import useHandleError from '../Hooks/useHandleError';

const baseURL = import.meta.env.VITE_USER_SERVICE_URL;

const StyledTableCell = styled(TableCell)(({ theme }) => ({
    [`&.${tableCellClasses.head}`]: {
        backgroundColor: theme.palette.common.black,
        color: theme.palette.common.white,
    },
    [`&.${tableCellClasses.body}`]: {
        fontSize: 14,
    },
}));

const StyledTableRow = styled(TableRow)(({ theme }) => ({
    '&:nth-of-type(odd)': {
        backgroundColor: theme.palette.action.hover,
    },
    '&:last-child td, &:last-child th': {
        border: 0,
    },
}));

function createData(number, username, email, role) {
    return { number, username, email, role };
}

function AdminUserDetails() {
    const handleError = useHandleError();
    const { id } = useParams();
    const [participants, setParticipants] = useState([]);
    const [searchTerm, setSearchTerm] = useState('');
    const [page, setPage] = useState(0);
    const [rowsPerPage, setRowsPerPage] = useState(10);

    useEffect(() => {
        const fetchParticipants = async () => {
            try {
                const response = await axios.get(`${baseURL}`);
                const data = response.data;
                const formattedData = data.map((participant, index) =>
                    createData(index + 1, participant.username, participant.email, participant.role)
                );
                setParticipants(formattedData);
            } catch (error) {
                handleError(error);
            }
        };

        fetchParticipants();
    }, [id]);

    // Handle search
    const handleSearchChange = (event) => {
        setSearchTerm(event.target.value);
        setPage(0); // Reset to first page on search
    };

    // Handle page change
    const handleChangePage = (newPage) => {
        setPage(newPage);
    };

    // Filter participants based on search term
    const filteredParticipants = participants.filter((participant) =>
        participant.username.toLowerCase().includes(searchTerm.toLowerCase())
    );

    // Calculate total pages
    const totalPages = Math.ceil(filteredParticipants.length / rowsPerPage);

    // Paginate participants
    const paginatedParticipants = filteredParticipants.slice(page * rowsPerPage, page * rowsPerPage + rowsPerPage);

    return (
        <div style={{ padding: '20px' }}>
            <Typography variant="header1" component="h2">
                Users
            </Typography>
            <TextField
                variant="outlined"
                placeholder="Search by Username"
                size='small'
                value={searchTerm}
                onChange={handleSearchChange}
                style={{ marginBottom: '20px', width: '95vw' }}
            />
            <TableContainer component={Paper} sx={{ padding: '20px' }}>
                <Table sx={{ minWidth: 700 }} aria-label="customized table">
                    <TableHead>
                        <TableRow>
                            <StyledTableCell>Number</StyledTableCell>
                            <StyledTableCell>Username</StyledTableCell>
                            <StyledTableCell>Email</StyledTableCell>
                            <StyledTableCell>Role</StyledTableCell>
                        </TableRow>
                    </TableHead>
                    <TableBody>
                        {paginatedParticipants.map((row) => (
                            <StyledTableRow key={row.number} hover>
                                <StyledTableCell>{row.number}</StyledTableCell>
                                <StyledTableCell>{row.username}</StyledTableCell>
                                <StyledTableCell>{row.email}</StyledTableCell>
                                <StyledTableCell>{row.role}</StyledTableCell>
                            </StyledTableRow>
                        ))}
                    </TableBody>
                </Table>
            </TableContainer>
            <Box sx={{ display: 'flex', justifyContent: 'center', alignItems: 'center', mt: 2 }}>
                <Button
                    onClick={() => handleChangePage(page - 1)}
                    disabled={page === 0}
                    variant="contained"
                    sx={{ mr: 2 }}
                >
                    Previous
                </Button>
                <Typography variant="body1">
                    Page {page + 1} of {totalPages}
                </Typography>
                <Button
                    onClick={() => handleChangePage(page + 1)}
                    disabled={page >= totalPages - 1}
                    variant="contained"
                    sx={{ ml: 2 }}
                >
                    Next
                </Button>
            </Box>
        </div>
    );
}

export default AdminUserDetails;
