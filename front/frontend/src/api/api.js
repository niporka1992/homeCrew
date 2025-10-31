// src/api.js
export const API_BASE = '/api'

async function apiFetch(path, options = {}) {
    const token = localStorage.getItem('token')

    const headers = {
        'Content-Type': 'application/json',
        ...(token ? { Authorization: `Bearer ${token}` } : {}),
        ...options.headers,
    }

    const res = await fetch(`${API_BASE}${path}`, {
        ...options,
        headers,
    })

    if (res.status === 401) {
        localStorage.removeItem('token')
        window.location.href = '/login'
        return
    }

    let data = null
    try {
        data = await res.json()
    } catch (_) {}

    if (!res.ok) {
        const msg = data?.message || `Ошибка ${res.status}: ${res.statusText}`
        throw new Error(msg)
    }

    return data
}

// 🔹 Добавляем новую версию для бинарных файлов (Blob)
async function apiFetchBlob(path, options = {}) {
    const token = localStorage.getItem('token')

    const headers = {
        ...(token ? { Authorization: `Bearer ${token}` } : {}),
        ...options.headers,
    }

    const res = await fetch(`${API_BASE}${path}`, {
        ...options,
        headers,
    })

    if (res.status === 401) {
        localStorage.removeItem('token')
        window.location.href = '/login'
        return
    }

    if (!res.ok) {
        throw new Error(`Ошибка ${res.status}: ${res.statusText}`)
    }

    return await res.blob()
}

// Универсальные методы API
export const api = {
    get: (path, options = {}) => apiFetch(path, { ...options, method: 'GET' }),
    post: (path, body = {}, options = {}) =>
        apiFetch(path, {
            ...options,
            method: 'POST',
            body: JSON.stringify(body),
        }),
    patch: (path, body = {}, options = {}) =>
        apiFetch(path, {
            ...options,
            method: 'PATCH',
            body: JSON.stringify(body),
        }),
    put: (path, body = {}, options = {}) =>
        apiFetch(path, {
            ...options,
            method: 'PUT',
            body: JSON.stringify(body),
        }),
    delete: (path, options = {}) =>
        apiFetch(path, { ...options, method: 'DELETE' }),

    // ✅ Новый метод для скачивания файлов с токеном
    blob: (path, options = {}) => apiFetchBlob(path, { ...options, method: 'GET' }),
}
