import request from './request'

export function getShortestPath(from, to, mode = 'graph') {
  return request.get('/paths/shortest', { params: { from, to, mode } })
}

export function getPathMatrix() {
  return request.get('/paths/matrix')
}
