import type { CSSProperties } from "react";
import { useDraggable } from "@dnd-kit/core";
import { Virtuoso } from "react-virtuoso";
import { Badge } from "@/components/ui/badge";
import type { Project } from "@/types";
import type { ScheduleMode } from "./ModeToggle";

interface DraggableProjectCardProps {
  project: Project;
  assigned: boolean;
  mode: ScheduleMode;
  isSelected: boolean;
  onSelect: (projectId: number | null) => void;
}

function DraggableProjectCard({
  project,
  assigned,
  mode,
  isSelected,
  onSelect,
}: DraggableProjectCardProps) {
  const isDndMode = mode === "dnd" && !assigned;
  const { attributes, listeners, setNodeRef, transform, isDragging } =
    useDraggable({
      id: project.id,
      data: { type: "project", project },
      disabled: !isDndMode,
    });

  const style: CSSProperties | undefined = transform
    ? { transform: `translate3d(${transform.x}px, ${transform.y}px, 0)`, opacity: isDragging ? 0 : undefined }
    : undefined;

  const handleClick = () => {
    if (mode === "click" && !assigned) {
      onSelect(isSelected ? null : project.id);
    }
  };

  return (
    <div
      ref={setNodeRef}
      style={style}
      {...(isDndMode ? { ...listeners, ...attributes } : {})}
      onClick={handleClick}
      className={
        assigned
          ? "rounded-lg border bg-muted p-4 opacity-50"
          : isSelected
            ? "cursor-pointer rounded-lg border-2 border-primary bg-primary/5 p-4 transition hover:-translate-y-0.5"
            : "cursor-pointer rounded-lg border bg-card p-4 transition hover:-translate-y-0.5"
      }
      data-testid={`coord-project-card-${project.id}`}
    >
      <div className="flex items-start justify-between gap-3">
        <div>
          <p className="font-medium" data-testid={`coord-project-title-${project.id}`}>{project.title}</p>
          <p className="mt-1 text-sm text-muted-foreground" data-testid={`coord-project-students-${project.id}`}>
            {project.studentNames?.join(", ") || "Groupe non renseigné"}
          </p>
        </div>
        <Badge variant="outline" data-testid={`coord-project-supervisor-${project.id}`}>{project.supervisorName}</Badge>
      </div>
    </div>
  );
}

interface ProjectListProps {
  projects: Project[];
  assignedProjectIds: Set<number>;
  mode: ScheduleMode;
  selectedProjectId: number | null;
  onSelect: (projectId: number | null) => void;
}

export function ProjectList({
  projects,
  assignedProjectIds,
  mode,
  selectedProjectId,
  onSelect,
}: ProjectListProps) {
  const itemContent = (_index: number, project: Project) => (
    <div className="py-1">
      <DraggableProjectCard
        project={project}
        assigned={assignedProjectIds.has(project.id)}
        mode={mode}
        isSelected={selectedProjectId === project.id}
        onSelect={onSelect}
      />
    </div>
  );

  if (projects && projects.length > 30) {
    return (
      <Virtuoso
        data={projects}
        itemContent={itemContent}
        className="h-[60vh]"
      />
    );
  }

   return (
     <div className="flex flex-col gap-2">
       {projects?.map((project) => (
         <DraggableProjectCard
           key={project.id}
           project={project}
           assigned={assignedProjectIds.has(project.id)}
           mode={mode}
           isSelected={selectedProjectId === project.id}
           onSelect={onSelect}
         />
       ))}
     </div>
   );
}
