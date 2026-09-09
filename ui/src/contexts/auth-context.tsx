import {
  createContext,
  useContext,
  useState,
  useEffect,
  useCallback,
  type ReactNode,
} from "react";
import { STORAGE_KEYS } from "@/lib/constants";
import { logout as logoutApi } from "@/lib/api-auth";
import type { UserRole } from "@/types";

interface User {
  id: number;
  firstName: string;
  lastName: string;
  email: string;
  role: UserRole;
  isActive: boolean;
}

function isValidUser(data: unknown): data is User {
  if (!data || typeof data !== "object") return false;
  const obj = data as Record<string, unknown>;
  return (
    typeof obj.id === "number" &&
    typeof obj.firstName === "string" &&
    typeof obj.lastName === "string" &&
    typeof obj.email === "string" &&
    ["admin", "coordinator", "teacher", "student"].includes(obj.role as string)
  );
}

interface AuthContextValue {
  user: User | null;
  isAuthenticated: boolean;
  isLoading: boolean;
  wasExpired: boolean;
  login: (user: User) => void;
  logout: () => void;
  clearExpired: () => void;
  updateUser: (partial: Partial<User>) => void;
}

const AuthContext = createContext<AuthContextValue | null>(null);

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<User | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [wasExpired, setWasExpired] = useState(false);

  useEffect(() => {
    const storedUser = localStorage.getItem(STORAGE_KEYS.USER);

    if (storedUser) {
      try {
        const parsed = JSON.parse(storedUser);
        if (isValidUser(parsed)) {
          setUser(parsed);
        } else {
          localStorage.removeItem(STORAGE_KEYS.USER);
        }
      } catch {
        localStorage.removeItem(STORAGE_KEYS.USER);
      }
    }
    setIsLoading(false);
  }, []);

  const login = useCallback(
    (newUser: User) => {
      localStorage.setItem(STORAGE_KEYS.USER, JSON.stringify(newUser));
      setUser(newUser);
    },
    [],
  );

  const logout = useCallback(async () => {
    localStorage.removeItem(STORAGE_KEYS.USER);
    setUser(null);
    try {
      await logoutApi();
    } catch {
      // Best-effort: cookie may already be expired
    }
  }, []);

  const clearExpired = useCallback(() => {
    setWasExpired(false);
  }, []);

  const updateUser = useCallback((partial: Partial<User>) => {
    setUser((prev) => {
      if (!prev) return prev;
      const updated = { ...prev, ...partial };
      localStorage.setItem(STORAGE_KEYS.USER, JSON.stringify(updated));
      return updated;
    });
  }, []);

  useEffect(() => {
    const handleExpired = () => {
      setWasExpired(true);
      logout();
      window.location.href = "/login";
    };

    window.addEventListener("auth:expired", handleExpired);
    return () => window.removeEventListener("auth:expired", handleExpired);
  }, [logout]);

  return (
    <AuthContext.Provider
      value={{
        user,
        isAuthenticated: !!user,
        isLoading,
        wasExpired,
        login,
        logout,
        clearExpired,
        updateUser,
      }}
    >
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error("useAuth must be used within an AuthProvider");
  }
  return context;
}
