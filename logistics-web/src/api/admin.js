import request from './request'

export function resetData() {
  return request.post('/admin/reset')
}
