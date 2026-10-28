import request from './request'

export function getGraph() {
  return request.get('/graph')
}

export function getAdjacency() {
  return request.get('/graph/adjacency')
}
