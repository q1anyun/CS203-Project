import React from 'react';
import { Navigate, useLocation } from 'react-router-dom';

const ProtectedRoute = ({ children }) => {
    const userRole = localStorage.getItem('role');
    const location = useLocation();

    const isAccessingAdminRoute = location.pathname.startsWith('/admin');
    const isAccessingPlayerRoute = location.pathname.startsWith('/player');

    if (!userRole) {
        return <Navigate to="/login" />;
    }

    if (isAccessingAdminRoute && userRole !== 'ADMIN') {
        return <Navigate to="/error" />;
    }

    if (isAccessingPlayerRoute && userRole !== 'PLAYER') {
        return <Navigate to="/error" />;
    }

    return children;
};

export default ProtectedRoute;
