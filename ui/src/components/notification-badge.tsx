import { BellIcon } from "lucide-react";
import { useUnreadCount } from "@/hooks/queries";
import { Button } from "@/components/ui/button";
import { useNavigate } from "react-router-dom";

export function NotificationBadge() {
  const unreadCount = useUnreadCount();
  const navigate = useNavigate();

  return (
    <Button
      variant="ghost"
      size="icon"
      className="relative"
      onClick={() => navigate("/notifications")}
    >
      <BellIcon />
      {unreadCount > 0 && (
        <span className="absolute -right-1 -top-1 flex size-5 items-center justify-center rounded-full bg-destructive text-[10px] font-bold text-destructive-foreground">
          {unreadCount > 9 ? "9+" : unreadCount}
        </span>
      )}
    </Button>
  );
}

