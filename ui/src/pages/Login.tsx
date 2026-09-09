import { useState, type FormEvent } from "react";
import { Loader2, Landmark, ShieldCheck, BookOpen } from "lucide-react";
import { Card, CardContent, CardDescription, CardFooter, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { PasswordInput } from "@/components/ui/password-input";
import { Field, FieldGroup, FieldLabel } from "@/components/ui/field";
import { Separator } from "@/components/ui/separator";
import { toast } from "sonner";
import { getErrorMessage } from "@/lib/utils";
import { Link, useNavigate } from "react-router-dom";
import { login as loginApi } from "@/lib/api-auth";
import { useAuth } from "@/contexts/auth-context";
import { validate, loginSchema } from "@/lib/validations";
import { siteConfig } from "@/config/site";

export default function Login() {
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [fieldErrors, setFieldErrors] = useState<Record<string, string>>({});
  const navigate = useNavigate();
  const { login } = useAuth();

  const handleLogin = async (e: FormEvent<HTMLFormElement>) => {
    e.preventDefault();
    const errors = validate(loginSchema, { email, password });
    if (errors) { setFieldErrors(errors); return; }
    setFieldErrors({});
    setIsSubmitting(true);

    try {
      const data = await loginApi({ email, password });

      toast.success(`Bienvenue, ${data.user.firstName} ${data.user.lastName}`);

      login(data.user);

      const roleRoutes: Record<string, string> = {
        admin: "/admin",
        coordinator: "/coordinator",
        teacher: "/teacher",
        student: "/student",
      };

      navigate(roleRoutes[data.user.role] || "/login");
    } catch (error) {
      toast.error(getErrorMessage(error, "Erreur de connexion"));
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <div className="min-h-screen bg-background flex flex-col items-center justify-center p-4 relative overflow-hidden">
      <svg
        className="absolute inset-0 w-full h-full pointer-events-none text-primary/5"
        viewBox="0 0 1440 900"
        fill="none"
        preserveAspectRatio="xMidYMid slice"
        aria-hidden="true"
      >
        <circle cx="200" cy="150" r="400" fill="currentColor" />
        <circle cx="1240" cy="750" r="350" fill="currentColor" />
        <circle cx="720" cy="450" r="500" fill="currentColor" className="text-accent/5" />
        <rect x="1100" y="100" width="300" height="300" rx="60" fill="currentColor" className="text-primary/8" transform="rotate(30 1250 250)" />
        <rect x="50" y="600" width="200" height="200" rx="40" fill="currentColor" className="text-accent/8" transform="rotate(-20 150 700)" />
        <path d="M720 0 L900 450 L540 450 Z" fill="currentColor" className="text-primary/5" />
        <path d="M0 900 L200 600 L400 900 Z" fill="currentColor" className="text-accent/5" />
      </svg>

      <div className="w-full max-w-4xl grid md:grid-cols-2 gap-8 items-center z-10">
        <div className="space-y-6 text-center md:text-left">
          <div>
            <h1 className="font-heading text-4xl font-bold tracking-tight text-foreground">
              {siteConfig.name}
            </h1>
            <p className="text-muted-foreground font-medium uppercase tracking-widest text-xs">
              {siteConfig.subtitle}
            </p>
          </div>

          <h2 className="font-heading text-5xl font-medium leading-[1.1] text-foreground">
            L'excellence académique <br />
            <span className="italic text-primary">au bout des doigts.</span>
          </h2>

          <p className="text-lg text-muted-foreground max-w-md">
            {siteConfig.description}
          </p>

          <div className="flex flex-wrap gap-4 pt-4 justify-center md:justify-start">
            <div className="flex items-center gap-2 text-sm text-muted-foreground border-r pr-4">
              <Landmark className="size-4" />
              <span>Officiel</span>
            </div>
            <div className="flex items-center gap-2 text-sm text-muted-foreground border-r pr-4">
              <ShieldCheck className="size-4" />
              <span>Sécurisé</span>
            </div>
            <div className="flex items-center gap-2 text-sm text-muted-foreground">
              <BookOpen className="size-4" />
              <span>Intuitif</span>
            </div>
          </div>
        </div>

        <Card>
          <CardHeader className="flex flex-col gap-1 text-center">
            <CardTitle className="text-3xl">Connexion</CardTitle>
            <CardDescription>
              Veuillez entrer vos identifiants pour accéder à votre espace
            </CardDescription>
          </CardHeader>
          <form onSubmit={handleLogin} data-testid="login-form">
            <CardContent className="flex flex-col gap-4">
              <div className="relative py-4">
                <div className="absolute inset-0 flex items-center">
                  <Separator className="w-full" />
                </div>
                <div className="relative flex justify-center text-xs uppercase">
                  <span className="bg-card px-2 text-muted-foreground font-medium tracking-wider">
                    Identifiants
                  </span>
                </div>
              </div>

              <FieldGroup>
                <Field>
                  <FieldLabel>Email académique</FieldLabel>
                  <Input
                    id="email"
                    type="email"
                    placeholder={`nom.prenom@${siteConfig.emailDomain}`}
                    value={email}
                    onChange={(e) => setEmail(e.target.value)}
                    required
                    disabled={isSubmitting}
                    error={fieldErrors?.email}
                    data-testid="login-email-input"
                  />
                </Field>
                <Field>
                  <FieldLabel>Mot de passe</FieldLabel>
                  <PasswordInput
                    id="password"
                    placeholder="••••••••"
                    value={password}
                    onChange={(e) => setPassword(e.target.value)}
                    required
                    disabled={isSubmitting}
                    error={fieldErrors?.password}
                    data-testid="login-password-input"
                  />
                </Field>
              </FieldGroup>
              <div className="text-right text-sm">
                <Link
                  to="/forgot-password"
                  className="text-muted-foreground hover:text-primary transition-colors"
                >
                  Mot de passe oublié ?
                </Link>
              </div>
            </CardContent>
            <CardFooter className="mt-6">
              <Button
                type="submit"
                className="w-full"
                variant="default"
                disabled={isSubmitting}
                data-testid="login-submit-button"
              >
                {isSubmitting && <Loader2 data-icon="inline-start" className="animate-spin" />}
                {isSubmitting ? "Connexion en cours..." : "Se connecter"}
              </Button>
            </CardFooter>
          </form>
        </Card>
      </div>

      <footer className="md:absolute pt-10 bottom-8 text-sm text-muted-foreground flex gap-6">
        <span>&copy; {new Date().getFullYear()} Copyright</span>
        <a href={`mailto:${siteConfig.supportEmail}`} className="hover:text-primary transition-colors">
          Assistance
        </a>
      </footer>
    </div>
  );
}
