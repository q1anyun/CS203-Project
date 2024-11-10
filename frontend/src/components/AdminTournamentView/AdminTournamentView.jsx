import React, { useState, useEffect, useRef } from 'react';
import axios from 'axios';
import styles from './AdminTournamentView.module.css';
import { useNavigate } from 'react-router-dom';
import DeleteConfirmationDialog from './DeleteConfirmationDialog';
import EditTournamentDialog from './EditTournamentDialog';
import CreateTournamentDialog from './CreateTournamentDialog';
import TournamentTable from './TournamentTable';

const tournamentURL = import.meta.env.VITE_TOURNAMENT_SERVICE_URL;
const gameTypeURL = import.meta.env.VITE_TOURNAMENT_GAMETYPE_URL;
const roundTypeURL = import.meta.env.VITE_TOURNAMENT_ROUNDTYPE_URL;

export default function AdminTournamentView() {
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
    const [tournamentId, setTournamentId] = useState('');
    const [selectedFile, setSelectedFile] = useState(null);
    const fileInputRef = useRef(null);
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
        format: '',
        locationAddress: '',
        locationLatitude: '',
        locationLongitude: ''
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
        format: '',
        locationAddress: '',
        locationLatitude: '',
        locationLongitude: ''
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
            tournamentType: '',
            description: '',
            format: '',
            locationAddress: '',
            locationLatitude: '',
            locationLongitude: ''
        });
    };

    const validateForm = (tournament) => {

        const isAnyFieldEmpty = Object.keys(tournament).some((key) => {
            if (key === 'locationLatitude' || key === 'locationLongitude' ||
                (tournament.format === 'ONLINE' && key === 'locationAddress'))
                return false;
            return !tournament[key];
        })

        if (isAnyFieldEmpty) {
            setCreateFormError('Please fill up all required fields');
            return false;
        }

        const { minElo, maxElo, tournamentType, maxPlayers } = tournament;
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
                const response = await axios.get(`${tournamentURL}`);
                console.log(response.data);
                setTournaments(response.data);
            } catch (error) {
                if (error.response) {
                    const statusCode = error.response.status;
                    const errorMessage = error.response.data?.message || 'An unexpected error occurred';
                    navigate(`/error?statusCode=${statusCode}&errorMessage=${encodeURIComponent(errorMessage)}`);
                } else if (err.request) {
                    navigate(`/error?statusCode=0&errorMessage=${encodeURIComponent('No response from server')}`);
                } else {
                    navigate(`/error?statusCode=500&errorMessage=${encodeURIComponent('Error: ' + err.message)}`);
                }
            }
        };

        fetchTournaments();
    }, []);

    const handleUploadClick = (id) => {
        setTournamentId(id);

    };

    const handleFileChange = async (event, tournamnetId) => {
        const file = event.target.files[0];
        if (file) {
            setSelectedFile(file);
        }

        if (selectedFile) {
            const formData = new FormData();
            formData.append("file", selectedFile);
            console.log(formData);
            console.log("its here");
            await axios.post(`${tournamentURL}/photo/${tournamentId}`, formData, null, {
                headers: {
                    'Authorization': `Bearer ${token}`,
                    'Content-Type': 'multipart/form-data',

                },
            });
            console.log('image updated successfully');
            window.location.reload();


        }
    }

    const handleDeleteClick = (tournamentId) => {
        setTournamentToDelete(tournamentId);
        setDeleteDialogOpen(true);
    };

    const handleEditClick = async (tournamentId) => {
        try {
            const response = await axios.get(`${tournamentURL}/${tournamentId}`);
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
                tournamentType: String(response.data.tournamentType.id) || '',
                format: response.data.format || '',
                locationAddress: response.data.locationAddress || '',
                locationLatitude: response.data.locationLatitude || '',
                locationLongitude: response.data.locationLongitude || ''
            });
            setEditDialogOpen(true);
        } catch (error) {
            if (error.response) {
                const statusCode = error.response.status;
                const errorMessage = error.response.data?.message || 'An unexpected error occurred';
                navigate(`/error?statusCode=${statusCode}&errorMessage=${encodeURIComponent(errorMessage)}`);
            } else if (err.request) {
                navigate(`/error?statusCode=0&errorMessage=${encodeURIComponent('No response from server')}`);
            } else {
                navigate(`/error?statusCode=500&errorMessage=${encodeURIComponent('Error: ' + err.message)}`);
            }
        }
    };

    const handleCreate = () => {
        setCreateDialogOpen(true);
    };

    return (
        <div className={styles.container}>

            <TournamentTable
                tournaments={tournaments}
                handleCreate={handleCreate}
                handleEditClick={handleEditClick}
                handleDeleteClick={handleDeleteClick}
                handleViewDetails={handleViewDetails}
                handleFileChange={handleFileChange}
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
                tournamentURL={tournamentURL}
                token={token}
            />

            <EditTournamentDialog
                tournamentURL={tournamentURL}
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
                tournamentURL={tournamentURL}
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