import { clearSession, readSession, writeSession } from "./lib/session";

const API_BASE_URL = "http://localhost:8080/api";
let onSessionUpdate = () => {};
let onUnauthorized = () => {};

export function registerApiAuthHandlers(handlers) {
  onSessionUpdate = handlers.onSessionUpdate || (() => {});
  onUnauthorized = handlers.onUnauthorized || (() => {});
}

async function parseResponse(response) {
  if (response.status === 204) {
    return null;
  }
  return response.json().catch(() => null);
}

function buildErrorMessage(errorPayload) {
  const details = errorPayload?.details?.join(", ");
  const message = errorPayload?.message || "Request failed";
  return details ? `${message}: ${details}` : message;
}

async function refreshSessionIfNeeded(refreshToken) {
  const response = await fetch(`${API_BASE_URL}/auth/refresh`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ refreshToken }),
  });

  if (!response.ok) {
    const payload = await parseResponse(response);
    throw new Error(buildErrorMessage(payload));
  }

  const nextSession = await response.json();
  writeSession(nextSession);
  onSessionUpdate(nextSession);
  return nextSession;
}

async function request(path, options = {}, retryOnUnauthorized = true) {
  const session = readSession();
  const response = await fetch(`${API_BASE_URL}${path}`, {
    headers: {
      "Content-Type": "application/json",
      ...(session?.accessToken ? { Authorization: `Bearer ${session.accessToken}` } : {}),
      ...(options.headers || {}),
    },
    method: options.method || "GET",
    body: options.body ? JSON.stringify(options.body) : undefined,
  });

  if (response.status === 401 && retryOnUnauthorized && session?.refreshToken && !path.startsWith("/auth/")) {
    try {
      const nextSession = await refreshSessionIfNeeded(session.refreshToken);
      return request(path, {
        ...options,
        headers: {
          ...(options.headers || {}),
          Authorization: `Bearer ${nextSession.accessToken}`,
        },
      }, false);
    } catch {
      clearSession();
      onUnauthorized();
      throw new Error("Your session has expired. Please log in again.");
    }
  }

  if (!response.ok) {
    const errorPayload = await parseResponse(response);
    throw new Error(buildErrorMessage(errorPayload));
  }

  return parseResponse(response);
}

export const api = {
  login(payload) {
    return request("/auth/login", { method: "POST", body: payload }, false);
  },
  signup(payload) {
    return request("/auth/signup", { method: "POST", body: payload }, false);
  },
  refresh(refreshToken) {
    return request("/auth/refresh", { method: "POST", body: { refreshToken } }, false);
  },
  logout(refreshToken) {
    return request("/auth/logout", { method: "POST", body: { refreshToken } }, false);
  },
  forgotPassword(email) {
    return request("/auth/forgot-password", { method: "POST", body: { email } }, false);
  },
  resetPassword(payload) {
    return request("/auth/reset-password", { method: "POST", body: payload }, false);
  },
  getCurrentUser() {
    return request("/auth/me");
  },
  updateProfile(payload) {
    return request("/auth/me", { method: "PUT", body: payload });
  },
  changePassword(payload) {
    return request("/auth/change-password", { method: "POST", body: payload });
  },
  getSummary() {
    return request("/dashboard/summary");
  },
  getRecords(filters = {}) {
    const params = new URLSearchParams();
    Object.entries(filters).forEach(([key, value]) => {
      if (value) {
        params.append(key, value);
      }
    });
    const query = params.toString() ? `?${params.toString()}` : "";
    return request(`/records${query}`);
  },
  getRecord(recordId) {
    return request(`/records/${recordId}`);
  },
  createRecord(payload) {
    return request("/records", { method: "POST", body: payload });
  },
  updateRecord(recordId, payload) {
    return request(`/records/${recordId}`, { method: "PUT", body: payload });
  },
  deleteRecord(recordId) {
    return request(`/records/${recordId}`, { method: "DELETE" });
  },
  getUsers() {
    return request("/users");
  },
  getUser(userId) {
    return request(`/users/${userId}`);
  },
  createUser(payload) {
    return request("/users", { method: "POST", body: payload });
  },
  updateUser(userId, payload) {
    return request(`/users/${userId}`, { method: "PUT", body: payload });
  },
};
