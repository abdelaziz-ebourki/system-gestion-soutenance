import { Loader2 } from "lucide-react";
import { useState } from "react";
import { toast } from "sonner";
import { getErrorMessage } from "@/lib/utils";
import { Button } from "@/components/ui/button";
import { Dialog, DialogContent, DialogDescription, DialogFooter, DialogHeader, DialogTitle } from "@/components/ui/dialog";
import { Select, SelectContent, SelectGroup, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import { DeleteAlert } from "@/components/admin/DeleteAlert";

interface BatchAction {
  key: string;
  label: string;
}

interface BatchActionsBarProps {
  selectedCount: number;
  entityLabel: string;
  actions?: BatchAction[];
  fieldOptionsMap?: Record<string, { value: string; label: string }[]>;
  onUpdateField?: (field: string, value: string) => Promise<void>;
  onDeleteSelected?: () => Promise<void>;
  isPending?: boolean;
  onClearSelection?: () => void;
}

export function BatchActionsBar({
  selectedCount,
  entityLabel,
  actions = [],
  fieldOptionsMap = {},
  onUpdateField,
  onDeleteSelected,
  isPending = false,
  onClearSelection,
}: BatchActionsBarProps) {
  const [batchDialog, setBatchDialog] = useState<string | null>(null);
  const [batchValue, setBatchValue] = useState("");

  if (selectedCount === 0) return null;

  const handleUpdate = async (field: string) => {
    if (!batchValue || !onUpdateField) return;
    try {
      await onUpdateField(field, batchValue);
      toast.success(`${selectedCount} ${entityLabel} mis à jour`);
      setBatchDialog(null);
      setBatchValue("");
      onClearSelection?.();
    } catch (error) {
      toast.error(getErrorMessage(error, "Erreur lors de la mise à jour"));
    }
  };

  const currentAction = actions.find((a) => a.key === batchDialog);

  return (
    <>
      <div className="flex items-center justify-between fixed bottom-0 left-0 right-0 z-50 border-t bg-background p-4 shadow-lg" data-testid="batch-actions-bar">
        <span className="text-sm font-medium">
          {selectedCount} {entityLabel} sélectionné(s)
        </span>
        <div className="flex gap-2">
          {actions.map((action) => (
            <Button
              key={action.key}
              variant={action.key === "delete" ? "destructive" : "outline"}
              size="sm"
              onClick={() => { setBatchDialog(action.key); setBatchValue(""); }}
            >
              {action.label}
            </Button>
          ))}
        </div>
      </div>

      {currentAction && currentAction.key !== "delete" && (
        <Dialog open={batchDialog === currentAction.key} onOpenChange={(o) => { if (!o) setBatchDialog(null); }}>
          <DialogContent>
            <DialogHeader>
              <DialogTitle>{currentAction.label}</DialogTitle>
              <DialogDescription>
                {selectedCount} {entityLabel} sélectionné(s).
              </DialogDescription>
            </DialogHeader>
            <div className="py-4">
              <Select value={batchValue} onValueChange={(v) => setBatchValue(v ?? "")}>
                <SelectTrigger><SelectValue placeholder="Choisir..." /></SelectTrigger>
                <SelectContent>
                  <SelectGroup>
                    {(fieldOptionsMap[currentAction.key] ?? []).map((opt) => (
                      <SelectItem key={opt.value} value={opt.value}>{opt.label}</SelectItem>
                    ))}
                  </SelectGroup>
                </SelectContent>
              </Select>
            </div>
            <DialogFooter>
              <Button variant="outline" onClick={() => setBatchDialog(null)}>Annuler</Button>
              <Button onClick={() => handleUpdate(currentAction.key)} disabled={isPending}>
                {isPending ? <><Loader2 data-icon="inline-start" className="animate-spin" /> Enregistrement...</> : "Enregistrer"}
              </Button>
            </DialogFooter>
          </DialogContent>
        </Dialog>
      )}

      {onDeleteSelected && (
        <DeleteAlert
          isOpen={batchDialog === "delete"}
          onOpenChange={(o) => { if (!o) setBatchDialog(null); }}
          entityName={`${selectedCount} ${entityLabel}`}
          onDelete={async () => {
            try {
              await onDeleteSelected();
              toast.success(`${selectedCount} ${entityLabel} supprimé(s)`);
              setBatchDialog(null);
              onClearSelection?.();
            } catch (error) {
               toast.error(getErrorMessage(error, "Erreur lors de la suppression"));
            }
          }}
          isPending={isPending}
        />
      )}
    </>
  );
}
