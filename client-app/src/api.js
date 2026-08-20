const API_BASE = '/api';

let authToken = null;
let currentUser = null;

export const setAuth = (token, user) => {
  authToken = token;
  currentUser = user;
};

export const getAuth = () => ({
  token: authToken,
  user: currentUser,
});

export const clearAuth = () => {
  authToken = null;
  currentUser = null;
};

export const isLoggedIn = () => authToken !== null;

const request = async (path, options = {}) => {
  const headers = {
    'Content-Type': 'application/json',
    ...options.headers,
  };

  if (authToken) {
    headers['Authorization'] = `Bearer ${authToken}`;
  }

  const response = await fetch(`${API_BASE}${path}`, {
    ...options,
    headers,
  });

  if (!response.ok) {
    const errorText = await response.text();
    throw new Error(errorText || `HTTP ${response.status}`);
  }

  if (response.status === 204) return null;

  const text = await response.text();
  return text ? JSON.parse(text) : null;
};

export const api = {
  // Auth
  register: (data) => request('/auth/register', { method: 'POST', body: JSON.stringify(data) }),
  login: (data) => request('/auth/login', { method: 'POST', body: JSON.stringify(data) }),

  // Books
  getBooks: (params) => {
    const query = new URLSearchParams();
    if (params?.title) query.set('title', params.title);
    if (params?.author) query.set('author', params.author);
    if (params?.category) query.set('category', params.category);
    const qs = query.toString();
    return request(`/books${qs ? '?' + qs : ''}`);
  },

  // Loans
  createLoan: (data) => request('/loans', { method: 'POST', body: JSON.stringify(data) }),
  returnLoan: (id) => request(`/loans/${id}/return`, { method: 'PUT' }),
  getMyLoans: (memberId) => request(`/loans/member/${memberId}`),

  // Reservations
  createReservation: (data) => request('/reservations', { method: 'POST', body: JSON.stringify(data) }),
  cancelReservation: (id) => request(`/reservations/${id}`, { method: 'DELETE' }),
  getMyReservations: (memberId) => request(`/reservations/member/${memberId}`),

  // Notifications
  getNotifications: (memberId) => request(`/notify/history/${memberId}`),
};
