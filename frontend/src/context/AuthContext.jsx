import { createContext, useContext, useState, useEffect } from "react";

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [token, setToken] = useState(() => localStorage.getItem("mandal-token"));
  const [user, setUser] = useState(() => {
    const stored = localStorage.getItem("mandal-user");
    return stored ? JSON.parse(stored) : null;
  });

  const [mandalName, setMandalName] = useState(() => localStorage.getItem("mandal-name"));

  useEffect(() => {
    if (token && user?.mandalId) {
      import("@/api").then(api => {
         api.getMandal().then(res => {
            const name = res.data?.mandalName || "";
            setMandalName(name);
            localStorage.setItem("mandal-name", name);
         }).catch(console.error);
      });
    } else {
      setMandalName(null);
      localStorage.removeItem("mandal-name");
    }
  }, [token, user?.mandalId, user?.role]);

  const login = (jwt, userData) => {
    setToken(jwt);
    setUser(userData);
    localStorage.setItem("mandal-token", jwt);
    localStorage.setItem("mandal-user", JSON.stringify(userData));
  };

  const logout = () => {
    setToken(null);
    setUser(null);
    setMandalName(null);
    localStorage.removeItem("mandal-token");
    localStorage.removeItem("mandal-user");
    localStorage.removeItem("mandal-name");
  };

  return (
    <AuthContext.Provider value={{ token, user, mandalName, login, logout, setMandalName }}>
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error("useAuth must be used within AuthProvider");
  return ctx;
}
