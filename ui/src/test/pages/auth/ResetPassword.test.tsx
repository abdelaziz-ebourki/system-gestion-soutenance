import { screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { describe, it, expect, vi, beforeEach } from "vitest";
import ResetPassword from "@/pages/auth/ResetPassword";
import { renderWithProviders } from "@/test/utils";
import { server } from "@/test/mocks/server";
import { toast } from "sonner";

vi.mock("sonner", () => ({
  toast: { success: vi.fn(), error: vi.fn() },
}));

const mockNavigate = vi.fn();
vi.mock("react-router-dom", async (importOriginal) => {
  const actual = await importOriginal<typeof import("react-router-dom")>();
  return { ...actual, useNavigate: () => mockNavigate };
});

describe("ResetPassword", () => {
  beforeEach(() => {
    vi.clearAllMocks();
    server.resetHandlers();
  });

  it("renders nothing and redirects when no token", () => {
    renderWithProviders(<ResetPassword />, {
      initialEntries: ["/reset-password"],
    });
    expect(mockNavigate).toHaveBeenCalledWith("/login", { replace: true });
  });

  it("renders the reset password form with a token", () => {
    renderWithProviders(<ResetPassword />, {
      initialEntries: ["/reset-password?token=valid-token"],
    });
    expect(screen.getByTestId("reset-password-form")).toBeInTheDocument();
    expect(screen.getByTestId("reset-password-input")).toBeInTheDocument();
    expect(screen.getByTestId("reset-password-confirm-input")).toBeInTheDocument();
    expect(screen.getByTestId("reset-password-submit-button")).toBeInTheDocument();
  });

  it("shows password strength indicator when typing", async () => {
    const user = userEvent.setup();
    renderWithProviders(<ResetPassword />, {
      initialEntries: ["/reset-password?token=valid-token"],
    });
    await user.type(screen.getByTestId("reset-password-input"), "Str0ng!P@ssw0rd#2026Xyz");
    expect(await screen.findByText("Très fort")).toBeInTheDocument();
  });

  it("shows validation error on password mismatch", async () => {
    const user = userEvent.setup();
    renderWithProviders(<ResetPassword />, {
      initialEntries: ["/reset-password?token=valid-token"],
    });
    await user.type(screen.getByTestId("reset-password-input"), "StrongPass1!");
    await user.type(screen.getByTestId("reset-password-confirm-input"), "DifferentPass1!");
    await user.click(screen.getByTestId("reset-password-submit-button"));
    expect(await screen.findByText("Les mots de passe ne correspondent pas")).toBeInTheDocument();
  });

  it("submits successfully and navigates to login", async () => {
    const user = userEvent.setup();
    renderWithProviders(<ResetPassword />, {
      initialEntries: ["/reset-password?token=valid-token"],
    });
    await user.type(screen.getByTestId("reset-password-input"), "StrongPass1!");
    await user.type(screen.getByTestId("reset-password-confirm-input"), "StrongPass1!");
    await user.click(screen.getByTestId("reset-password-submit-button"));
    await vi.waitFor(() => {
      expect(toast.success).toHaveBeenCalled();
    });
    expect(mockNavigate).toHaveBeenCalledWith("/login");
  });

  it("shows error on expired token", async () => {
    const user = userEvent.setup();
    renderWithProviders(<ResetPassword />, {
      initialEntries: ["/reset-password?token=expired-token"],
    });
    await user.type(screen.getByTestId("reset-password-input"), "StrongPass1!");
    await user.type(screen.getByTestId("reset-password-confirm-input"), "StrongPass1!");
    await user.click(screen.getByTestId("reset-password-submit-button"));
    await vi.waitFor(() => {
      expect(toast.error).toHaveBeenCalled();
    });
  });

  it("has a link back to login", () => {
    renderWithProviders(<ResetPassword />, {
      initialEntries: ["/reset-password?token=valid-token"],
    });
    expect(screen.getByText("Retour à la connexion")).toBeInTheDocument();
  });
});
