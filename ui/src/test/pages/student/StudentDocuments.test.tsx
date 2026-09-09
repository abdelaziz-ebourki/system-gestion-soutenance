import { screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { describe, it, expect, vi, beforeEach } from "vitest";
import StudentDocuments from "@/pages/student/StudentDocuments";
import { renderWithProviders } from "@/test/utils";
import { server } from "@/test/mocks/server";
import { http, HttpResponse } from "msw";
import { toast } from "sonner";

vi.mock("sonner", () => ({
  toast: { success: vi.fn(), error: vi.fn(), warning: vi.fn() },
}));

const groupLeaderUser = {
  id: 1,
  email: "jean.dupont@example.com",
  firstName: "Jean",
  lastName: "Dupont",
  role: "student" as const,
  isActive: true,
};

const groupMemberUser = {
  id: 2,
  email: "sophie.martin@example.com",
  firstName: "Sophie",
  lastName: "Martin",
  role: "student" as const,
  isActive: true,
};

const soloUser = {
  id: 5,
  email: "solo@example.com",
  firstName: "Solo",
  lastName: "Student",
  role: "student" as const,
  isActive: true,
};

describe("StudentDocuments", () => {
  beforeEach(() => {
    vi.clearAllMocks();
    server.resetHandlers();
  });

  it("shows no-group message when student has no group", async () => {
    server.use(
      http.get("*/api/student/groups", () => HttpResponse.json({ currentGroup: null })),
    );
    renderWithProviders(<StudentDocuments />, {
      initialAuthState: { user: soloUser },
    });
    expect(await screen.findByText(/Vous devez faire partie d'un groupe/i)).toBeInTheDocument();
  });

  it("renders three document cards for group leader", async () => {
    renderWithProviders(<StudentDocuments />, {
      initialAuthState: { user: groupLeaderUser },
    });
    expect(await screen.findByTestId("group-document-card-REPORT")).toBeInTheDocument();
    expect(screen.getByTestId("group-document-card-PRESENTATION")).toBeInTheDocument();
    expect(screen.getByTestId("group-document-card-DIVERSE")).toBeInTheDocument();
  });

  it("renders stats cards", async () => {
    renderWithProviders(<StudentDocuments />, {
      initialAuthState: { user: groupLeaderUser },
    });
    expect(await screen.findByTestId("student-documents-stats-total")).toBeInTheDocument();
    expect(screen.getByTestId("student-documents-stats-validated")).toBeInTheDocument();
    expect(screen.getByTestId("student-documents-stats-missing")).toBeInTheDocument();
  });

  it("shows upload controls for missing documents when user is group leader", async () => {
    renderWithProviders(<StudentDocuments />, {
      initialAuthState: { user: groupLeaderUser },
    });
    expect(await screen.findByTestId("group-document-file-input-PRESENTATION")).toBeInTheDocument();
    expect(screen.getByTestId("group-document-file-input-DIVERSE")).toBeInTheDocument();
  });

  it("does not show upload controls for submitted documents", async () => {
    renderWithProviders(<StudentDocuments />, {
      initialAuthState: { user: groupLeaderUser },
    });
    expect(await screen.findByTestId("group-document-card-REPORT")).toBeInTheDocument();
    expect(screen.queryByTestId("group-document-file-input-REPORT")).not.toBeInTheDocument();
  });

  it("shows blocked message for non-leader group member", async () => {
    renderWithProviders(<StudentDocuments />, {
      initialAuthState: { user: groupMemberUser },
    });
    expect(await screen.findAllByText(/Seul le chef de groupe peut déposer/i)).toHaveLength(2);
  });

  it("uploads a document successfully as group leader", async () => {
    const user = userEvent.setup();
    renderWithProviders(<StudentDocuments />, {
      initialAuthState: { user: groupLeaderUser },
    });
    const fileInput = await screen.findByTestId("group-document-file-input-PRESENTATION");
    const file = new File(["dummy content"], "presentation.pdf", { type: "application/pdf" });
    await user.upload(fileInput, file);
    await user.click(screen.getByTestId("group-document-upload-btn-PRESENTATION"));
    await vi.waitFor(() => {
      expect(toast.success).toHaveBeenCalledWith("Document envoyé avec succès");
    });
  });

  it("shows error when uploading without selecting a file", async () => {
    const user = userEvent.setup();
    renderWithProviders(<StudentDocuments />, {
      initialAuthState: { user: groupLeaderUser },
    });
    const uploadBtn = await screen.findByTestId("group-document-upload-btn-PRESENTATION");
    await user.click(uploadBtn);
    expect(toast.error).toHaveBeenCalledWith("Veuillez sélectionner un fichier.");
  });
});
