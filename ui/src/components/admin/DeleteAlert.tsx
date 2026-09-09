import { Loader2 } from "lucide-react";
import { AlertDialog, AlertDialogAction, AlertDialogCancel, AlertDialogContent, AlertDialogDescription, AlertDialogFooter, AlertDialogHeader, AlertDialogTitle } from "@/components/ui/alert-dialog";

interface DeleteAlertProps {
  isOpen: boolean;
  onOpenChange: (open: boolean) => void;
  onDelete: () => void;
  title?: string;
  description?: string;
  entityName?: string;
  isPending: boolean;
}

export function DeleteAlert({
  isOpen,
  onOpenChange,
  onDelete,
  title = "Confirmation",
  description,
  entityName,
  isPending,
}: DeleteAlertProps) {
  return (
    <AlertDialog open={isOpen} onOpenChange={onOpenChange}>
      <AlertDialogContent data-testid="delete-alert">
        <AlertDialogHeader>
          <AlertDialogTitle>{title}</AlertDialogTitle>
          <AlertDialogDescription>
            {description ?? (
              <>
                Cette action est irréversible.
                {entityName ? (
                  <> L'élément "{entityName}" sera définitivement supprimé.</>
                ) : null}
              </>
            )}
          </AlertDialogDescription>
        </AlertDialogHeader>
        <AlertDialogFooter>
          <AlertDialogCancel>Annuler</AlertDialogCancel>
          <AlertDialogAction
            onClick={(e) => {
              e.preventDefault();
              onDelete();
            }}
            variant="destructive"
            disabled={isPending}
            data-testid="delete-alert-confirm"
          >
            {isPending && <Loader2 data-icon="inline-start" className="animate-spin" />}
            {isPending ? "Supprimer" : "Supprimer"}
          </AlertDialogAction>
        </AlertDialogFooter>
      </AlertDialogContent>
    </AlertDialog>
  );
}
