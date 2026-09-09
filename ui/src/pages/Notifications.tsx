import { useMemo, useState } from "react";
import { BellIcon, CheckCheck, Info, AlertTriangle, CheckCircle, XCircle, Loader2 } from "lucide-react";
import { useNavigate } from "react-router-dom";
import { format, parseISO } from "date-fns";
import { fr } from "date-fns/locale";

import { useNotifications, useMarkNotificationRead, useMarkAllNotificationsRead } from "@/hooks/queries";
import type { AppNotification } from "@/types";
import { toast } from "sonner";
import { getErrorMessage } from "@/lib/utils";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Card, CardContent } from "@/components/ui/card";
import { EmptyState } from "@/components/ui/empty-state";
import { Skeleton } from "@/components/ui/skeleton";
import { cn } from "@/lib/utils";

const typeIcons: Record<string, typeof Info> = {
  info: Info,
  warning: AlertTriangle,
  success: CheckCircle,
  error: XCircle,
};

const typeColors: Record<string, string> = {
  info: "bg-primary/10 text-primary",
  warning: "bg-primary/10 text-primary",
  success: "bg-primary/10 text-primary",
  error: "bg-destructive/10 text-destructive",
};

const formatTimestamp = (ts: string) => {
  try {
    return format(parseISO(ts), "dd MMM yyyy 'à' HH:mm", { locale: fr });
  } catch {
    return ts;
  }
};

export default function NotificationsPage() {
  const { data: notificationsData, isLoading } = useNotifications();
  const notifications = useMemo(() => notificationsData?.items ?? [], [notificationsData]);
  const markReadMutation = useMarkNotificationRead();
  const markAllReadMutation = useMarkAllNotificationsRead();
  const navigate = useNavigate();
  const [readingId, setReadingId] = useState<number | null>(null);
 
  const unreadCount = useMemo(
    () => notifications.filter((n) => !n.read).length,
    [notifications],
  );

  const handleMarkRead = async (id: number) => {
    setReadingId(id);
    try {
      await markReadMutation.mutateAsync(id);
    } catch (error) {
      toast.error(getErrorMessage(error, "Erreur lors du marquage de la notification"));
    } finally {
      setReadingId(null);
    }
  };

  const handleMarkAllRead = async () => {
    await markAllReadMutation.mutateAsync();
  };

  const handleAction = (notification: AppNotification) => {
    if (notification.actionLink) {
      navigate(notification.actionLink);
    }
  };

  return (
    <div className="flex flex-col gap-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-3xl font-bold tracking-tight" data-testid="notifications-header">Notifications</h1>
          <p className="text-muted-foreground" data-testid="notifications-subtitle">
            {unreadCount > 0
              ? `${unreadCount} notification${unreadCount > 1 ? "s" : ""} non lue${unreadCount > 1 ? "s" : ""}`
              : "Toutes les notifications sont lues"}
          </p>
        </div>
        {unreadCount > 0 && (
          <Button
            variant="outline"
            size="sm"
            onClick={handleMarkAllRead}
            disabled={markAllReadMutation.isPending}
            data-testid="notifications-mark-all-read"
          >
            {markAllReadMutation.isPending && <Loader2 data-icon="inline-start" className="animate-spin" />}
            {markAllReadMutation.isPending ? "Tout marquer comme lu" : <><CheckCheck data-icon="inline-start" /> Tout marquer comme lu</>}
          </Button>
        )}
      </div>

      <Card data-testid="notifications-card">
        {isLoading ? (
          <CardContent>
            <div className="flex flex-col gap-3">
              {[1, 2, 3].map((i) => (
                <div key={i} className="flex items-start gap-4">
                  <Skeleton className="size-10 rounded-full" />
                  <div className="flex-1 flex flex-col gap-2">
                    <Skeleton className="h-4 w-48" />
                    <Skeleton className="h-3 w-full" />
                  </div>
                </div>
              ))}
            </div>
          </CardContent>
        ) : notifications.length === 0 ? (
          <CardContent>
            <EmptyState icon={BellIcon} title="Aucune notification" description="Les notifications apparaîtront ici une fois que vous recevrez des mises à jour." />
          </CardContent>
        ) : (
          <div className="divide-y">
            {notifications.map((notification) => {
              const Icon = typeIcons[notification.type] || Info;
              return (
                <div
                  key={notification.id}
                  className={cn(
                    "flex items-start gap-4 px-6 py-4 transition hover:bg-muted/30",
                    !notification.read && "bg-muted/20",
                  )}
                  data-testid={`notifications-item-${notification.id}`}
                >
                  <div className={cn("rounded-full p-2", typeColors[notification.type])}>
                    <Icon />
                  </div>
                  <div className="flex-1 flex flex-col gap-1">
                    <div className="flex items-center gap-2">
                      <p className="font-medium">{notification.title}</p>
                      {!notification.read && (
                        <Badge variant="default" className="size-2 rounded-full p-0" />
                      )}
                    </div>
                    <p className="text-sm text-muted-foreground">{notification.message}</p>
                    <div className="flex items-center gap-3 text-xs text-muted-foreground">
                      <span>{formatTimestamp(notification.timestamp)}</span>
                      {notification.actor && <span>• {notification.actor}</span>}
                    </div>
                  </div>
                  <div className="flex shrink-0 gap-1">
                    {notification.actionLink && (
                      <Button variant="ghost" size="sm" onClick={() => handleAction(notification)} data-testid={`notifications-action-${notification.id}`}>
                        Voir
                      </Button>
                    )}
                    {!notification.read && (
                      <Button
                        variant="ghost"
                        size="icon"
                        className="size-8"
                        onClick={() => handleMarkRead(notification.id)}
                        disabled={readingId === notification.id}
                        data-testid={`notifications-mark-read-${notification.id}`}
                      >
                        {readingId === notification.id ? <Loader2 data-icon="inline-start" className="animate-spin" /> : <CheckCheck />}
                      </Button>
                    )}
                  </div>
                </div>
              );
            })}
          </div>
        )}
      </Card>
    </div>
  );
}

