import { useState, useEffect } from 'react'
import { api, getAuth } from '../api.js'

function Notifications() {
  const [notifications, setNotifications] = useState([])
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    const fetchNotifications = async () => {
      const { user } = getAuth()
      try {
        const data = await api.getNotifications(user.id)
        setNotifications(data || [])
      } catch (err) {
        console.error('Failed to fetch notifications:', err)
      } finally {
        setLoading(false)
      }
    }
    fetchNotifications()
  }, [])

  const getIcon = (type) => {
    switch (type) {
      case 'EMAIL': return '📧'
      case 'DUE_REMINDER': return '⏰'
      case 'RESERVATION_AVAILABLE': return '✅'
      default: return '🔔'
    }
  }

  return (
    <div className="page-container">
      <div className="page-header">
        <h1>🔔 Notifications</h1>
        <p>Your notification history</p>
      </div>

      {loading ? (
        <div className="loading">Loading notifications...</div>
      ) : notifications.length === 0 ? (
        <p className="empty-state">No notifications yet.</p>
      ) : (
        <div className="notification-list">
          {notifications.map((notif) => (
            <div key={notif.id} className="notification-card">
              <div className="notification-icon">{getIcon(notif.type)}</div>
              <div className="notification-content">
                <div className="notification-type">{notif.type.replace(/_/g, ' ')}</div>
                <p className="notification-message">{notif.message}</p>
                <span className="notification-time">
                  {new Date(notif.sentAt).toLocaleString()}
                </span>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  )
}

export default Notifications
