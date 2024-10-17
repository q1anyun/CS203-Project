import * as React from 'react';
import { useState, useEffect } from 'react';
import { styled } from '@mui/material/styles';
import Table from '@mui/material/Table';
import TableBody from '@mui/material/TableBody';
import TableCell, { tableCellClasses } from '@mui/material/TableCell';
import TableContainer from '@mui/material/TableContainer';
import TableHead from '@mui/material/TableHead';
import TableRow from '@mui/material/TableRow';
import Paper from '@mui/material/Paper';
import VisibilityIcon from '@mui/icons-material/Visibility';
import { Button, Chip, Dialog, DialogActions, DialogContent, DialogContentText, DialogTitle, Checkbox, FormControlLabel, Typography, Box, Grid } from '@mui/material';
import styles from './PlayerTournamentView.module.css';
import axios from 'axios';
import { useNavigate } from 'react-router-dom';

const baseURL = import.meta.env.VITE_TOURNAMENT_SERVICE_URL;
const baseURL2 = import.meta.env.VITE_TOURNAMENT_PLAYER_URL;

import RegisterDialog from './RegisterDialog';
import WithdrawDialog from './WithdrawDialog';
import TournamentTable from './TournamentTable';

// const statusColorMap = {
//     LIVE: 'success',
//     UPCOMING: 'warning',
//     COMPLETED: 'default',
// };

// const StyledTableCell = styled(TableCell)(({ theme }) => ({
//     [`&.${tableCellClasses.head}`]: {
//         backgroundColor: theme.palette.common.black,
//         color: theme.palette.common.white,
//         textAlign: 'center',
//         variant: 'header1'
//     },
//     [`&.${tableCellClasses.body}`]: {
//         fontSize: 14,
//         textAlign: 'center',
//     },
// }));

// const StyledTableRow = styled(TableRow)(({ theme }) => ({
//     '&:nth-of-type(odd)': {
//         backgroundColor: theme.palette.action.hover,
//     },
//     '&:last-child td, &:last-child th': {
//         border: 0,
//     },
// }));

function PlayerTournamentView() {
    const [tournaments, setTournaments] = useState([]);
    const [joinedTournaments, setJoinedTournaments] = useState([]);
    const [openRegisterDialog, setOpenRegisterDialog] = useState(false);
    const [openWithdrawDialog, setOpenWithdrawDialog] = useState(false);
    const [selectedTournament, setSelectedTournament] = useState({});
    const [agreedToTerms, setAgreedToTerms] = useState(false);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const token = localStorage.getItem('token');
    const navigate = useNavigate();

    // const [currentPage, setCurrentPage] = useState(1);
    // const itemsPerPage = 7;  
    // const totalPages = Math.ceil(tournaments.length / itemsPerPage);
    // const handleNextPage = () => {
    //     if (currentPage < totalPages) {
    //         setCurrentPage(prevPage => prevPage + 1);
    //     }
    // };

    // const handlePrevPage = () => {
    //     if (currentPage > 1) {
    //         setCurrentPage(prevPage => prevPage - 1);
    //     }
    // };

    useEffect(() => {
        const fetchTournaments = async () => {
            try {
                // Fetch all tournaments
                const tournamentResponse = await axios.get(baseURL, {
                    headers: { 'Authorization': `Bearer ${token}` },
                });
                setTournaments(tournamentResponse.data);

                // Fetch tournaments that the user has registered for
                const registeredResponse = await axios.get(`${baseURL}/registered/current`, {
                    headers: { 'Authorization': `Bearer ${token}` },
                });
                setJoinedTournaments(registeredResponse.data);
            } catch (err) {
                setError(err.message);
            } finally {
                setLoading(false);
            }
        };

        fetchTournaments();
    }, []);

    const isJoined = (tournamentId) => joinedTournaments.some(tournament => tournament.id === tournamentId);

    const handleJoin = (tournament) => {
        setSelectedTournament(tournament);
        setOpenRegisterDialog(true);
    };

    const handleWithdraw = (tournament) => {
        setSelectedTournament(tournament);
        setOpenWithdrawDialog(true);
    }

    const handleRegister = async () => {
        try {
            const response = await axios.post(`${baseURL2}/register/current/${selectedTournament.id}`, null, {
                headers: { 'Authorization': `Bearer ${token}` },
            });

            if (response.status !== 200) {
                throw new Error('Failed to enroll in the tournament');
            }

            setJoinedTournaments(prev => [...prev, selectedTournament]);
            setOpenRegisterDialog(false);
        } catch (err) {
            console.error(err);
        }
    };

    {/*WAITING FOR API TO WITHDRAW */ }
    const handleWithdrawConfirmation = async () => {
        // try {
        //     const response = await axios.post(`${baseURL2}/register/current/${selectedTournament.id}`, null, {
        //         headers: { 'Authorization': `Bearer ${token}` },
        //     });

        //     if (response.status !== 200) {
        //         throw new Error('Failed to enroll in the tournament');
        //     }

        //     setJoinedTournaments(prev => [...prev, selectedTournament]);
        //     setOpenWithdrawDialog(false);
        // } catch (err) {
        //     console.error(err);
        // }

        setOpenWithdrawDialog(false);
    };

    const handleViewDetails = (tournamentId) => {
        navigate(`${tournamentId}`);
    };

    if (loading) return <Typography>Loading tournaments...</Typography>;
    if (error) return <Typography>Error: {error}</Typography>;


    return (
        <div>
            <div className={styles.container}>
                
            </div>
            <RegisterDialog
                handleRegister={handleRegister}
                agreedToTerms={agreedToTerms}
                setAgreedToTerms={setAgreedToTerms}
                openRegisterDialog={openRegisterDialog}
                setOpenRegisterDialog={setOpenRegisterDialog}
            /> 
            <WithdrawDialog
                openWithdrawDialog={openWithdrawDialog}
                setOpenWithdrawDialog={setOpenWithdrawDialog}
                handleWithdrawConfirmation={handleWithdrawConfirmation}
            /> 
        </div>
    );
}

export default PlayerTournamentView;

