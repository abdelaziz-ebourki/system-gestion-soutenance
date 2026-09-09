import { screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { describe, it, expect, vi, beforeEach } from "vitest";
import ForgotPassword from "@/pages/auth/ForgotPassword";
import { renderWithProviders } from "@/test/utils";
import { server } from "@/test/mocks/server";
import { http, HttpResponse } from "msw";
import { toast } from "sonner";

vi.mock("sonner", () => ({
  toast: { success: vi.fn(), error: vi.fn() },
}));

describe("ForgotPassword", () => {
  beforeEach(() => {
    vi.clearAllMocks();
    server.resetHandlers();
  });

  it("renders the forgot password form", () => {
    renderWithProviders(<ForgotPassword />);
    expect(screen.getByTestId("forgot-password-form")).toBeInTheDocument();
    expect(screen.getByTestId("forgot-password-email-input")).toBeInTheDocument();
    expect(screen.getByTestId("forgot-password-submit-button")).toBeInTheDocument();
  });

  it("shows success state after submitting valid email", async () => {
    const user = userEvent.setup();
    renderWithProviders(<ForgotPassword />);
    await user.type(screen.getByTestId("forgot-password-email-input"), "student@univh2c.ma");
    await user.click(screen.getByTestId("forgot-password-submit-button"));
    expect(await screen.findByTestId("forgot-password-success-card")).toBeInTheDocument();
    expect(screen.getByText("Email envoyé")).toBeInTheDocument();
    expect(toast.success).toHaveBeenCalledWith("Si cet email existe, un lien de réinitialisation a été envoyé.");
  });

  it("shows error toast on API failure", async () => {
    const user = userEvent.setup();
    server.use(
      http.post("*/api/auth/forgot-password", () =>
        HttpResponse.json({ message: "Erreur serveur" }, { status: 500 }),
      ),
    );
    renderWithProviders(<ForgotPassword />);
    await user.type(screen.getByTestId("forgot-password-email-input"), "student@univh2c.ma");
    await user.click(screen.getByTestId("forgot-password-submit-button"));
    await vi.waitFor(() => {
      expect(toast.error).toHaveBeenCalled();
    });
  });

  it("has a link back to login", () => {
    renderWithProviders(<ForgotPassword />);
    expect(screen.getByText("Retour à la connexion")).toBeInTheDocument();
  });
});
