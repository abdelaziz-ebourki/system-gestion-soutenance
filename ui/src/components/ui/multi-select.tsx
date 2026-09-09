import { cn } from "@/lib/utils";
import { Button } from "@/components/ui/button";
import {
  DropdownMenu,
  DropdownMenuTrigger,
  DropdownMenuContent,
  DropdownMenuCheckboxItem,
} from "@/components/ui/dropdown-menu";
import { ChevronDownIcon } from "lucide-react";

interface MultiSelectOption {
  value: string;
  label: string;
}

interface MultiSelectProps {
  options: MultiSelectOption[];
  value: string[];
  onChange: (value: string[]) => void;
  placeholder?: string;
  disabled?: boolean;
  className?: string;
}

function MultiSelect({
  options,
  value,
  onChange,
  placeholder = "Sélectionner...",
  disabled = false,
  className,
}: MultiSelectProps) {
  return (
    <DropdownMenu>
      <DropdownMenuTrigger disabled={disabled} asChild>
        <Button
          variant="outline"
          className={cn(
            "flex h-8 w-full items-center justify-between gap-1.5 rounded-lg border border-input bg-transparent px-2.5 py-1 text-sm transition-colors",
            value.length === 0 && "text-muted-foreground",
            className,
          )}
        >
          <span className="truncate">
            {value.length > 0
              ? `${value.length} sélectionné${value.length > 1 ? "s" : ""}`
              : placeholder}
          </span>
          <ChevronDownIcon className="size-4 shrink-0 text-muted-foreground" />
        </Button>
      </DropdownMenuTrigger>
      <DropdownMenuContent className="max-h-60 w-(--anchor-width) overflow-y-auto">
        {options.length > 0 ? (
          options.map((option) => (
            <DropdownMenuCheckboxItem
              key={option.value}
              checked={value.includes(option.value)}
              onCheckedChange={(checked) => {
                if (checked) {
                  onChange([...value, option.value]);
                } else {
                  onChange(value.filter((v) => v !== option.value));
                }
              }}
            >
              {option.label}
            </DropdownMenuCheckboxItem>
          ))
        ) : (
          <div className="px-2 py-4 text-center text-sm text-muted-foreground">
            Aucune option disponible
          </div>
        )}
      </DropdownMenuContent>
    </DropdownMenu>
  );
}

export { MultiSelect };
export type { MultiSelectOption, MultiSelectProps };
