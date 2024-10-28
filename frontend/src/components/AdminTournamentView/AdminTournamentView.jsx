import React, { useState, useEffect } from 'react';
import { Typography, CircularProgress } from '@mui/material';
import axios from 'axios';
import styles from './AdminTournamentView.module.css';
import { useNavigate } from 'react-router-dom';
import DeleteConfirmationDialog from './DeleteConfirmationDialog';
import EditTournamentDialog from './EditTournamentDialog';
import CreateTournamentDialog from './CreateTournamentDialog';
import TournamentTable from './TournamentTable';

const baseURL = import.meta.env.VITE_TOURNAMENT_SERVICE_URL;
const gameTypeURL = import.meta.env.VITE_TOURNAMENT_GAMETYPE_URL;
const roundTypeURL = import.meta.env.VITE_TOURNAMENT_ROUNDTYPE_URL;

export default function AdminTournamentView() {
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [deleteDialogOpen, setDeleteDialogOpen] = useState(false);
    const [tournaments, setTournaments] = useState([]);
    const [tournamentToEdit, setTournamentToEdit] = useState([]);
    const [tournamentToDelete, setTournamentToDelete] = useState(null);
    const [createDialogOpen, setCreateDialogOpen] = useState(false);
    const [editDialogOpen, setEditDialogOpen] = useState(false);
    const [timeControlOptions, setTimeControlOptions] = useState([]);
    const [roundTypeOptions, setRoundTypeOptions] = useState([]);
    const [errors, setErrors] = useState({});
    const [createFormError, setCreateFormError] = useState('');
    const [eloError, setEloError] = useState('');
    const token = localStorage.getItem('token');




    const navigate = useNavigate();
    useEffect(() => {
        const fetchTimeControls = async () => {
            const response = await axios.get(`${gameTypeURL}`);
            setTimeControlOptions(response.data);
        };

        fetchTimeControls();
    }, []);
    useEffect(() => {
        const fetchRoundType = async () => {
            const response = await axios.get(`${roundTypeURL}/choices`);
            setRoundTypeOptions(response.data);
        };

        fetchRoundType();
    }, []);

    const [newTournament, setNewTournament] = useState({
        name: '',
        startDate: null,
        endDate: null,
        timeControl: '',
        minElo: '',
        maxElo: '',
        maxPlayers: '',
        tournamentType: '',
        description: '',
        format: ''
    });

    const [updateTournament, setUpdateTournament] = useState({
        name: '',
        startDate: '',
        endDate: '',
        timeControl: '',
        minElo: '',
        maxElo: '',
        maxPlayers: '',
        tournamentType: '',
        description: '',
        format: ''
    });

    const resetNewTournament = () => {
        setNewTournament({
            name: '',
            startDate: '',
            endDate: '',
            timeControl: '',
            minElo: '',
            maxElo: '',
            maxPlayers: '',
        });
    };

    const validateForm = (tournament) => {
        console.log(newTournament);
        const isAnyFieldEmpty = Object.keys(tournament).some((key) => {
            return !tournament[key];
        });

        if (isAnyFieldEmpty) {
            setCreateFormError('Please fill up all required fields');
            return false;
        }

        const { minElo, maxElo } = tournament;
        if (maxElo < minElo) {
            setEloError('Max ELO must be greater than Min ELO.');
            setCreateFormError('');
            return false;
        }

        setEloError('');
        setCreateFormError('');
        return true;
    };

    const handleViewDetails = (id, photoUrl) => {
        navigate(`/admin/tournaments/${id}`, { state: { photoUrl } });
    };

    useEffect(() => {
        const fetchTournaments = async () => {
            try {
                const response = await axios.get(`${baseURL}`);
                console.log(response.data);
                setTournaments(response.data);
                setLoading(false);

            } catch (error) {
                console.error('Error fetching tournaments:', error);
                setError(error);
                setLoading(false);


            }

        };

        fetchTournaments();
    }, []);

    const handleUploadClick = (tournamentId) => {
        // Logic to handle file upload interaction
        console.log("Upload button clicked for tournament ID:", tournamentId);
        // You can extend this to actually show a dialog or direct file input
    };





    const handleDeleteClick = (tournamentId) => {
        setTournamentToDelete(tournamentId);
        setDeleteDialogOpen(true);
    };

    const handleEditClick = async (tournamentId) => {
        try {
            const response = await axios.get(`${baseURL}/${tournamentId}`);
            console.log(response.data);
            setTournamentToEdit(response.data);
            const timeControlOption = timeControlOptions.find(option => option.name === response.data.timeControl.name) || '';
            setUpdateTournament({
                name: response.data.name || '',
                startDate: response.data.startDate || '',
                endDate: response.data.endDate || '',
                timeControl: timeControlOption.id || '',
                minElo: response.data.minElo || '',
                maxElo: response.data.maxElo || '',
                maxPlayers: response.data.maxPlayers || '',
                description: response.data.description || '',
                tournamentType: response.data.tournamentType.id || '',
                format: response.data.format || ''
            });
            setEditDialogOpen(true);
        } catch (error) {
            console.error('Error fetching tournament data:', error);
        }
    };

    const handleCreate = () => {
        setCreateDialogOpen(true);
    };

    if (loading) {
        return <CircularProgress />;
    }

    if (error) {
        return <Typography color="error">Error loading tournaments: {error.message}</Typography>;
    }

    return (
        <div className={styles.container}>

            <TournamentTable
                tournaments={tournaments}
                handleCreate={handleCreate}
                handleEditClick={handleEditClick}
                handleDeleteClick={handleDeleteClick}
                handleViewDetails={handleViewDetails}
                handleUploadClick={handleUploadClick}
            />

            <CreateTournamentDialog
                createDialogOpen={createDialogOpen}
                setCreateDialogOpen={setCreateDialogOpen}
                newTournament={newTournament}
                setNewTournament={setNewTournament}
                resetNewTournament={resetNewTournament}
                timeControlOptions={timeControlOptions}
                roundTypeOptions={roundTypeOptions}
                validateForm={validateForm}
                errors={errors}
                eloError={eloError}
                createFormError={createFormError}
                setCreateFormError={setCreateFormError}
                setTournaments={setTournaments}
                baseURL={baseURL}
                token={token}
            />

            <EditTournamentDialog
                baseURL={baseURL}
                token={token}
                updateTournament={updateTournament}
                timeControlOptions={timeControlOptions}
                roundTypeOptions={roundTypeOptions}
                errors={errors}
                setErrors={setErrors}
                eloError={eloError}
                createFormError={createFormError}
                setCreateFormError={setCreateFormError}
                validateForm={validateForm}
                editDialogOpen={editDialogOpen}
                setUpdateTournament={setUpdateTournament}
                setEditDialogOpen={setEditDialogOpen}
                tournamentToEdit={tournamentToEdit}
                tournaments={tournaments}
                setTournaments={setTournaments}
            />


            <DeleteConfirmationDialog
                open={deleteDialogOpen}
                baseURL={baseURL}
                token={token}
                tournamentToDelete={tournamentToDelete}
                setTournaments={setTournaments}
                setDeleteDialogOpen={setDeleteDialogOpen}
                setTournamentToDelete={setTournamentToDelete}
                tournaments={tournaments}

            />
        </div>
    );
}