import { SimpleSelect } from "@/components/ui/select";
import type { Room } from "@/types";

interface RoomSearchSelectProps {
  rooms: Room[];
  value: string | null;
  onChange: (roomId: string | null) => void;
}

export function RoomSearchSelect({ rooms, value, onChange }: RoomSearchSelectProps) {
  return (
     <SimpleSelect
       label="Salle"
       placeholder="Choisir une salle..."
       options={rooms?.map((r) => ({ value: String(r.id), label: `${r.name} (${r.capacity} places)` }))}
       value={value ?? undefined}
       onChange={(val) => {
         if (val) onChange(val);
       }}
       fullWidth
     />
  );
}
