import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom'
import AppLayout from '../components/AppLayout'
import Scheduler from '../pages/scheduler/Scheduler.jsx'
import UsersContainer from '../pages/users/UsersContainer.jsx'
import TasksContainer from '../pages/tasks/TasksContainer.jsx'
import LoginPage from '../pages/Login.jsx' // новая страница логина

function PrivateRoute({ children }) {
    const token = localStorage.getItem('token')
    if (!token) {
        return <Navigate to="/login" replace />
    }
    return children
}

export default function AppRouter() {
    return (
        <BrowserRouter>
            <Routes>
                {/* публичный логин */}
                <Route path="/login" element={<LoginPage />} />

                {/* приватные маршруты */}
                <Route
                    path="/"
                    element={
                        <PrivateRoute>
                            <AppLayout />
                        </PrivateRoute>
                    }
                >
                    <Route index element={<Scheduler />} />
                    <Route path="users" element={<UsersContainer />} />
                    <Route path="tasks" element={<TasksContainer />} />
                </Route>
            </Routes>
        </BrowserRouter>
    )
}
