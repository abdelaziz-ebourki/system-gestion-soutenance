import { useDefenseSchedule } from "@/hooks/use-defense-schedule";
import {
  DndContext,
  DragOverlay,
} from "@dnd-kit/core";
import {
  Wand2,
  Save,
  Send,
  Loader2,
} from "lucide-react";
import { AlertDialog, AlertDialogAction, AlertDialogCancel, AlertDialogContent, AlertDialogDescription, AlertDialogFooter, AlertDialogHeader, AlertDialogTitle } from "@/components/ui/alert-dialog";
import { Button } from "@/components/ui/button";
import { Select, SelectContent, SelectGroup, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import { Skeleton } from "@/components/ui/skeleton";
import JurySidebar from "@/components/coordinator/JurySidebar";
import DefenseCalendar from "@/components/coordinator/DefenseCalendar";
import DraggableJurySlot from "@/components/coordinator/DraggableJurySlot";

export default function DefenseDesigner() {
  const {
    sessions,
    juries,
    rooms,
    projects,
    allLoading,
    selectedSessionId,
    setSelectedSessionId,
    currentSession,
    days,
    timeSlots,
    defenseDuration,
    searchQuery,
    setSearchQuery,
    filteredJuries,
    activeJuryId,
    schedule,
    handleDragStart,
    handleDragEnd,
    handleRemove,
    handleSave,
    handleAutoGenerate,
    handlePublish,
    isPublishDialogOpen,
    setIsPublishDialogOpen,
    saveSchedule,
    transitionSession,
  } = useDefenseSchedule();

  if (allLoading) return <Skeleton className="h-[600px] w-full" />;

  if (!sessions?.length) {
    return (
      <div className="flex flex-col gap-6">
        <div>
          <h1 className="text-3xl font-bold tracking-tight">Planificateur de Soutenances</h1>
          <p className="text-muted-foreground">
            Aucune session de soutenance disponible. Créez d'abord une session dans
            {" "}<a href="/coordinator/defense-sessions" className="text-primary underline">Gestion des sessions</a>.
          </p>
        </div>
      </div>
    );
  }

  if (!currentSession) return <Skeleton className="h-[600px] w-full" />;

  return (
    <div className="flex flex-col gap-6" data-testid="coord-designer-page">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-3xl font-bold tracking-tight">Planificateur de Soutenances</h1>
          <div className="flex items-center gap-2 mt-2">
            <span className="text-sm text-muted-foreground">Session:</span>
            <Select
              value={selectedSessionId != null ? String(selectedSessionId) : undefined}
              onValueChange={(v) => setSelectedSessionId(Number(v))}
            >
              <SelectTrigger className="w-72" data-testid="coord-designer-session-select">
                <SelectValue placeholder="Sélectionner une session" />
              </SelectTrigger>
              <SelectContent>
                <SelectGroup>
                  {sessions.map((s) => (
                    <SelectItem key={s.id} value={String(s.id)}>
                      {s.name}
                    </SelectItem>
                  ))}
                </SelectGroup>
              </SelectContent>
            </Select>
          </div>
        </div>
        <div className="flex gap-3">
          <Button variant="outline" className="gap-2" onClick={handleAutoGenerate} disabled={saveSchedule.isPending} data-testid="coord-designer-auto-generate">
            {saveSchedule.isPending && <Loader2 data-icon="inline-start" className="animate-spin" />}
            {saveSchedule.isPending ? "Génération Auto" : <><Wand2 data-icon="inline-start" /> Génération Auto</>}
          </Button>
          <Button className="gap-2" onClick={handleSave} disabled={saveSchedule.isPending} data-testid="coord-designer-save">
            {saveSchedule.isPending && <Loader2 data-icon="inline-start" className="animate-spin" />}
            {saveSchedule.isPending ? "Enregistrer" : <><Save data-icon="inline-start" /> Enregistrer</>}
          </Button>
          <Button variant="default" className="gap-2"
            onClick={() => setIsPublishDialogOpen(true)} data-testid="coord-designer-publish">
            <Send data-icon="inline-start" /> Publier
          </Button>
        </div>
      </div>

      <DndContext onDragStart={handleDragStart} onDragEnd={handleDragEnd}>
        <div className="grid grid-cols-12 gap-6 min-h-[calc(100vh-12rem)]">
          <JurySidebar
            juries={filteredJuries}
            searchQuery={searchQuery}
            onSearchChange={setSearchQuery}
          />
           <div className="col-span-9 min-w-0 h-full flex flex-col" data-testid="coord-designer-calendar">
            <DefenseCalendar
              days={days}
              timeSlots={timeSlots}
              schedule={schedule}
              juries={juries}
              onRemove={handleRemove}
              rooms={rooms}
              defenseDuration={defenseDuration}
              projects={projects}
            />
          </div>
        </div>

        <DragOverlay>
          {(() => {
            const activeJury = juries.find((j) => j.id === activeJuryId);
            return activeJury ? (
              <div className="w-64">
                <DraggableJurySlot jury={activeJury} isOverlay />
              </div>
            ) : null;
          })()}
        </DragOverlay>
      </DndContext>

      <AlertDialog open={isPublishDialogOpen} onOpenChange={setIsPublishDialogOpen}>
        <AlertDialogContent data-testid="coord-designer-publish-dialog">
          <AlertDialogHeader>
            <AlertDialogTitle>Publier le planning</AlertDialogTitle>
            <AlertDialogDescription>
              Cette action rendra le planning visible pour les étudiants et les enseignants.
              Assurez-vous d'avoir enregistré vos modifications avant de publier.
            </AlertDialogDescription>
          </AlertDialogHeader>
          <AlertDialogFooter>
            <AlertDialogCancel data-testid="coord-designer-publish-cancel">Annuler</AlertDialogCancel>
            <AlertDialogAction onClick={handlePublish} disabled={transitionSession.isPending} data-testid="coord-designer-publish-confirm">
              {transitionSession.isPending && <Loader2 data-icon="inline-start" className="animate-spin" />}
              {transitionSession.isPending ? "Publier" : "Publier"}
            </AlertDialogAction>
          </AlertDialogFooter>
        </AlertDialogContent>
      </AlertDialog>
    </div>
  );
}
