import * as React from "react"

import { cn } from "@/lib/utils"
import { useFieldId } from "@/components/ui/field"

const Input = React.forwardRef<HTMLInputElement, React.ComponentProps<"input"> & { error?: string }>(
  ({ className, type, error, id, ...props }, ref) => {
    const fieldId = useFieldId()
    return (
      <input
        ref={ref}
        id={id ?? fieldId}
        type={type}
        data-slot="input"
        data-invalid={!!error}
        className={cn(
          "h-9 w-full min-w-0 rounded-3xl border border-transparent bg-input/50 px-3 py-1 text-base transition-[color,box-shadow,background-color] outline-none file:inline-flex file:h-7 file:border-0 file:bg-transparent file:text-sm file:font-medium file:text-foreground placeholder:text-muted-foreground focus-visible:border-ring focus-visible:ring-3 focus-visible:ring-ring/30 disabled:pointer-events-none disabled:cursor-not-allowed disabled:opacity-50 aria-invalid:border-destructive aria-invalid:ring-3 aria-invalid:ring-destructive/20 md:text-sm dark:aria-invalid:border-destructive/50 dark:aria-invalid:ring-destructive/40",
          error && "border-destructive ring-3 ring-destructive/20 dark:ring-destructive/40",
          className
        )}
        aria-invalid={!!error}
        {...props}
      />
    )
  }
)

export { Input }
