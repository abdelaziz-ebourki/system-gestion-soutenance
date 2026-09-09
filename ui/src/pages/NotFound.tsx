import { Button } from "@/components/ui/button";
import { FileQuestion } from "lucide-react";
import { useNavigate } from "react-router-dom";
import { useAuth } from "@/contexts/auth-context";

const ROLE_HOMES: Record<string, string> = {
  admin: "/admin",
  coordinator: "/coordinator",
  teacher: "/teacher",
  student: "/student",
};

export default function NotFound() {
  const navigate = useNavigate();
  const { user, isAuthenticated } = useAuth();

  const handleHome = () => {
    if (isAuthenticated && user) {
      navigate(ROLE_HOMES[user.role] ?? "/login");
    } else {
      navigate("/login");
    }
  };

  return (
    <div className="min-h-screen bg-background flex items-center justify-center p-4" data-testid="not-found">
      <div className="flex flex-col items-center gap-6 text-center max-w-md">
        <FileQuestion className="size-24 text-muted-foreground/50" />
        <h1 className="font-heading text-5xl font-bold text-foreground" data-testid="not-found-title">404</h1>
        <p className="text-lg text-muted-foreground" data-testid="not-found-description">
          Page introuvable. La page que vous cherchez n'existe pas ou a été
          déplacée.
        </p>
        <Button onClick={handleHome} variant="default" size="lg" data-testid="not-found-home-btn">
          Retour à l'accueil
        </Button>
      </div>
    </div>
  );
}
