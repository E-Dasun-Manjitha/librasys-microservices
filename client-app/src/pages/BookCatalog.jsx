import { useState, useEffect } from 'react'
import { api, getAuth } from '../api.js'

function BookCatalog() {
  const [books, setBooks] = useState([])
  const [search, setSearch] = useState('')
  const [searchBy, setSearchBy] = useState('title')
  const [loading, setLoading] = useState(true)
  const [message, setMessage] = useState('')

  const fetchBooks = async (params = {}) => {
    setLoading(true)
    try {
      const data = await api.getBooks(params)
      setBooks(data || [])
    } catch (err) {
      console.error('Failed to fetch books:', err)
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    fetchBooks()
  }, [])

  const handleSearch = (e) => {
    e.preventDefault()
    if (!search.trim()) {
      fetchBooks()
      return
    }
    const params = {}
    params[searchBy] = search.trim()
    fetchBooks(params)
  }

  const handleBorrow = async (bookId) => {
    const { user } = getAuth()
    setMessage('')
    try {
      await api.createLoan({ memberId: user.id, bookId })
      setMessage('Book borrowed successfully!')
      fetchBooks()
    } catch (err) {
      setMessage('Failed to borrow: ' + (err.message || 'No copies available'))
    }
  }

  const handleReserve = async (bookId) => {
    const { user } = getAuth()
    setMessage('')
    try {
      await api.createReservation({ memberId: user.id, bookId })
      setMessage('Reservation created successfully!')
    } catch (err) {
      setMessage('Failed to reserve: ' + err.message)
    }
  }

  return (
    <div className="page-container">
      <div className="page-header">
        <h1>📖 Book Catalog</h1>
        <p>Browse and borrow from our collection</p>
      </div>

      {message && (
        <div className={`alert ${message.includes('Failed') ? 'alert-error' : 'alert-success'}`}>
          {message}
          <button onClick={() => setMessage('')} className="alert-close">×</button>
        </div>
      )}

      <form className="search-bar" onSubmit={handleSearch}>
        <select value={searchBy} onChange={(e) => setSearchBy(e.target.value)}>
          <option value="title">Title</option>
          <option value="author">Author</option>
          <option value="category">Category</option>
        </select>
        <input
          type="text"
          value={search}
          onChange={(e) => setSearch(e.target.value)}
          placeholder={`Search by ${searchBy}...`}
        />
        <button type="submit" className="btn btn-primary">Search</button>
        <button type="button" className="btn btn-secondary" onClick={() => { setSearch(''); fetchBooks(); }}>
          Clear
        </button>
      </form>

      {loading ? (
        <div className="loading">Loading books...</div>
      ) : (
        <div className="book-grid">
          {books.map((book) => (
            <div key={book.id} className="book-card">
              <div className="book-category">{book.category}</div>
              <h3 className="book-title">{book.title}</h3>
              <p className="book-author">by {book.author}</p>
              <p className="book-isbn">ISBN: {book.isbn}</p>
              <div className="book-availability">
                <span className={book.copiesAvailable > 0 ? 'available' : 'unavailable'}>
                  {book.copiesAvailable} / {book.totalCopies} available
                </span>
              </div>
              <div className="book-actions">
                {book.copiesAvailable > 0 ? (
                  <button onClick={() => handleBorrow(book.id)} className="btn btn-primary">
                    Borrow
                  </button>
                ) : (
                  <button onClick={() => handleReserve(book.id)} className="btn btn-warning">
                    Reserve
                  </button>
                )}
              </div>
            </div>
          ))}
          {books.length === 0 && <p className="empty-state">No books found.</p>}
        </div>
      )}
    </div>
  )
}

export default BookCatalog
