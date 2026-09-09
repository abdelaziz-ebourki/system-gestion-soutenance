import { useState, type FormEvent } from "react";
import { Link } from "react-router-dom";
import { ArrowLeft, Mail, Loader2 } from "lucide-react";
import { toast } from "sonner";
import { getErrorMessage } from "@/lib/utils";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Field, FieldGroup, FieldLabel } from "@/components/ui/field";
import { forgotPassword } from "@/lib/api-auth";
import { validate, forgotPasswordSchema } from "@/lib/validations";
import { siteConfig } from "@/config/site";
export default function ForgotPassword() {
  const [email, setEmail] = useState("");
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [sent, setSent] = useState(false);
  const [fieldErrors, setFieldErrors] = useState<Record<string, string>>({});

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault();
    const errors = validate(forgotPasswordSchema, { email });
    if (errors) { setFieldErrors(errors); return; }
    setFieldErrors({});
    setIsSubmitting(true);
    try {
      await forgotPassword(email);
      setSent(true);
      toast.success("Si cet email existe, un lien de réinitialisation a été envoyé.");
    } catch (error) {
      toast.error(getErrorMessage(error, "Erreur lors de l'envoi du lien."));
    } finally {
      setIsSubmitting(false);
    }
  };

  if (sent) {
    return (
      <div className="flex min-h-screen items-center justify-center p-4">
        <Card className="w-full max-w-md" data-testid="forgot-password-success-card">
          <CardHeader className="text-center">
            <div className="mx-auto mb-4 flex size-12 items-center justify-center rounded-full bg-primary/10">
              <Mail className="text-primary" />
            </div>
            <CardTitle>Email envoyé</CardTitle>
            <CardDescription>
              Si un compte existe avec cette adresse, vous recevrez un lien de
              réinitialisation par email. Vérifiez votre boîte de réception.
            </CardDescription>
          </CardHeader>
          <CardContent className="text-center">
            <Button variant="outline" asChild>
              <Link to="/login">Retour à la connexion</Link>
            </Button>
          </CardContent>
        </Card>
      </div>
    );
  }

  return (
    <div className="flex min-h-screen items-center justify-center p-4">
      <Card className="w-full max-w-md">
        <CardHeader>
          <CardTitle>Mot de passe oublié</CardTitle>
          <CardDescription>
            Saisissez votre adresse email pour recevoir un lien de
            réinitialisation.
          </CardDescription>
        </CardHeader>
        <CardContent>
          <form onSubmit={handleSubmit} data-testid="forgot-password-form">
            <FieldGroup>
              <Field>
                <FieldLabel>Email académique</FieldLabel>
                <Input
                  type="email"
                  placeholder={`nom.prenom@${siteConfig.emailDomain}`}
                  value={email}
                  onChange={(e) => setEmail(e.target.value)}
                  required
                  error={fieldErrors?.email}
                  data-testid="forgot-password-email-input"
                />
              </Field>
            </FieldGroup>
            <Button
              className="w-full"
              type="submit"
              disabled={isSubmitting}
              data-testid="forgot-password-submit-button"
            >
              {isSubmitting && <Loader2 data-icon="inline-start" className="animate-spin" />}
              {isSubmitting ? "Envoi en cours..." : "Envoyer le lien"}
            </Button>
          </form>
          <div className="mt-4 text-center text-sm">
            <Link
              to="/login"
              className="inline-flex items-center gap-1 text-muted-foreground hover:text-primary transition-colors"
            >
              <ArrowLeft data-icon="inline-start" />
              Retour à la connexion
            </Link>
          </div>
        </CardContent>
      </Card>
    </div>
  );
}
