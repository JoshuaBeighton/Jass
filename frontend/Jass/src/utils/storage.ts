const storage = import.meta.env.DEV ? window.sessionStorage : window.localStorage

export function getStoredValue<T>(key: string, fallback: T): T {
  if (typeof window === 'undefined') {
    return fallback
  }

  try {
    const stored = storage.getItem(key)
    return stored ? (JSON.parse(stored) as T) : fallback
  } catch {
    return fallback
  }
}

export function setStoredValue<T>(key: string, value: T): void {
  if (typeof window === 'undefined') {
    return
  }

  storage.setItem(key, JSON.stringify(value))
}
