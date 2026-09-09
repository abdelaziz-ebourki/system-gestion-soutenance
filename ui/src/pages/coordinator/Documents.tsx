import { useState, useCallback, useMemo } from "react";
import { FileText, Users, Calendar, ClipboardList, ScrollText, Loader2, Search } from "lucide-react";
import { useJuries, useProjects, useCoordinatorDefenseSessions, useProjectGrades } from "@/hooks/queries";
import * as api from "@/lib/api";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { EmptyState } from "@/components/ui/empty-state";
import { Dialog, DialogContent, DialogDescription, DialogFooter, DialogHeader, DialogTitle } from "@/components/ui/dialog";
import { Input } from "@/components/ui/input";
import { Skeleton } from "@/components/ui/skeleton";

import { toast } from "sonner";

const DOC_TYPES: {
  id: string;
  title: string;
  description: string;
  icon: React.ComponentType<{ className?: string }>;
  actionLabel: string;
  needsPicker: boolean;
}[] = [
  {
    id: "evaluation-sheet",
    title: "Fiches d'évaluation",
    description: "Fiches d'évaluation individuelles par projet avec grille de notation et signatures.",
    icon: ClipboardList,
    actionLabel: "Générer",
    needsPicker: true,
  },
  {
    id: "proces-verbal",
    title: "Procès-Verbaux (PV)",
    description: "PV de soutenance avec notes, décisions et signatures du jury.",
    icon: ScrollText,
    actionLabel: "Générer",
    needsPicker: true,
  },
  {
    id: "attendance-list",
    title: "Listes de présence",
    description: "Listes d'émargement par date avec créneaux et signatures.",
    icon: Users,
    actionLabel: "Choisir une date",
    needsPicker: false,
  },
  {
    id: "jury-convocation",
    title: "Convocations jury",
    description: "Convocations individuelles pour chaque membre du jury.",
    icon: FileText,
    actionLabel: "Générer",
    needsPicker: true,
  },
  {
    id: "schedule",
    title: "Planning des soutenances",
    description: "Planning complet des soutenances par session avec salles et jurys.",
    icon: Calendar,
    actionLabel: "Générer",
    needsPicker: false,
  },
];

function openPdfBlob(blob: Blob) {
  const url = URL.createObjectURL(blob);
  window.open(url, "_blank");
}

export default function Documents() {
  const [isDateDialogOpen, setIsDateDialogOpen] = useState(false);
  const [dateInput, setDateInput] = useState(() => new Date().toISOString().split("T")[0]);
  const [loadingPdf, setLoadingPdf] = useState<string | null>(null);
  const [pickerDocId, setPickerDocId] = useState<string | null>(null);
  const [pickerSearch, setPickerSearch] = useState("");

  const projectsQuery = useProjects();
  const juriesQuery = useJuries();
  const sessionsQuery = useCoordinatorDefenseSessions();
  const gradesQuery = useProjectGrades();

  const juries = useMemo(() => juriesQuery.data?.items ?? [], [juriesQuery.data]);
  const sessions = useMemo(() => sessionsQuery.data?.items ?? [], [sessionsQuery.data]);
  const grades = useMemo(() => gradesQuery.data ?? [], [gradesQuery.data]);

  const isLoading = projectsQuery.isLoading || juriesQuery.isLoading || sessionsQuery.isLoading;

  const pickerItems = useMemo(() => {
    if (pickerDocId === "jury-convocation") {
      return juries.map((j) => ({ id: j.projectId, label: j.projectTitle }));
    }
    return grades.map((g) => ({ id: g.projectId, label: g.projectTitle }));
  }, [pickerDocId, juries, grades]);

  const filteredPickerItems = useMemo(() => {
    if (!pickerSearch) return pickerItems;
    const q = pickerSearch.toLowerCase();
    return pickerItems.filter((item) => item.label.toLowerCase().includes(q));
  }, [pickerItems, pickerSearch]);

  const handleOpenDateDialog = () => {
    setDateInput(new Date().toISOString().split("T")[0]);
    setIsDateDialogOpen(true);
  };

  const fetchAndOpenPdf = useCallback(async (fetcher: () => Promise<Blob>, key: string) => {
    setLoadingPdf(key);
    try {
      const blob = await fetcher();
      openPdfBlob(blob);
    } catch {
      toast.error("Erreur lors de la génération du PDF.");
    } finally {
      setLoadingPdf(null);
    }
  }, []);

  const handleGenerate = (doc: (typeof DOC_TYPES)[number]) => {
    if (doc.needsPicker) {
      setPickerDocId(doc.id);
      setPickerSearch("");
      return;
    }

    if (doc.id === "schedule") {
      if (sessions.length === 0) { toast.error("Aucune session disponible."); return; }
      fetchAndOpenPdf(() => api.getDefenseScheduleDocPdf(sessions[0].id), `schedule-${sessions[0].id}`);
    } else if (doc.id === "attendance-list") {
      handleOpenDateDialog();
    }
  };

  const handlePickerSelect = (projectId: number) => {
    if (!pickerDocId) return;

    if (pickerDocId === "jury-convocation") {
      const jury = juries.find((j) => j.projectId === projectId);
      if (!jury) { toast.error("Aucun jury pour ce projet."); return; }
      if (jury.members.length === 0) { toast.error("Aucun membre dans ce jury."); return; }
      fetchAndOpenPdf(() => api.getJuryConvocationsPdf(projectId), `jury-convocation-${projectId}`);
    } else if (pickerDocId === "evaluation-sheet") {
      fetchAndOpenPdf(() => api.getEvaluationSheetPdf(projectId), `evaluation-sheet-${projectId}`);
    } else if (pickerDocId === "proces-verbal") {
      fetchAndOpenPdf(() => api.getProcesVerbalPdf(projectId), `proces-verbal-${projectId}`);
    }

    setPickerDocId(null);
  };

  const handleAttendanceGenerate = () => {
    if (sessions.length === 0) { toast.error("Aucune session disponible."); return; }
    fetchAndOpenPdf(
      () => api.getAttendanceListPdf(sessions[0].id),
      `attendance-list-${dateInput}`,
    );
    setIsDateDialogOpen(false);
  };

  const pickerTitle = DOC_TYPES.find((d) => d.id === pickerDocId)?.title ?? "";

  if (isLoading) {
    return (
      <div className="flex flex-col gap-6">
        <div><h1 className="text-3xl font-bold tracking-tight">Génération de documents</h1></div>
        <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
          {[1, 2, 3, 4, 5].map((i) => <Skeleton key={i} className="h-44 rounded-xl" />)}
        </div>
      </div>
    );
  }

  return (
    <div className="flex flex-col gap-6" data-testid="coord-documents-page">
      <div>
        <h1 className="text-3xl font-bold tracking-tight">Génération de documents</h1>
        <p className="text-muted-foreground">
          Générez les documents PDF pour les soutenances.
        </p>
      </div>

      <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
        {DOC_TYPES.map((doc) => (
          <Card key={doc.id} className="flex flex-col" data-testid={`coord-documents-card-${doc.id}`}>
            <CardHeader>
              <div className="rounded-lg bg-primary/10 p-2.5 text-primary w-fit">
                <doc.icon />
              </div>
              <CardTitle className="mt-3 text-base">{doc.title}</CardTitle>
              <CardDescription>{doc.description}</CardDescription>
            </CardHeader>
            <CardContent className="mt-auto">
              <Button
                className="w-full"
                disabled={loadingPdf !== null}
                onClick={() => handleGenerate(doc)}
              >
                {loadingPdf?.startsWith(doc.id) ? (
                  <Loader2 data-icon="inline-start" className="animate-spin" />
                ) : null}
                {doc.actionLabel}
              </Button>
            </CardContent>
          </Card>
        ))}
      </div>

      <Dialog open={pickerDocId !== null} onOpenChange={(open) => { if (!open) setPickerDocId(null); }}>
        <DialogContent className="sm:max-w-md" data-testid="coord-documents-picker-dialog">
          <DialogHeader>
            <DialogTitle>{pickerTitle}</DialogTitle>
          </DialogHeader>
          <DialogDescription className="sr-only">
            Sélectionnez un projet pour générer le document.
          </DialogDescription>
          <div className="relative">
            <Search className="absolute left-3 top-1/2 -translate-y-1/2 text-muted-foreground" />
            <Input
              placeholder="Rechercher un projet..."
              value={pickerSearch}
              onChange={(e) => setPickerSearch(e.target.value)}
              className="pl-9"
              data-testid="coord-documents-picker-search"
            />
          </div>
          <div className="max-h-80 overflow-y-auto" data-testid="coord-documents-picker-list">
            {filteredPickerItems.length === 0 ? (
              pickerItems.length === 0 ? (
                <EmptyState description="Aucun élément disponible" />
              ) : (
                <EmptyState description="Aucun résultat" />
              )
            ) : (
              <div className="flex flex-col gap-1">
                {filteredPickerItems.map((item) => (
                  <Button
                    key={item.id}
                    variant="ghost"
                    className="w-full justify-start text-sm"
                    disabled={loadingPdf !== null}
                    onClick={() => handlePickerSelect(item.id)}
                    data-testid={`coord-documents-picker-item-${item.id}`}
                  >
                    {loadingPdf === `${pickerDocId}-${item.id}` ? (
                      <Loader2 data-icon="inline-start" className="animate-spin" />
                    ) : null}
                    {item.label}
                  </Button>
                ))}
              </div>
            )}
          </div>
          <DialogFooter>
            <Button variant="outline" onClick={() => setPickerDocId(null)} data-testid="coord-documents-picker-cancel">
              Annuler
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>

      <Dialog open={isDateDialogOpen} onOpenChange={setIsDateDialogOpen}>
        <DialogContent className="sm:max-w-sm" data-testid="coord-documents-date-dialog">
          <DialogHeader>
            <DialogTitle>Choisir une date</DialogTitle>
          </DialogHeader>
          <DialogDescription className="sr-only">
            Sélectionnez la date pour laquelle vous souhaitez générer la liste de présence.
          </DialogDescription>
          <Input type="date" value={dateInput} onChange={(e) => setDateInput(e.target.value)} data-testid="coord-documents-date-input" />
          <DialogFooter>
            <Button variant="outline" onClick={() => setIsDateDialogOpen(false)} data-testid="coord-documents-date-cancel">Annuler</Button>
            <Button
              onClick={handleAttendanceGenerate}
              disabled={loadingPdf !== null}
              data-testid="coord-documents-date-generate"
            >
              {loadingPdf?.startsWith("attendance-list-") && <Loader2 data-icon="inline-start" className="animate-spin" />}
              Générer
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </div>
  );
}
