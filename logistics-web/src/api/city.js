import request from './request'

export function listCities() {
  return request.get('/cities')
}

export function listSortedCities(referenceId) {
  return request.get('/cities/sorted', { params: { reference: referenceId } })
}

export function getCity(id) {
  return request.get(`/cities/${id}`)
}

export function addCity(data) {
  return request.post('/cities', data)
}

export function updateCity(id, data) {
  return request.put(`/cities/${id}`, data)
}

export function deleteCity(id) {
  return request.delete(`/cities/${id}`)
}
