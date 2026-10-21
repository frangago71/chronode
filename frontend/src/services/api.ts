import axios from 'axios'

export const apiClient = axios.create({
  baseURL: '/api/v1',
  headers: {
    'Content-Type': 'application/json',
  },
})

// Interceptor para inyectar token JWT y workspace activo
apiClient.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('chronode_token')
    const activeWorkspaceId = localStorage.getItem('chronode_active_workspace')

    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }

    if (activeWorkspaceId) {
      config.headers['X-Workspace-Id'] = activeWorkspaceId
    }

    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

// Interceptor de respuesta para manejo de autenticación
apiClient.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      // Redirección o limpieza en sesión expirada
      localStorage.removeItem('chronode_token')
    }
    return Promise.reject(error)
  }
)

export default apiClient
