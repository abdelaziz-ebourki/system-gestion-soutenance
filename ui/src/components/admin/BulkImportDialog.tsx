import { useState, useRef } from "react";
import * as XLSX from "xlsx";
import { toast } from "sonner";
import { cn, getErrorMessage } from "@/lib/utils";
import { Upload, FileUp, AlertCircle, LoaderCircle, Download, Loader2 } from "lucide-react";
import { Alert, AlertDescription } from "@/components/ui/alert";
import { Button } from "@/components/ui/button";
import { Dialog, DialogContent, DialogDescription, DialogFooter, DialogHeader, DialogTitle, DialogTrigger } from "@/components/ui/dialog";
import { bulkCreateUsers, bulkCreateRooms } from "@/lib/api";
import { bulkCreateProjects } from "@/lib/api-coordinator";

interface BulkImportDialogProps {
  entity: "student" | "teacher" | "coordinator" | "room" | "project";
  triggerButtonText?: string;
  onSuccess?: () => void;
}

const ENTITY_HEADERS: Record<string, string[]> = {
  student: ["prénom", "nom", "email", "cne", "major", "niveau"],
  teacher: ["prénom", "nom", "email", "département"],
  coordinator: ["prénom", "nom", "email"],
  room: ["nom", "département", "capacité"],
  project: ["titre", "description", "encadrant", "type"],
};

const ENTITY_SAMPLE_DATA: Record<string, Record<string, string>> = {
  student: { prénom: "Jean", nom: "Dupont", email: "jean@univh2c.ma", cne: "CNE001", major: "Génie Informatique", niveau: "L3" },
  teacher: { prénom: "Marie", nom: "Curie", email: "marie@univh2c.ma", département: "Informatique" },
  coordinator: { prénom: "Ahmed", nom: "Benani", email: "ahmed@univh2c.ma" },
  room: { nom: "Salle 101", département: "Informatique", capacité: "30" },
  project: { titre: "Application web de gestion", description: "Système de gestion...", encadrant: "prof@univh2c.ma", type: "pfe" },
};

export function BulkImportDialog({
  entity,
  triggerButtonText = "Importation en masse",
  onSuccess,
}: BulkImportDialogProps) {
  const [file, setFile] = useState<File | null>(null);
  const [data, setData] = useState<Record<string, string | number>[]>([]);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [isParsing, setIsParsing] = useState(false);
  const [isOpen, setIsOpen] = useState(false);
  const [isDragging, setIsDragging] = useState(false);
  const fileInputRef = useRef<HTMLInputElement>(null);

  const parseFile = async (selectedFile: File) => {
    const isExcel =
      selectedFile.type ===
        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet" ||
      selectedFile.type === "application/vnd.ms-excel";

    if (!isExcel) {
      toast.error(
        "Format de fichier non supporté. Veuillez utiliser un fichier Excel (.xlsx ou .xls).",
      );
      return;
    }

    setFile(selectedFile);
    setIsParsing(true);
    await new Promise((resolve) => setTimeout(resolve, 0));

    const reader = new FileReader();
    reader.onload = (evt) => {
      const bstr = evt.target?.result;
      const wb = XLSX.read(bstr, { type: "array" });
      const wsname = wb.SheetNames[0];
      const ws = wb.Sheets[wsname];
      const rawData = XLSX.utils.sheet_to_json(ws) as Record<string, string | number>[];

      if (rawData.length === 0) {
        toast.error("Le fichier semble être vide.");
        setFile(null);
        setData([]);
        setIsParsing(false);
        return;
      }

      const headers = Object.keys(rawData[0]).map((h) =>
        h.toLowerCase().trim(),
      );
      const expectedHeaders = ENTITY_HEADERS[entity];

      const missingHeaders = expectedHeaders.filter(
        (h) => !headers.some((header) => header.includes(h.toLowerCase())),
      );

      if (missingHeaders.length > 0) {
        toast.error(`Colonnes manquantes : ${missingHeaders.join(", ")}`);
        setFile(null);
        setData([]);
        setIsParsing(false);
        return;
      }

      const mappedData = rawData.map((item) => {
        const newItem: Record<string, string | number> = {};
        Object.keys(item).forEach((key) => {
          const normalizedKey = key.toLowerCase().trim();
          if (entity === "project") {
            if (normalizedKey.includes("titre")) newItem.title = item[key];
            else if (normalizedKey.includes("description")) newItem.description = item[key];
            else if (normalizedKey.includes("encadrant")) newItem.supervisorEmail = item[key];
            else if (normalizedKey.includes("type")) newItem.defenseType = item[key];
            else newItem[normalizedKey] = item[key];
          } else {
            if (normalizedKey.includes("prénom")) newItem.firstName = item[key];
            else if (normalizedKey.includes("nom")) {
              if (entity === "room") newItem.name = item[key];
              else newItem.lastName = item[key];
            } else if (normalizedKey.includes("email")) newItem.email = item[key];
            else if (normalizedKey.includes("cne")) newItem.cne = item[key];
            else if (normalizedKey.includes("major"))
              newItem.majorName = item[key];
            else if (normalizedKey.includes("niveau"))
              newItem.levelName = item[key];
            else if (normalizedKey.includes("département")) {
              if (entity === "room") newItem.departmentId = item[key];
              else newItem.departmentName = item[key];
            }
            else if (normalizedKey.includes("capacité"))
              newItem.capacity = item[key];
            else newItem[normalizedKey] = item[key];
          }
        });
        return newItem;
      });

      setData(mappedData);
      setIsParsing(false);
    };
    reader.readAsArrayBuffer(selectedFile);
  };

  const handleDragOver = (e: React.DragEvent) => {
    e.preventDefault();
    setIsDragging(true);
  };

  const handleDragLeave = (e: React.DragEvent) => {
    e.preventDefault();
    setIsDragging(false);
  };

  const handleDrop = (e: React.DragEvent) => {
    e.preventDefault();
    setIsDragging(false);
    const droppedFile = e.dataTransfer.files?.[0];
    if (droppedFile) parseFile(droppedFile);
  };

  const ENTITY_LABELS: Record<string, string> = {
    student: "Étudiants",
    teacher: "Enseignants",
    coordinator: "Coordinateurs",
    room: "Salles",
    project: "Projets",
  };

  const downloadTemplate = () => {
    const headers = ENTITY_HEADERS[entity];
    const sample = ENTITY_SAMPLE_DATA[entity];
    const ws = XLSX.utils.json_to_sheet([sample], { header: headers });
    const wb = XLSX.utils.book_new();
    XLSX.utils.book_append_sheet(wb, ws, "Modèle");
    XLSX.writeFile(wb, `modele_import_${entity}.xlsx`);
  };

  const handleSubmit = async () => {
    setIsSubmitting(true);
    try {
      if (entity === "room") {
        await bulkCreateRooms(data as Array<{ name: string; capacity: number; departmentId: number }>);
      } else if (entity === "project") {
        await bulkCreateProjects(data as Array<{ title: string; description: string; supervisorEmail: string; defenseType: string }>);
      } else {
        await bulkCreateUsers(data as Array<{ lastName: string; firstName: string; email: string; cne?: string; majorName?: string; levelName?: string; teacherRankName?: string; departmentName?: string }>, entity);
      }
      toast.success(
        `${data.length} ${ENTITY_LABELS[entity].toLowerCase()} importés avec succès.`,
      );
      setIsOpen(false);
      setFile(null);
      setData([]);
      onSuccess?.();
    } catch (error) {
      toast.error(getErrorMessage(error, "Échec de l'importation."));
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <Dialog open={isOpen} onOpenChange={setIsOpen}>
      <DialogTrigger asChild>
        <Button variant="outline" data-testid="bulk-import-trigger">
          <FileUp data-icon="inline-start" />
          {triggerButtonText}
        </Button>
      </DialogTrigger>
      <DialogContent className="sm:max-w-125" data-testid="bulk-import-dialog">
        <DialogHeader>
          <DialogTitle>Importation en masse : {entity}</DialogTitle>
          <DialogDescription>
            Téléchargez un fichier Excel pour importer plusieurs {entity}s à la
            fois.
          </DialogDescription>
        </DialogHeader>

        <Alert className="mb-4">
          <AlertCircle />
          <AlertDescription>
            Le fichier Excel doit contenir les colonnes suivantes :
            <span className="font-semibold block mt-1">
              {ENTITY_HEADERS[entity]?.join(", ")}
            </span>
          </AlertDescription>
        </Alert>

        <Button variant="ghost" size="sm" onClick={downloadTemplate} className="self-start mb-2" data-testid="download-template">
          <Download data-icon="inline-start" />
          Télécharger le modèle
        </Button>

        <div className="grid gap-4">
          <div
            className={cn("flex flex-col items-center justify-center w-full h-32 border-2 border-dashed rounded-lg cursor-pointer transition-colors", isParsing ? "border-muted-foreground/30 bg-muted" : isDragging ? "border-primary bg-primary/5" : "border-border bg-muted hover:bg-accent")}
            onDragOver={isParsing ? undefined : handleDragOver}
            onDragLeave={isParsing ? undefined : handleDragLeave}
            onDrop={isParsing ? undefined : handleDrop}
            onClick={isParsing ? undefined : () => fileInputRef.current?.click()}
          >
            <div className="flex flex-col items-center justify-center pt-5 pb-6">
              {isParsing ? (
                <LoaderCircle className="size-8 mb-2 text-muted-foreground animate-spin" />
              ) : (
                <Upload className="size-8 mb-2 text-muted-foreground" />
              )}
              <p className="text-sm text-muted-foreground">
                {isParsing ? "Analyse du fichier en cours..." : file ? file.name : "Glissez-déposez ou cliquez pour télécharger le fichier"}
              </p>
            </div>
            <input
              ref={fileInputRef}
              type="file"
              className="hidden"
              accept=".xlsx, .xls"
              disabled={isParsing}
              onChange={(e) =>
                e.target.files?.[0] && parseFile(e.target.files[0])
              }
            />
          </div>

          {data.length > 0 && (
            <div className="text-sm text-muted-foreground p-2 bg-muted rounded">
              {data.length} enregistrements trouvés.
            </div>
          )}
        </div>
        <DialogFooter>
          <Button
            onClick={handleSubmit}
            disabled={!file || isSubmitting}
          >
            {isSubmitting ? <><Loader2 data-icon="inline-start" className="animate-spin" /> Importation...</> : "Importer"}
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}
