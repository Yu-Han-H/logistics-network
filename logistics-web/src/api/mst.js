import request from './request'

export function getMst(algorithm = 'prim') {
  return request.get('/mst', { params: { algorithm } })
}
