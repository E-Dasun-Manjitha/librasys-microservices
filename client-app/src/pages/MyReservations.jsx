import { useState, useEffect } from 'react'
import { api, getAuth } from '../api.js'

const formatDate = (dateStr) => {
  if (!dateStr) return '—'
  try {
    return new Date(dateStr).toLocaleString('en-US', {
      year: 'numeric', month: 'short', day: 'numeric',
      hour: '2-digit', minute: '2-digit'
    })
  } catch { return dateStr }
}

function MyReservations() {
  const [reservations, setReservations] = useState([])
  const [booksMap, setBooksMap] = useState({})
  const [loading, setLoading] = useState(true)
  const [message, setMessage] = useState('')

  const fetchReservationsAndBooks = async () => {
    const { user } = getAuth()
    setLoading(true)
    try {
      const [reservationsData, booksData] = await Promise.all([
        api.getMyReservations(user.id),
        api.getBooks()
      ])

      setReservations(reservationsData || [])

      // Create a lookup map: bookId -> { title, author }
      const map = {}
      if (Array.isArray(booksData)) {
        booksData.forEach((b) => {
          map[b.id] = b
        })
      }
      setBooksMap(map)
    } catch (err) {
      console.error('Failed to fetch reservations or books:', err)
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    fetchReservationsAndBooks()
  }, [])

  const handleCancel = async (id) => {
    setMessage('')
    try {
      await api.cancelReservation(id)
      setMessage('Reservation cancelled.')
      fetchReservationsAndBooks()
    } catch (err) {
      setMessage('Failed to cancel: ' + err.message)
    }
  }

  const getBookDisplay = (bookId) => {
    const book = booksMap[bookId]
    if (book) {
      return (
        <div>
          <span style={{ fontWeight: 600, color: '#f3f4f6' }}>{book.title}</span>
          <span style={{ display: 'block', fontSize: '0.8rem', color: '#9ca3af' }}>
            by {book.author} (ID: #{bookId})
          </span>
        </div>
      )
    }
    return <span>Book #{bookId}</span>
  }

  return (
    <div className="page-container">
      <div className="page-header">
        <h1>🔖 My Reservations</h1>
        <p>Track your book reservations</p>
      </div>

      {message && (
        <div className={`alert ${message.includes('Failed') ? 'alert-error' : 'alert-success'}`}>
          {message}
          <button onClick={() => setMessage('')} className="alert-close">×</button>
        </div>
      )}

      {loading ? (
        <div className="loading">Loading reservations...</div>
      ) : reservations.length === 0 ? (
        <p className="empty-state">No reservations yet.</p>
      ) : (
        <div className="table-container">
          <table>
            <thead>
              <tr>
                <th>Reservation ID</th>
                <th>Book Details</th>
                <th>Date</th>
                <th>Status</th>
                <th>Action</th>
              </tr>
            </thead>
            <tbody>
              {reservations.map((res) => (
                <tr key={res.id}>
                  <td>#{res.id}</td>
                  <td>{getBookDisplay(res.bookId)}</td>
                  <td>{formatDate(res.reservationDate)}</td>
                  <td>
                    <span className={`badge badge-${res.status.toLowerCase()}`}>
                      {res.status}
                    </span>
                  </td>
                  <td>
                    {res.status === 'PENDING' && (
                      <button onClick={() => handleCancel(res.id)} className="btn btn-sm btn-danger">
                        Cancel
                      </button>
                    )}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  )
}

export default MyReservations
