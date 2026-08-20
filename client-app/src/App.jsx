import { useState } from 'react'
import { Routes, Route, Link, useNavigate, Navigate } from 'react-router-dom'
import { isLoggedIn, getAuth, clearAuth } from './api.js'
import Login from './pages/Login.jsx'
import Register from './pages/Register.jsx'
import BookCatalog from './pages/BookCatalog.jsx'
import MyLoans from './pages/MyLoans.jsx'
import MyReservations from './pages/MyReservations.jsx'
import Notifications from './pages/Notifications.jsx'

function App() {
  const [loggedIn, setLoggedIn] = useState(isLoggedIn())
  const navigate = useNavigate()

  const handleLoginSuccess = () => {
    setLoggedIn(true)
    navigate('/books')
  }

  const handleLogout = () => {
    clearAuth()
    setLoggedIn(false)
    navigate('/login')
  }

  return (
    <div className="app">
      <nav className="navbar">
        <div className="nav-brand">
          <Link to="/">📚 LibraSys</Link>
        </div>
        <div className="nav-links">
          {loggedIn ? (
            <>
              <Link to="/books">Catalog</Link>
              <Link to="/loans">My Loans</Link>
              <Link to="/reservations">Reservations</Link>
              <Link to="/notifications">Notifications</Link>
              <span className="nav-user">
                {getAuth().user?.name || 'Member'}
              </span>
              <button onClick={handleLogout} className="btn-logout">Logout</button>
            </>
          ) : (
            <>
              <Link to="/login">Login</Link>
              <Link to="/register">Register</Link>
            </>
          )}
        </div>
      </nav>

      <main className="main-content">
        <Routes>
          <Route path="/login" element={<Login onSuccess={handleLoginSuccess} />} />
          <Route path="/register" element={<Register />} />
          <Route path="/books" element={loggedIn ? <BookCatalog /> : <Navigate to="/login" />} />
          <Route path="/loans" element={loggedIn ? <MyLoans /> : <Navigate to="/login" />} />
          <Route path="/reservations" element={loggedIn ? <MyReservations /> : <Navigate to="/login" />} />
          <Route path="/notifications" element={loggedIn ? <Notifications /> : <Navigate to="/login" />} />
          <Route path="/" element={<Navigate to={loggedIn ? "/books" : "/login"} />} />
        </Routes>
      </main>
    </div>
  )
}

export default App
