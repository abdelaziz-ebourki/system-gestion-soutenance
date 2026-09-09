import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import * as api from "@/lib/api";
import { NOTIFICATION_POLL_INTERVAL } from "@/lib/constants";

export function useNotifications() {
  return useQuery({
    queryKey: ["notifications"],
    queryFn: api.getNotifications,
    refetchInterval: NOTIFICATION_POLL_INTERVAL,
  });
}

export function useUnreadCount() {
  const { data } = useNotifications();
    return data?.items?.filter((n) => !n.read).length ?? 0;
}

export function useMarkNotificationRead() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (id: number) => api.markNotificationRead(id),
    onSuccess: () => qc.invalidateQueries({ queryKey: ["notifications"] }),
  });
}

export function useMarkAllNotificationsRead() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: () => api.markAllNotificationsRead(),
    onSuccess: () => qc.invalidateQueries({ queryKey: ["notifications"] }),
  });
}
