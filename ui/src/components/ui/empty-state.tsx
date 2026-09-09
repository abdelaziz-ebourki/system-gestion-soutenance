import type { LucideIcon } from "lucide-react";
import { cn } from "@/lib/utils";

interface EmptyStateProps {
  icon?: LucideIcon;
  title?: string;
  description: string;
  action?: React.ReactNode;
  variant?: "default" | "card" | "dashed";
  className?: string;
}

export function EmptyState({
  icon: Icon,
  title,
  description,
  action,
  variant = "default",
  className,
}: EmptyStateProps) {
  if (variant === "card") {
    return (
      <div className={cn("rounded-lg border bg-secondary p-4 text-sm text-secondary-foreground", className)}>
        {description}
      </div>
    );
  }

  if (variant === "dashed") {
    return (
      <div className={cn("rounded-lg border border-dashed p-5 text-sm text-muted-foreground", className)}>
        {description}
      </div>
    );
  }

  return (
    <div className={cn("flex flex-col items-center justify-center py-10 text-center", className)}>
      {Icon && (
        <div className="mb-4 rounded-full bg-muted p-3">
          <Icon className="size-6 text-muted-foreground" />
        </div>
      )}
      {title && <p className="text-base font-medium">{title}</p>}
      <p className="mt-1 text-sm text-muted-foreground">{description}</p>
      {action && <div className="mt-4">{action}</div>}
    </div>
  );
}
