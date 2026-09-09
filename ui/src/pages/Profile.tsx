import { useState } from "react";
import { User as UserIcon, Lock, Loader2 } from "lucide-react";

import { useAuth } from "@/contexts/auth-context";
import { updateProfile, changePassword } from "@/lib/api-auth";
import {
  profileEditSchema,
  changePasswordSchema,
  validate,
} from "@/lib/validations";
import { getErrorMessage } from "@/lib/utils";
import { toast } from "sonner";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { PasswordInput } from "@/components/ui/password-input";
import { Skeleton } from "@/components/ui/skeleton";
import { Field, FieldGroup, FieldLabel, FieldError } from "@/components/ui/field";

const ROLE_LABELS: Record<string, string> = {
  admin: "Administrateur",
  coordinator: "Coordinateur",
  teacher: "Enseignant",
  student: "Étudiant",
};

export default function Profile() {
  const { user, updateUser } = useAuth();

  if (!user) return <Skeleton className="h-48 rounded-xl" data-testid="profile-skeleton" />;

  return (
    <div className="flex flex-col gap-6">
      <div>
        <h1 className="text-3xl font-bold tracking-tight" data-testid="profile-header">Mon Profil</h1>
        <p className="text-muted-foreground" data-testid="profile-description">Informations personnelles de votre compte.</p>
      </div>

      <ProfileEditCard user={user} onUpdate={updateUser} />
      <ChangePasswordCard />
    </div>
  );
}

function ProfileEditCard({
  user,
  onUpdate,
}: {
  user: { firstName: string; lastName: string; email: string; role: string };
  onUpdate: (partial: { firstName: string; lastName: string }) => void;
}) {
  const [firstName, setFirstName] = useState(user.firstName);
  const [lastName, setLastName] = useState(user.lastName);
  const [fieldErrors, setFieldErrors] = useState<{ firstName?: string; lastName?: string } | null>(null);
  const [isPending, setIsPending] = useState(false);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    const data = { firstName, lastName };
    const errors = validate(profileEditSchema, data);
    if (errors) {
      setFieldErrors(errors);
      return;
    }
    setFieldErrors(null);
    setIsPending(true);
    try {
      const updated = await updateProfile(data);
      onUpdate({ firstName: updated.firstName, lastName: updated.lastName });
      toast.success("Profil mis à jour avec succès");
    } catch (error) {
      toast.error(getErrorMessage(error, "Erreur lors de la mise à jour du profil"));
    } finally {
      setIsPending(false);
    }
  };

  return (
    <Card data-testid="profile-edit-card">
      <CardHeader>
        <CardTitle className="flex items-center gap-2">
          <UserIcon data-icon="inline-start" />
          Informations personnelles
        </CardTitle>
        <CardDescription>Modifiez votre nom et prénom.</CardDescription>
      </CardHeader>
      <CardContent>
        <form onSubmit={handleSubmit}>
          <FieldGroup>
            <Field data-testid="profile-field-email">
              <FieldLabel>Email</FieldLabel>
              <Input value={user.email} disabled />
            </Field>
            <Field data-testid="profile-field-role">
              <FieldLabel>Rôle</FieldLabel>
              <Input value={ROLE_LABELS[user.role] ?? user.role} disabled />
            </Field>
            <Field data-testid="profile-field-firstName">
              <FieldLabel htmlFor="profile-firstName">Prénom</FieldLabel>
              <Input
                id="profile-firstName"
                value={firstName}
                onChange={(e) => setFirstName(e.target.value)}
                error={fieldErrors?.firstName}
              />
              {fieldErrors?.firstName && <FieldError>{fieldErrors.firstName}</FieldError>}
            </Field>
            <Field data-testid="profile-field-lastName">
              <FieldLabel htmlFor="profile-lastName">Nom</FieldLabel>
              <Input
                id="profile-lastName"
                value={lastName}
                onChange={(e) => setLastName(e.target.value)}
                error={fieldErrors?.lastName}
              />
              {fieldErrors?.lastName && <FieldError>{fieldErrors.lastName}</FieldError>}
            </Field>
            <div>
              <Button type="submit" disabled={isPending} data-testid="profile-save-btn">
                {isPending && <Loader2 data-icon="inline-start" className="animate-spin" />}
                {isPending ? "Enregistrement..." : "Enregistrer"}
              </Button>
            </div>
          </FieldGroup>
        </form>
      </CardContent>
    </Card>
  );
}

function ChangePasswordCard() {
  const [currentPassword, setCurrentPassword] = useState("");
  const [newPassword, setNewPassword] = useState("");
  const [confirmPassword, setConfirmPassword] = useState("");
  const [fieldErrors, setFieldErrors] = useState<{
    currentPassword?: string;
    newPassword?: string;
    confirmPassword?: string;
  } | null>(null);
  const [isPending, setIsPending] = useState(false);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    const data = { currentPassword, newPassword, confirmPassword };
    const errors = validate(changePasswordSchema, data);
    if (errors) {
      setFieldErrors(errors);
      return;
    }
    setFieldErrors(null);
    setIsPending(true);
    try {
      await changePassword({ currentPassword, newPassword });
      toast.success("Mot de passe modifié avec succès");
      setCurrentPassword("");
      setNewPassword("");
      setConfirmPassword("");
    } catch (error) {
      toast.error(getErrorMessage(error, "Erreur lors de la modification du mot de passe"));
    } finally {
      setIsPending(false);
    }
  };

  return (
    <Card data-testid="password-card">
      <CardHeader>
        <CardTitle className="flex items-center gap-2">
          <Lock data-icon="inline-start" />
          Changer le mot de passe
        </CardTitle>
        <CardDescription>Assurez-vous d'utiliser un mot de passe fort.</CardDescription>
      </CardHeader>
      <CardContent>
        <form onSubmit={handleSubmit}>
          <FieldGroup>
            <Field data-testid="password-field-current">
              <FieldLabel htmlFor="password-current">Mot de passe actuel</FieldLabel>
              <PasswordInput
                id="password-current"
                value={currentPassword}
                onChange={(e) => setCurrentPassword(e.target.value)}
                error={fieldErrors?.currentPassword}
              />
              {fieldErrors?.currentPassword && <FieldError>{fieldErrors.currentPassword}</FieldError>}
            </Field>
            <Field data-testid="password-field-new">
              <FieldLabel htmlFor="password-new">Nouveau mot de passe</FieldLabel>
              <PasswordInput
                id="password-new"
                value={newPassword}
                onChange={(e) => setNewPassword(e.target.value)}
                error={fieldErrors?.newPassword}
              />
              {fieldErrors?.newPassword && <FieldError>{fieldErrors.newPassword}</FieldError>}
            </Field>
            <Field data-testid="password-field-confirm">
              <FieldLabel htmlFor="password-confirm">Confirmer le mot de passe</FieldLabel>
              <PasswordInput
                id="password-confirm"
                value={confirmPassword}
                onChange={(e) => setConfirmPassword(e.target.value)}
                error={fieldErrors?.confirmPassword}
              />
              {fieldErrors?.confirmPassword && <FieldError>{fieldErrors.confirmPassword}</FieldError>}
            </Field>
            <div>
              <Button type="submit" disabled={isPending} data-testid="password-save-btn">
                {isPending && <Loader2 data-icon="inline-start" className="animate-spin" />}
                {isPending ? "Mise à jour..." : "Mettre à jour"}
              </Button>
            </div>
          </FieldGroup>
        </form>
      </CardContent>
    </Card>
  );
}
