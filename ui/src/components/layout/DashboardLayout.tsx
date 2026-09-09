import { Fragment } from "react";
import {
  SidebarInset,
  SidebarProvider,
  SidebarTrigger,
} from "@/components/ui/sidebar";
import { AppSidebar } from "@/components/app-sidebar";
import { Breadcrumb, BreadcrumbItem, BreadcrumbLink, BreadcrumbList, BreadcrumbPage, BreadcrumbSeparator } from "@/components/ui/breadcrumb";
import { Separator } from "@/components/ui/separator";
import { Outlet, useLocation, Link } from "react-router-dom";
import { VALID_DASHBOARD_ROUTES, BREADCRUMB_LABELS } from "@/config/routes";

export default function DashboardLayout() {
  const location = useLocation();
  const segments = location.pathname.split("/").filter(Boolean);
  const breadcrumbs = segments.map((segment, index) => {
    const path = "/" + segments.slice(0, index + 1).join("/");
    const label = BREADCRUMB_LABELS[path] || segment;
    const isLast = index === segments.length - 1;
    const isLinkable = VALID_DASHBOARD_ROUTES.has(path as (typeof VALID_DASHBOARD_ROUTES) extends Set<infer T> ? T : never);
    return { label, path, isLast, isLinkable };
  });

  return (
    <SidebarProvider>
      <AppSidebar />
      <SidebarInset>
        <header className="flex h-16 shrink-0 items-center gap-2 transition-[width,height] ease-linear group-has-data-[collapsible=icon]/sidebar-wrapper:h-12 border-b px-4" data-testid="dashboard-layout-header">
          <div className="flex items-center gap-2">
            <SidebarTrigger className="-ml-1" data-testid="dashboard-layout-sidebar-trigger" />
            <Separator orientation="vertical" className="mr-2" />
            <Breadcrumb>
              <BreadcrumbList data-testid="dashboard-layout-breadcrumb">
                {breadcrumbs.map((crumb, index) => (
                  <Fragment key={crumb.path}>
                    {index > 0 && <BreadcrumbSeparator />}
                    <BreadcrumbItem data-testid={`dashboard-layout-breadcrumb-item-${index}`}>
                      {crumb.isLast ? (
                        <BreadcrumbPage className="font-heading font-bold uppercase tracking-widest text-xs text-muted-foreground">
                          {crumb.label}
                        </BreadcrumbPage>
                      ) : crumb.isLinkable ? (
                        <BreadcrumbLink asChild>
                          <Link to={crumb.path}>{crumb.label}</Link>
                        </BreadcrumbLink>
                      ) : (
                        <span className="text-muted-foreground">{crumb.label}</span>
                      )}
                    </BreadcrumbItem>
                  </Fragment>
                ))}
              </BreadcrumbList>
            </Breadcrumb>
          </div>
        </header>
        <main className="flex-1 overflow-auto p-6">
          <div className="mx-auto max-w-7xl h-full">
            <Outlet />
          </div>
        </main>
      </SidebarInset>
    </SidebarProvider>
  );
}
