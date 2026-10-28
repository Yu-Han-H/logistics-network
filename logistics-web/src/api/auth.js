import request from './request'

export function login(data) {
  return request.post('/auth/login', data)
}

export function getMe() {
  return request.get('/auth/me')
}

export function logout() {
  return request.post('/auth/logout')
}
