export function errorMessage(error, fallback = '请求失败，请稍后重试') {
  return error?.response?.data?.message || error?.message || fallback
}

export function formatNanos(nanos) {
  if (nanos === null || nanos === undefined) return '--'
  if (nanos < 1_000_000) return `${(nanos / 1000).toFixed(1)} μs`
  return `${(nanos / 1_000_000).toFixed(3)} ms`
}

export function formatKm(value) {
  if (value === null || value === undefined || Number.isNaN(Number(value))) return '--'
  return `${Number(value).toFixed(2)} km`
}

export function downloadCsv(filename, rows) {
  const escape = (value) => {
    const text = value === null || value === undefined ? '' : String(value)
    return /[",\n]/.test(text) ? `"${text.replaceAll('"', '""')}"` : text
  }
  const content = '\uFEFF' + rows.map((row) => row.map(escape).join(',')).join('\r\n')
  const blob = new Blob([content], { type: 'text/csv;charset=utf-8;' })
  const url = URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.download = filename
  document.body.appendChild(link)
  link.click()
  link.remove()
  URL.revokeObjectURL(url)
}
