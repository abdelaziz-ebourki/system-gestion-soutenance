import { Loader2 } from "lucide-react";
import { useState, useEffect } from "react";
import { useSearchParams, useNavigate, Link } from "react-router-dom";
import { zxcvbn, zxcvbnOptions } from "@zxcvbn-ts/core";
import { adjacencyGraphs } from "@zxcvbn-ts/language-common";
import { translations } from "@zxcvbn-ts/language-en";
import { toast } from "sonner";
import { cn, getErrorMessage } from "@/lib/utils";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { PasswordInput } from "@/components/ui/password-input";
import { Field, FieldGroup, FieldLabel } from "@/components/ui/field";
import { resetPassword as apiResetPassword } from "@/lib/api-auth";
import { validate, resetPasswordSchema } from "@/lib/validations";

zxcvbnOptions.setOptions({
  translations,
  graphs: adjacencyGraphs,
  useLevenshteinDistance: true,
});

const PASSWORD_LABELS = ["Très faible", "Faible", "Moyen", "Fort", "Très fort"];
const PASSWORD_COLORS = [
  "bg-red-500",
  "bg-orange-500",
  "bg-yellow-500",
  "bg-lime-500",
  "bg-green-500",
];

export default function ResetPassword() {
  const [searchParams] = useSearchParams();
  const navigate = useNavigate();
  const token = searchParams.get("token");

  const [password, setPassword] = useState("");
  const [confirmPassword, setConfirmPassword] = useState("");
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [fieldErrors, setFieldErrors] = useState<Record<string, string>>({});

  const passwordResult = password ? zxcvbn(password) : null;

  useEffect(() => {
    if (!token) {
      navigate("/login", { replace: true });
    }
  }, [token, navigate]);

  const handleReset = async (e: React.FormEvent) => {
    e.preventDefault();
    const errors = validate(resetPasswordSchema, { password, confirmPassword });
    if (errors) { setFieldErrors(errors); return; }
    setFieldErrors({});
    setIsSubmitting(true);
    try {
      await apiResetPassword(token!, password);
      toast.success("Mot de passe réinitialisé avec succès. Vous pouvez maintenant vous connecter.");
      navigate("/login");
    } catch (error) {
      toast.error(getErrorMessage(error, "Ce lien de réinitialisation est invalide ou a expiré."));
    } finally {
      setIsSubmitting(false);
    }
  };

  if (!token) return null;

  return (
    <div className="flex min-h-screen items-center justify-center p-4">
      <Card className="w-full max-w-md">
        <CardHeader>
          <CardTitle>Réinitialisation du mot de passe</CardTitle>
          <CardDescription>
            Choisissez un nouveau mot de passe pour votre compte.
          </CardDescription>
        </CardHeader>
        <CardContent>
          <form onSubmit={handleReset} data-testid="reset-password-form">
            <FieldGroup>
              <Field>
                <FieldLabel>Nouveau mot de passe</FieldLabel>
                <PasswordInput
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  required
                  error={fieldErrors?.password}
                  data-testid="reset-password-input"
                />
                {passwordResult && (
                  <div className="mt-2 flex flex-col gap-1">
                    <div className="flex gap-1">
                      {[0, 1, 2, 3, 4].map((level) => (
                        <div
                          key={level}
                          className={cn("h-1.5 flex-1 rounded-full transition-colors", level <= passwordResult.score ? PASSWORD_COLORS[passwordResult.score] : "bg-muted")}
                        />
                      ))}
                    </div>
                    <div className="flex items-center justify-between">
                      <span className="text-xs text-muted-foreground">
                        {PASSWORD_LABELS[passwordResult.score]}
                      </span>
                      {passwordResult.feedback.suggestions.length > 0 && (
                        <span className="text-xs text-muted-foreground">
                          {passwordResult.feedback.suggestions[0]}
                        </span>
                      )}
                    </div>
                  </div>
                )}
              </Field>
              <Field>
                <FieldLabel>Confirmer le mot de passe</FieldLabel>
                <PasswordInput
                  value={confirmPassword}
                  onChange={(e) => setConfirmPassword(e.target.value)}
                  required
                  error={fieldErrors?.confirmPassword}
                  data-testid="reset-password-confirm-input"
                />
              </Field>
            </FieldGroup>
            <Button
              className="w-full"
              type="submit"
              disabled={isSubmitting}
              data-testid="reset-password-submit-button"
            >
              {isSubmitting && <Loader2 data-icon="inline-start" className="animate-spin" />}
              {isSubmitting ? "Réinitialisation..." : "Réinitialiser le mot de passe"}
            </Button>
          </form>
          <div className="mt-4 text-center text-sm">
            <Link
              to="/login"
              className="text-muted-foreground hover:text-primary transition-colors"
            >
              Retour à la connexion
            </Link>
          </div>
        </CardContent>
      </Card>
    </div>
  );
}
