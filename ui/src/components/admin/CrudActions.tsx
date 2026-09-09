import { MoreHorizontal, Pencil, Trash2 } from "lucide-react";
import { Button } from "@/components/ui/button";
import { DropdownMenu, DropdownMenuContent, DropdownMenuGroup, DropdownMenuItem, DropdownMenuLabel, DropdownMenuTrigger } from "@/components/ui/dropdown-menu";

interface CrudActionsProps<TEntity> {
  entity: TEntity;
  onEdit: (entity: TEntity) => void;
  onDelete: (entity: TEntity) => void;
}

export function CrudActions<TEntity>({
  entity,
  onEdit,
  onDelete,
}: CrudActionsProps<TEntity>) {
  return (
    <DropdownMenu>
        <DropdownMenuTrigger asChild>
          <Button variant="ghost" className="size-8 p-0" data-testid="crud-actions-trigger">
          <span className="sr-only">Ouvrir le menu</span>
          <MoreHorizontal />
        </Button>
      </DropdownMenuTrigger>
      <DropdownMenuContent align="end">
        <DropdownMenuGroup>
          <DropdownMenuLabel>Actions</DropdownMenuLabel>
          <DropdownMenuItem onClick={() => onEdit(entity)} data-testid="crud-actions-edit">
            <Pencil data-icon="inline-start" /> Modifier
          </DropdownMenuItem>
          <DropdownMenuItem
            variant="destructive"
            onClick={() => onDelete(entity)}
            data-testid="crud-actions-delete"
          >
            <Trash2 data-icon="inline-start" /> Supprimer
          </DropdownMenuItem>
        </DropdownMenuGroup>
      </DropdownMenuContent>
    </DropdownMenu>
  );
}
