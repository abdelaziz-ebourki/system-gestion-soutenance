import { Search, Users } from "lucide-react";
import { Badge } from "@/components/ui/badge";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { EmptyState } from "@/components/ui/empty-state";
import { Input } from "@/components/ui/input";
import DraggableJurySlot from "@/components/coordinator/DraggableJurySlot";
import type { Jury } from "@/types";

interface JurySidebarProps {
  juries: Jury[];
  searchQuery: string;
  onSearchChange: (q: string) => void;
}

export default function JurySidebar({
  juries,
  searchQuery,
  onSearchChange,
}: JurySidebarProps) {
  return (
    <Card className="col-span-3     h-[calc(100vh-10.4rem)] flex flex-col" data-testid="coord-designer-jury-sidebar">
      <CardHeader className="pb-3">
<CardTitle className="text-lg flex items-center gap-2">
  <Users data-icon="inline-start" /> À positionner
  <Badge variant="secondary" className="ml-auto" data-testid="coord-designer-jury-count">{juries.length}</Badge>
</CardTitle>
        <div className="relative mt-2">
          <Search className="absolute left-2 top-2.5 text-muted-foreground" />
          <Input
            placeholder="Rechercher un jury..."
            className="pl-8"
            value={searchQuery}
            onChange={(e) => onSearchChange(e.target.value)}
            data-testid="coord-designer-jury-search"
          />
        </div>
      </CardHeader>
       <CardContent className="flex-1 overflow-y-auto flex flex-col gap-3 pt-0" data-testid="coord-designer-jury-list">
         {juries?.map((jury) => (
           <DraggableJurySlot key={jury.id} jury={jury} />
         ))}
         {juries?.length === 0 && (
           <EmptyState description="Aucun jury en attente" />
         )}
       </CardContent>
    </Card>
  );
}
