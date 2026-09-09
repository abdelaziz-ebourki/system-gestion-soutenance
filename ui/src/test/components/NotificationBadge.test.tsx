import { screen } from "@testing-library/react";
import { describe, it, expect } from "vitest";
import { NotificationBadge } from "@/components/notification-badge";
import { renderWithProviders } from "@/test/utils";
import { server } from "@/test/mocks/server";
import { http, HttpResponse } from "msw";

describe("NotificationBadge", () => {
  it("renders bell button", () => {
    renderWithProviders(<NotificationBadge />);
    expect(screen.getByRole("button")).toBeInTheDocument();
  });

  it("shows unread count badge", async () => {
      server.use(
        http.get("*/api/notifications", () =>
          HttpResponse.json({
            items: [
              { id: 1, type: "info", title: "Test", message: "msg", timestamp: new Date().toISOString(), read: false },
            ],
            total: 1,
            pageCount: 1,
            currentPage: 0,
            size: 10,
          }),
        ),
      );
    renderWithProviders(<NotificationBadge />);
    expect(await screen.findByText("1")).toBeInTheDocument();
  });

  it("shows 9+ when count exceeds 9", async () => {
    const items = Array.from({ length: 10 }).map((_, i) => ({
      id: String(i + 1), type: "info", title: `N${i}`, message: `msg${i}`,
      timestamp: new Date().toISOString(), read: false,
    }));
      server.use(
        http.get("*/api/notifications", () => HttpResponse.json({
          items,
          total: items.length,
          pageCount: 1,
          currentPage: 0,
          size: 10,
        })),
      );
    renderWithProviders(<NotificationBadge />);
    expect(await screen.findByText("9+")).toBeInTheDocument();
  });
});
