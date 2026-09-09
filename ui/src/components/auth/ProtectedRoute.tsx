import { Navigate, Outlet } from "react-router-dom";
import { useAuth } from "@/contexts/auth-context";
import { toast } from "sonner";
import { useEffect } from "react";

interface ProtectedRouteProps {
  allowedRoles: string[];
}

export default function ProtectedRoute({ allowedRoles }: ProtectedRouteProps) {
  const { user, isAuthenticated, isLoading, wasExpired, clearExpired } = useAuth();

  useEffect(() => {
    if (!isAuthenticated && wasExpired) {
      toast.error("Votre session a expiré. Veuillez vous reconnecter.");
      clearExpired();
    }
  }, [isAuthenticated, wasExpired, clearExpired]);

  if (isLoading) {
    return null;
  }

  if (!isAuthenticated) {
    return <Navigate to="/login" replace />;
  }

  if (!allowedRoles.includes(user!.role)) {
    toast.error("Vous n'avez pas les droits nécessaires pour accéder à cette page.");
    return <Navigate to="/login" replace />;
  }

  return <Outlet />;
}
