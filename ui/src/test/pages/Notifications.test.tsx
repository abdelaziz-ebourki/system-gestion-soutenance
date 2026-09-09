import { screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { describe, it, expect, vi, beforeEach } from "vitest";
import NotificationsPage from "@/pages/Notifications";
import { renderWithProviders } from "@/test/utils";
import { server } from "@/test/mocks/server";
import { http, HttpResponse } from "msw";

const adminUser = {
  id: 1,
  email: "admin@univh2c.ma",
  firstName: "Admin",
  lastName: "User",
  role: "admin" as const,
  isActive: true,
};

describe("NotificationsPage", () => {
  beforeEach(() => {
    server.resetHandlers();
  });

  it("renders notifications list", async () => {
    renderWithProviders(<NotificationsPage />, {
      initialAuthState: { user: adminUser },
    });
    expect(await screen.findByText("Notification test")).toBeInTheDocument();
    expect(screen.getByText("Ceci est une notification de test")).toBeInTheDocument();
  });

  it("shows unread count", async () => {
    renderWithProviders(<NotificationsPage />, {
      initialAuthState: { user: adminUser },
    });
    expect(await screen.findByText("1 notification non lue")).toBeInTheDocument();
  });

  it("shows 'all read' message when no unread", async () => {
    server.use(
      http.get("*/api/notifications", () =>
        HttpResponse.json([
          {
            id: 1,
            type: "info",
            title: "Notification test",
            message: "Ceci est une notification de test",
            timestamp: new Date().toISOString(),
            read: true,
          },
        ]),
      ),
    );
    renderWithProviders(<NotificationsPage />, {
      initialAuthState: { user: adminUser },
    });
    expect(await screen.findByText("Toutes les notifications sont lues")).toBeInTheDocument();
  });

  it("shows empty state when no notifications", async () => {
    server.use(
      http.get("*/api/notifications", () => HttpResponse.json([])),
    );
    renderWithProviders(<NotificationsPage />, {
      initialAuthState: { user: adminUser },
    });
    expect(await screen.findByText("Aucune notification")).toBeInTheDocument();
  });

  it("marks a notification as read", async () => {
    const user = userEvent.setup();
    renderWithProviders(<NotificationsPage />, {
      initialAuthState: { user: adminUser },
    });
    expect(await screen.findByTestId("notifications-mark-read-1")).toBeInTheDocument();
    await user.click(screen.getByTestId("notifications-mark-read-1"));
  });

  it("shows mark all read button when unread exist", async () => {
    renderWithProviders(<NotificationsPage />, {
      initialAuthState: { user: adminUser },
    });
    expect(await screen.findByTestId("notifications-mark-all-read")).toBeInTheDocument();
  });

  it("hides mark all read when all are read", async () => {
    server.use(
      http.get("*/api/notifications", () =>
        HttpResponse.json([
          {
            id: 1,
            type: "info",
            title: "Notification test",
            message: "Test",
            timestamp: new Date().toISOString(),
            read: true,
          },
        ]),
      ),
    );
    renderWithProviders(<NotificationsPage />, {
      initialAuthState: { user: adminUser },
    });
    await vi.waitFor(() => {
      expect(screen.queryByTestId("notifications-mark-all-read")).not.toBeInTheDocument();
    });
  });
});
