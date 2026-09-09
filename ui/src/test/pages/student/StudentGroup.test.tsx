import { screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { describe, it, expect, vi, beforeEach } from "vitest";
import StudentGroup from "@/pages/student/StudentGroup";
import { renderWithProviders } from "@/test/utils";
import { server } from "@/test/mocks/server";
import { http, HttpResponse } from "msw";
import { toast } from "sonner";

vi.mock("sonner", () => ({
  toast: { success: vi.fn(), error: vi.fn() },
}));

const studentUser = {
  id: 4,
  email: "student@univh2c.ma",
  firstName: "Student",
  lastName: "User",
  role: "student" as const,
  isActive: true,
};

describe("StudentGroup", () => {
  beforeEach(() => {
    vi.clearAllMocks();
    server.resetHandlers();
  });

  it("renders group details when user has a group", async () => {
    renderWithProviders(<StudentGroup />, {
      initialAuthState: { user: studentUser },
    });
    expect(await screen.findByText("Groupe Alpha")).toBeInTheDocument();
    expect(screen.getByText("Jean Dupont")).toBeInTheDocument();
    expect(screen.getByText("Sophie Martin")).toBeInTheDocument();
    expect(screen.getByText("Responsable de groupe")).toBeInTheDocument();
  });

  it("shows create button when group creation is open and user has no group", async () => {
    server.use(
      http.get("*/api/student/groups", () =>
        HttpResponse.json({
          currentGroup: null,
          availableGroups: [],
          groupCreationStartDate: "2026-01-01",
          groupCreationEndDate: "2026-06-01",
          isGroupCreationOpen: true,
        }),
      ),
    );
    renderWithProviders(<StudentGroup />, {
      initialAuthState: { user: studentUser },
    });
    expect(await screen.findByTestId("student-group-create-btn")).toBeInTheDocument();
    expect(screen.getByText("Vous n'appartenez à aucun groupe pour le moment.")).toBeInTheDocument();
  });

  it("disables create button when user already has a group", async () => {
    renderWithProviders(<StudentGroup />, {
      initialAuthState: { user: studentUser },
    });
    const createBtn = await screen.findByTestId("student-group-create-btn");
    expect(createBtn).toBeDisabled();
  });

  it("shows closure message when group creation is closed", async () => {
    server.use(
      http.get("*/api/student/groups", () =>
        HttpResponse.json({
          currentGroup: null,
          availableGroups: [],
          groupCreationStartDate: "2026-01-01",
          groupCreationEndDate: "2026-02-01",
          isGroupCreationOpen: false,
        }),
      ),
    );
    renderWithProviders(<StudentGroup />, {
      initialAuthState: { user: studentUser },
    });
    expect(await screen.findByText(/La création et la jonction des groupes sont actuellement fermées/)).toBeInTheDocument();
  });

  it("shows available groups to join", async () => {
    server.use(
      http.get("*/api/student/groups", () =>
        HttpResponse.json({
          currentGroup: null,
          availableGroups: [
            { id: 2, groupName: "Groupe Beta", memberCount: 2 },
          ],
          groupCreationStartDate: "2026-01-01",
          groupCreationEndDate: "2026-06-01",
          isGroupCreationOpen: true,
        }),
      ),
    );
    renderWithProviders(<StudentGroup />, {
      initialAuthState: { user: studentUser },
    });
    expect(await screen.findByText("Groupe Beta")).toBeInTheDocument();
    expect(screen.getByTestId("student-group-join-btn-2")).toBeInTheDocument();
  });

  it("creates a group successfully", async () => {
    const user = userEvent.setup();
    server.use(
      http.get("*/api/student/groups", () =>
        HttpResponse.json({
          currentGroup: null,
          availableGroups: [],
          groupCreationStartDate: "2026-01-01",
          groupCreationEndDate: "2026-06-01",
          isGroupCreationOpen: true,
        }),
      ),
    );
    renderWithProviders(<StudentGroup />, {
      initialAuthState: { user: studentUser },
    });
    const createBtn = await screen.findByTestId("student-group-create-btn");
    await user.click(createBtn);
    await vi.waitFor(() => {
      expect(toast.success).toHaveBeenCalledWith("Votre groupe a été créé automatiquement");
    });
  });
});
