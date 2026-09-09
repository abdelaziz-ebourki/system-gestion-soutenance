import { FolderKanban, Mail, Plus, UserRound, Users, Loader2 } from "lucide-react";

import {
  useStudentGroup,
  useCreateStudentGroup,
  useJoinStudentGroup,
} from "@/hooks/queries";
import { toast } from "sonner";
import { getErrorMessage } from "@/lib/utils";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Skeleton } from "@/components/ui/skeleton";
import { EmptyState } from "@/components/ui/empty-state";
import { StatsCard } from "@/components/ui/stats-card";

export default function StudentGroup() {
  const { data: workspace, isLoading } = useStudentGroup();
  const createGroup = useCreateStudentGroup();
  const joinGroup = useJoinStudentGroup();

  const isSubmitting = createGroup.isPending || joinGroup.isPending;

  const handleCreateGroup = async () => {
    try {
      await createGroup.mutateAsync();
      toast.success("Votre groupe a été créé automatiquement");
    } catch (error) {
      toast.error(getErrorMessage(error, "Erreur lors de la création"));
    }
  };

  const handleJoinGroup = async (groupId: number) => {
    try {
      await joinGroup.mutateAsync(groupId);
      toast.success("Vous avez rejoint le groupe sélectionné");
    } catch (error) {
      toast.error(getErrorMessage(error, "Erreur lors de la jonction"));
    }
  };

  const group = workspace?.currentGroup || null;

  return (
    <div className="flex flex-col gap-6">
      <div>
        <h1 className="text-3xl font-bold tracking-tight" data-testid="student-group-header">Mon groupe</h1>
        <p className="text-muted-foreground" data-testid="student-group-description">
          Consultez la composition de votre groupe et les informations du
          projet.
        </p>
      </div>

      <div className="grid gap-4 md:grid-cols-3">
        <StatsCard
          label="Nom du groupe"
          value={group?.groupName || "Aucun groupe"}
          icon={Users}
          loading={isLoading}
          valueClassName="text-xl font-semibold"
          data-testid="student-group-stats-name"
        />
        <StatsCard label="Membres" value={group?.members.length || 0} icon={UserRound} loading={isLoading} data-testid="student-group-stats-members" />
        <StatsCard
          label="Encadrant"
          value={group?.supervisorName || "En attente"}
          icon={FolderKanban}
          loading={isLoading}
          valueClassName="text-xl font-semibold"
          data-testid="student-group-stats-supervisor"
        />
      </div>

      <div className="grid gap-6 xl:grid-cols-[1.1fr_0.9fr]">
        <Card data-testid="student-group-project-card">
          <CardHeader>
            <CardTitle>Projet affecté</CardTitle>
            <CardDescription>
              Le sujet actuellement rattaché à votre groupe, lorsque
              l'affectation existe.
            </CardDescription>
          </CardHeader>
          <CardContent>
            <div className="rounded-lg border p-5">
              <p className="text-sm text-muted-foreground">Titre</p>
              <div className="mt-2 text-xl font-semibold">
                {isLoading
                  ? <Skeleton className="h-6 w-64" />
                  : group?.projectTitle || "Projet non affecté"}
              </div>
              <div className="mt-4">
                <Badge variant="secondary">
                  {group?.supervisorName || "Encadrant en attente"}
                </Badge>
              </div>
            </div>
          </CardContent>
        </Card>

        <Card data-testid="student-group-members-card">
          <CardHeader>
            <CardTitle>Membres du groupe</CardTitle>
            <CardDescription>
              La répartition actuelle de votre équipe.
            </CardDescription>
          </CardHeader>
          <CardContent className="flex flex-col gap-3">
            {group?.members.map((member) => (
              <div key={member.id} className="rounded-lg border p-4" data-testid={`student-group-member-${member.id}`}>
                <div className="flex flex-wrap items-center justify-between gap-3">
                  <div>
                    <p className="font-medium">{member.fullName}</p>
                    <p className="mt-1 inline-flex items-center gap-2 text-sm text-muted-foreground">
                      <Mail />
                      {member.email}
                    </p>
                  </div>
                  <Badge variant="outline">
                    {member.role === "leader"
                      ? "Responsable de groupe"
                      : "Membre"}
                  </Badge>
                </div>
              </div>
            ))}
            {!isLoading && !group && (
              <EmptyState variant="card" description="Vous n'appartenez à aucun groupe pour le moment." />
            )}
          </CardContent>
        </Card>
      </div>

      <Card data-testid="student-group-creation-card">
        <CardHeader>
          <CardTitle>Fenêtre de création des groupes</CardTitle>
          <CardDescription>
            Les groupes sont nommés automatiquement par le système: Groupe-1,
            Groupe-2, etc.
          </CardDescription>
        </CardHeader>
        <CardContent className="flex flex-col gap-4">
          <div className="rounded-lg border p-4 text-sm text-muted-foreground">
            Période autorisée: {workspace?.groupCreationStartDate} au{" "}
            {workspace?.groupCreationEndDate}
          </div>
          {workspace?.isGroupCreationOpen ? (
            <div className="flex flex-wrap gap-3">
              <Button
                onClick={handleCreateGroup}
                disabled={Boolean(group) || isSubmitting}
                data-testid="student-group-create-btn"
              >
                {isSubmitting ? <><Loader2 data-icon="inline-start" className="animate-spin" /> Création...</> : <><Plus data-icon="inline-start" /> Créer un groupe</>}
              </Button>
            </div>
          ) : (
            <div className="rounded-lg border bg-destructive/10 p-4 text-sm text-destructive">
              La création et la jonction des groupes sont actuellement fermées.
            </div>
          )}

          {workspace && workspace.availableGroups.length > 0 && !group && (
            <div className="flex flex-col gap-3">
              <p className="text-sm font-medium">Groupes disponibles</p>
              {workspace.availableGroups.map((availableGroup) => (
                <div
                  key={availableGroup.id}
                  className="flex flex-wrap items-center justify-between gap-3 rounded-lg border p-4"
                  data-testid={`student-group-available-${availableGroup.id}`}
                >
                  <div>
                    <p className="font-medium">{availableGroup.groupName}</p>
                    <p className="text-sm text-muted-foreground">
                      {availableGroup.memberCount} membre(s)
                    </p>
                  </div>
                  <Button
                    variant="outline"
                    onClick={() => handleJoinGroup(availableGroup.id)}
                    disabled={!workspace.isGroupCreationOpen || isSubmitting}
                    data-testid={`student-group-join-btn-${availableGroup.id}`}
                  >
                    {isSubmitting ? <><Loader2 data-icon="inline-start" className="animate-spin" /> Jonction...</> : "Rejoindre"}
                  </Button>
                </div>
              ))}
            </div>
          )}
        </CardContent>
      </Card>
    </div>
  );
}

