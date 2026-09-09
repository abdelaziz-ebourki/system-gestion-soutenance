import { useState, useMemo } from "react";
import {
  CalendarClock,
  FileCheck2,
  FileText,
  Upload,
  Ban,
  Loader2,
} from "lucide-react";
import { format, parseISO } from "date-fns";
import { fr } from "date-fns/locale";

import { useStudentGroup, useGroupDocuments, useUploadGroupDocument } from "@/hooks/queries";
import { useAuth } from "@/contexts/auth-context";
import type { GroupDocument, GroupDocumentType } from "@/types";
import { toast } from "sonner";
import { getErrorMessage } from "@/lib/utils";
import { GRACE_PERIOD_DAYS, MS_PER_DAY } from "@/lib/constants";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Skeleton } from "@/components/ui/skeleton";
import { StatsCard } from "@/components/ui/stats-card";

const DOCUMENT_CONFIG: Record<GroupDocumentType, { label: string; description: string; icon: typeof FileText }> = {
  REPORT: { label: "Rapport", description: "Rapport de stage / PFE", icon: FileText },
  PRESENTATION: { label: "Présentation", description: "Fiche de présentation / Slides", icon: FileCheck2 },
  DIVERSE: { label: "Divers", description: "Autres documents (annexes, code source, etc.)", icon: FileText },
};

const statusLabel: Record<string, string> = {
  submitted: "Déposé",
  validated: "Validé",
  missing: "Manquant",
  rejected: "Refusé",
};

const statusClass: Record<string, string> = {
  submitted: "bg-secondary text-secondary-foreground",
  validated: "bg-primary text-primary-foreground",
  missing: "bg-destructive/10 text-destructive",
  rejected: "bg-destructive/10 text-destructive line-through",
};

const formatDate = (dateStr: string | null | undefined) => {
  if (!dateStr) return "Non déposé";
  try {
    return format(parseISO(dateStr), "dd MMM yyyy", { locale: fr });
  } catch {
    return dateStr;
  }
};

export default function StudentDocuments() {
  const { user } = useAuth();
  const { data: groupData, isLoading: groupLoading } = useStudentGroup();
  const currentGroup = groupData?.currentGroup ?? null;
  const groupId = currentGroup?.id ?? null;
  const isGroupLeader = currentGroup?.members.some((m) => m.id === user?.id && m.role === "leader") ?? false;

  const { data: documents, isLoading: docsLoading } = useGroupDocuments(groupId);
  const uploadMutation = useUploadGroupDocument();
  const [files, setFiles] = useState<Record<string, File | null>>({});
  const [uploadingType, setUploadingType] = useState<string | null>(null);

  const validatedCount = useMemo(
    () => documents?.filter((d) => d.status === "validated").length ?? 0,
    [documents],
  );
  const missingCount = useMemo(
    () => documents?.filter((d) => d.status === "missing").length ?? 0,
    [documents],
  );

  const documentByType = useMemo(() => {
    const map = new Map<GroupDocumentType, GroupDocument | undefined>();
    for (const type of ["REPORT", "PRESENTATION", "DIVERSE"] as GroupDocumentType[]) {
      map.set(type, documents?.find((d) => d.type === type));
    }
    return map;
  }, [documents]);

  const handleUpload = async (type: GroupDocumentType) => {
    if (!groupId) return;
    const file = files[type];
    if (!file) {
      toast.error("Veuillez sélectionner un fichier.");
      return;
    }

    const document = documentByType.get(type);
    if (!document) return;

    const now = new Date();
    const deadline = new Date(document.deadline);
    const graceDeadline = new Date(deadline);
    graceDeadline.setDate(graceDeadline.getDate() + GRACE_PERIOD_DAYS);

    if (now > graceDeadline) {
      toast.error(`Date limite dépassée depuis ${Math.ceil((now.getTime() - deadline.getTime()) / MS_PER_DAY)} jours. Dépôt bloqué.`);
      return;
    }

    if (now > deadline) {
      toast.warning(`Date limite dépassée. Dépôt en période de grâce (${GRACE_PERIOD_DAYS} jours).`);
    }

    setUploadingType(type);
    try {
      await uploadMutation.mutateAsync({ groupId, type, file });
      toast.success("Document envoyé avec succès");
      setFiles((prev) => ({ ...prev, [type]: null }));
    } catch (error) {
      toast.error(getErrorMessage(error, "Erreur lors de l'envoi du document"));
    } finally {
      setUploadingType(null);
    }
  };

  if (groupLoading) {
    return <Skeleton className="h-[400px] w-full" />;
  }

  if (!currentGroup) {
    return (
      <div className="flex flex-col gap-6">
        <div>
          <h1 className="text-3xl font-bold tracking-tight" data-testid="student-documents-header">Documents</h1>
          <p className="text-muted-foreground">Vous devez faire partie d'un groupe pour accéder aux livrables.</p>
        </div>
      </div>
    );
  }

  return (
    <div className="flex flex-col gap-6">
      <div>
        <h1 className="text-3xl font-bold tracking-tight" data-testid="student-documents-header">Livrables du groupe</h1>
        <p className="text-muted-foreground" data-testid="student-documents-description">
          Groupe : {currentGroup.groupName}
          {isGroupLeader ? " (Chef de groupe — vous pouvez déposer les fichiers)" : " (Consultation seule)"}
        </p>
      </div>

      <div className="grid gap-4 md:grid-cols-3">
        <StatsCard label="Documents attendus" value={3} icon={FileText} data-testid="student-documents-stats-total" />
        <StatsCard label="Validés" value={validatedCount} icon={FileCheck2} data-testid="student-documents-stats-validated" />
        <StatsCard label="Échéances ouvertes" value={missingCount} icon={CalendarClock} data-testid="student-documents-stats-missing" />
      </div>

      {docsLoading ? (
        <div className="flex flex-col gap-3">
          {[1, 2, 3].map((i) => (
            <div key={i} className="rounded-lg border p-4">
              <div className="flex items-center gap-3">
                <Skeleton className="size-10 rounded-lg" />
                <div className="flex flex-col gap-2">
                  <Skeleton className="h-4 w-48" />
                  <Skeleton className="h-3 w-32" />
                </div>
              </div>
            </div>
          ))}
        </div>
      ) : (
        <div className="grid gap-4 md:grid-cols-3">
          {(["REPORT", "PRESENTATION", "DIVERSE"] as GroupDocumentType[]).map((type) => {
            const config = DOCUMENT_CONFIG[type];
            const document = documentByType.get(type);
            const Icon = config.icon;

            return (
              <Card key={type} className="flex flex-col" data-testid={`group-document-card-${type}`}>
                <CardHeader>
                  <div className="flex items-start justify-between">
                    <div className="flex items-center gap-3">
                      <div className="rounded-lg bg-secondary p-3 text-primary">
                        <Icon />
                      </div>
                      <div>
                        <CardTitle className="text-base">{config.label}</CardTitle>
                        <CardDescription>{config.description}</CardDescription>
                      </div>
                    </div>
                    {document && (
                      <Badge className={statusClass[document.status]}>
                        {statusLabel[document.status]}
                      </Badge>
                    )}
                  </div>
                </CardHeader>
                <CardContent className="flex-1 flex flex-col justify-between gap-4">
                  {document ? (
                    <>
                      <div className="flex flex-col gap-1 text-sm text-muted-foreground">
                        <p suppressHydrationWarning>Échéance : {formatDate(document.deadline)}</p>
                        {document.submittedAt && (
                          <p suppressHydrationWarning>Dépôt : {formatDate(document.submittedAt)}</p>
                        )}
                      </div>

                      {document.status === "missing" ? (
                        isGroupLeader ? (
                          <div className="flex flex-col gap-2">
                            <div className="grid w-full items-center gap-1.5">
                              <Label htmlFor={`file-${type}`} className="sr-only">Fichier</Label>
                              <Input
                                id={`file-${type}`}
                                type="file"
                                onChange={(e) => {
                                  const f = e.target.files?.[0] ?? null;
                                  setFiles((prev) => ({ ...prev, [type]: f }));
                                }}
                                data-testid={`group-document-file-input-${type}`}
                              />
                              {files[type] && (
                                <p className="text-xs text-muted-foreground truncate">
                                  {files[type]!.name}
                                </p>
                              )}
                            </div>
                            <Button
                              className="w-full"
                              onClick={() => handleUpload(type)}
                              disabled={uploadingType === type}
                              data-testid={`group-document-upload-btn-${type}`}
                            >
                              {uploadingType === type && <Loader2 data-icon="inline-start" className="animate-spin" />}
                              {uploadingType === type ? "Envoi..." : <><Upload data-icon="inline-start" /> Déposer</>}
                            </Button>
                          </div>
                        ) : (
                          <div className="flex items-center gap-2 text-sm text-muted-foreground">
                            <Ban data-icon="inline-start" />
                            Seul le chef de groupe peut déposer
                          </div>
                        )
                      ) : (
                        <p className="text-sm text-muted-foreground">
                          {document.status === "validated" ? "Document validé par le coordinateur." :
                           document.status === "submitted" ? "En attente de validation." :
                           "Document refusé."}
                        </p>
                      )}
                    </>
                  ) : (
                    <p className="text-sm text-muted-foreground">Aucun document défini.</p>
                  )}
                </CardContent>
              </Card>
            );
          })}
        </div>
      )}
    </div>
  );
}
