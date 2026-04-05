import { createContext, useContext, useEffect, useState } from "react";
import { api, registerApiAuthHandlers } from "../api";
import { clearSession, readSession, writeSession } from "../lib/session";

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [session, setSession] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    setSession(readSession());
    setLoading(false);
  }, []);

  useEffect(() => {
    registerApiAuthHandlers({
      onSessionUpdate(nextSession) {
        setSession(nextSession);
      },
      onUnauthorized() {
        setSession(null);
      },
    });
  }, []);

  async function login(payload) {
    const nextSession = await api.login(payload);
    setSession(nextSession);
    writeSession(nextSession);
    return nextSession;
  }

  async function signup(payload) {
    const nextSession = await api.signup(payload);
    setSession(nextSession);
    writeSession(nextSession);
    return nextSession;
  }

  async function logout() {
    const refreshToken = session?.refreshToken;
    clearSession();
    setSession(null);

    if (refreshToken) {
      try {
        await api.logout(refreshToken);
      } catch {
        // Ignore logout failures after local session has already been cleared.
      }
    }
  }

  function updateSession(nextSession) {
    setSession(nextSession);
    if (nextSession) {
      writeSession(nextSession);
    } else {
      clearSession();
    }
  }

  function updateUser(user) {
    if (!session) {
      return;
    }
    updateSession({ ...session, user });
  }

  return (
    <AuthContext.Provider
      value={{
        session,
        user: session?.user || null,
        loading,
        login,
        signup,
        logout,
        updateUser,
        updateSession,
      }}
    >
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error("useAuth must be used inside AuthProvider");
  }
  return context;
}
