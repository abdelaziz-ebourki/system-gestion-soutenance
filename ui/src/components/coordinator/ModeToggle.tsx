import { GripVertical, MousePointerClick } from "lucide-react";

import { ToggleGroup, ToggleGroupItem } from "@/components/ui/toggle-group";

export type ScheduleMode = "click" | "dnd";

interface ModeToggleProps {
  value: ScheduleMode;
  onChange: (value: ScheduleMode) => void;
}

export function ModeToggle({ value, onChange }: ModeToggleProps) {
  return (
    <ToggleGroup
      type="single"
      value={value}
      onValueChange={(val) => {
        if (val) onChange(val as ScheduleMode);
      }}
      variant="outline"
      spacing={0}
      size="sm"
      data-testid="coord-toggle-mode"
    >
      <ToggleGroupItem value="click" className="gap-2" data-testid="coord-toggle-click">
        <MousePointerClick data-icon="inline-start" />
        Placement rapide
      </ToggleGroupItem>
      <ToggleGroupItem value="dnd" className="gap-2" data-testid="coord-toggle-dnd">
        <GripVertical data-icon="inline-start" />
        Glisser-déposer
      </ToggleGroupItem>
    </ToggleGroup>
  );
}
