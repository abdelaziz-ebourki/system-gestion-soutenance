import { screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { describe, it, expect, vi, beforeEach } from "vitest";
import { toast } from "sonner";
import Profile from "@/pages/Profile";
import { renderWithProviders } from "@/test/utils";

vi.mock("sonner", () => ({
  toast: { success: vi.fn(), error: vi.fn() },
}));

const mockUser = {
  id: 1,
  email: "admin@univh2c.ma",
  firstName: "Admin",
  lastName: "User",
  role: "admin" as const,
  isActive: true,
};

function renderProfile() {
  return renderWithProviders(<Profile />, {
    initialEntries: ["/profile"],
    initialAuthState: { user: mockUser },
  });
}

describe("Profile", () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it("renders loading skeleton when no user", () => {
    renderWithProviders(<Profile />);
    expect(screen.getByTestId("profile-skeleton")).toBeInTheDocument();
  });

  it("renders both profile cards when authenticated", async () => {
    renderProfile();
    expect(await screen.findByTestId("profile-header")).toBeInTheDocument();
    expect(screen.getByTestId("profile-edit-card")).toBeInTheDocument();
    expect(screen.getByTestId("password-card")).toBeInTheDocument();
  });

  it("pre-populates name fields from auth context", async () => {
    renderProfile();
    expect(await screen.findByDisplayValue("Admin")).toBeInTheDocument();
    expect(screen.getByDisplayValue("User")).toBeInTheDocument();
  });

  it("shows email and role as disabled fields", async () => {
    renderProfile();
    expect(await screen.findByDisplayValue("admin@univh2c.ma")).toBeInTheDocument();
    expect(screen.getByDisplayValue("Administrateur")).toBeInTheDocument();
  });

  it("saves profile edit successfully", async () => {
    const user = userEvent.setup();
    renderProfile();
    await screen.findByTestId("profile-field-firstName");

    const firstNameInput = screen.getByLabelText("Prénom");
    await user.clear(firstNameInput);
    await user.type(firstNameInput, "Jean");
    await user.click(screen.getByTestId("profile-save-btn"));

    await waitFor(() => {
      expect(toast.success).toHaveBeenCalledWith("Profil mis à jour avec succès");
    });
  });

  it("shows validation error for empty first name", async () => {
    const user = userEvent.setup();
    renderProfile();
    await screen.findByTestId("profile-field-firstName");

    const firstNameInput = screen.getByLabelText("Prénom");
    await user.clear(firstNameInput);
    await user.click(screen.getByTestId("profile-save-btn"));

    await waitFor(() => {
      expect(screen.getByText("Le prénom est requis")).toBeInTheDocument();
    });
  });

  it("validates password confirmation match", async () => {
    const user = userEvent.setup();
    renderProfile();
    await screen.findByTestId("password-card");

    await user.type(screen.getByLabelText("Mot de passe actuel"), "CurrentPass1!");
    await user.type(screen.getByLabelText("Nouveau mot de passe"), "NewPass123!");
    await user.type(screen.getByLabelText("Confirmer le mot de passe"), "DifferentPass1!");
    await user.click(screen.getByTestId("password-save-btn"));

    await waitFor(() => {
      expect(screen.getAllByText("Les mots de passe ne correspondent pas").length).toBeGreaterThan(0);
    });
  });

  it("validates minimum password length", async () => {
    const user = userEvent.setup();
    renderProfile();
    await screen.findByTestId("password-card");

    await user.type(screen.getByLabelText("Mot de passe actuel"), "CurrentPass1!");
    await user.type(screen.getByLabelText("Nouveau mot de passe"), "short");
    await user.type(screen.getByLabelText("Confirmer le mot de passe"), "short");
    await user.click(screen.getByTestId("password-save-btn"));

    await waitFor(() => {
      expect(screen.getAllByText("Le mot de passe doit contenir au moins 8 caractères").length).toBeGreaterThan(0);
    });
  });

  it("changes password successfully", async () => {
    const user = userEvent.setup();
    renderProfile();
    await screen.findByTestId("password-card");

    await user.type(screen.getByLabelText("Mot de passe actuel"), "CurrentPass1!");
    await user.type(screen.getByLabelText("Nouveau mot de passe"), "NewPass123!");
    await user.type(screen.getByLabelText("Confirmer le mot de passe"), "NewPass123!");
    await user.click(screen.getByTestId("password-save-btn"));

    await waitFor(() => {
      expect(toast.success).toHaveBeenCalledWith("Mot de passe modifié avec succès");
    });
  });

  it("shows error toast when current password is wrong", async () => {
    const user = userEvent.setup();
    renderProfile();
    await screen.findByTestId("password-card");

    await user.type(screen.getByLabelText("Mot de passe actuel"), "wrong");
    await user.type(screen.getByLabelText("Nouveau mot de passe"), "NewPass123!");
    await user.type(screen.getByLabelText("Confirmer le mot de passe"), "NewPass123!");
    await user.click(screen.getByTestId("password-save-btn"));

    await waitFor(() => {
      expect(toast.error).toHaveBeenCalled();
    });
  });
});
