import { useState, useEffect } from 'react'
import { api, getAuth } from '../api.js'

function MyLoans() {
  const [loans, setLoans] = useState([])
  const [booksMap, setBooksMap] = useState({})
  const [loading, setLoading] = useState(true)
  const [message, setMessage] = useState('')

  const fetchLoansAndBooks = async () => {
    const { user } = getAuth()
    setLoading(true)
    try {
      const [loansData, booksData] = await Promise.all([
        api.getMyLoans(user.id),
        api.getBooks()
      ])
      
      setLoans(loansData || [])

      // Create a lookup map: bookId -> { title, author }
      const map = {}
      if (Array.isArray(booksData)) {
        booksData.forEach((b) => {
          map[b.id] = b
        })
      }
      setBooksMap(map)
    } catch (err) {
      console.error('Failed to fetch loans or books:', err)
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    fetchLoansAndBooks()
  }, [])

  const handleReturn = async (loanId) => {
    setMessage('')
    try {
      await api.returnLoan(loanId)
      setMessage('Book returned successfully!')
      fetchLoansAndBooks()
    } catch (err) {
      setMessage('Failed to return book: ' + err.message)
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

  const activeLoans = loans.filter((l) => l.status === 'ACTIVE')
  const pastLoans = loans.filter((l) => l.status !== 'ACTIVE')

  return (
    <div className="page-container">
      <div className="page-header">
        <h1>📋 My Loans</h1>
        <p>Manage your borrowed books</p>
      </div>

      {message && (
        <div className={`alert ${message.includes('Failed') ? 'alert-error' : 'alert-success'}`}>
          {message}
          <button onClick={() => setMessage('')} className="alert-close">×</button>
        </div>
      )}

      <h2 className="section-title">Active Loans ({activeLoans.length})</h2>
      {loading ? (
        <div className="loading">Loading loans...</div>
      ) : activeLoans.length === 0 ? (
        <p className="empty-state">No active loans.</p>
      ) : (
        <div className="table-container">
          <table>
            <thead>
              <tr>
                <th>Loan ID</th>
                <th>Book Details</th>
                <th>Loan Date</th>
                <th>Due Date</th>
                <th>Status</th>
                <th>Action</th>
              </tr>
            </thead>
            <tbody>
              {activeLoans.map((loan) => (
                <tr key={loan.id}>
                  <td>#{loan.id}</td>
                  <td>{getBookDisplay(loan.bookId)}</td>
                  <td>{loan.loanDate}</td>
                  <td>{loan.dueDate}</td>
                  <td><span className="badge badge-active">{loan.status}</span></td>
                  <td>
                    <button onClick={() => handleReturn(loan.id)} className="btn btn-sm btn-primary">
                      Return
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      {pastLoans.length > 0 && (
        <>
          <h2 className="section-title">Past Loans ({pastLoans.length})</h2>
          <div className="table-container">
            <table>
              <thead>
                <tr>
                  <th>Loan ID</th>
                  <th>Book Details</th>
                  <th>Loan Date</th>
                  <th>Return Date</th>
                  <th>Status</th>
                </tr>
              </thead>
              <tbody>
                {pastLoans.map((loan) => (
                  <tr key={loan.id}>
                    <td>#{loan.id}</td>
                    <td>{getBookDisplay(loan.bookId)}</td>
                    <td>{loan.loanDate}</td>
                    <td>{loan.returnDate || '—'}</td>
                    <td><span className="badge badge-returned">{loan.status}</span></td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </>
      )}
    </div>
  )
}

export default MyLoans
