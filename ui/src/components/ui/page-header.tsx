interface PageHeaderProps {
  title: string
  subtitle?: string
  actions?: React.ReactNode
}

export function PageHeader({ title, subtitle, actions }: PageHeaderProps) {
  return (
    <div className="flex items-center justify-between">
      <div className="relative pb-4">
        <h1 className="text-4xl font-bold tracking-tight">{title}</h1>
        <div className="absolute bottom-0 left-0 h-1 w-20 bg-primary rounded-full" />
        {subtitle && <p className="text-muted-foreground mt-2">{subtitle}</p>}
      </div>
      <div className="flex gap-2">{actions}</div>
    </div>
  )
}