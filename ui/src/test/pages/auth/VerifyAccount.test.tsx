import { screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { describe, it, expect, vi, beforeEach } from "vitest";
import VerifyAccount from "@/pages/auth/VerifyAccount";
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

describe("VerifyAccount", () => {
  beforeEach(() => {
    vi.clearAllMocks();
    server.resetHandlers();
  });

  it("renders nothing and redirects when no token", () => {
    renderWithProviders(<VerifyAccount />, {
      initialEntries: ["/verify-account"],
    });
    expect(mockNavigate).toHaveBeenCalledWith("/login", { replace: true });
  });

  it("renders the verify account form with a token", () => {
    renderWithProviders(<VerifyAccount />, {
      initialEntries: ["/verify-account?token=valid-token"],
    });
    expect(screen.getByTestId("verify-account-form")).toBeInTheDocument();
    expect(screen.getByTestId("verify-account-password-input")).toBeInTheDocument();
    expect(screen.getByTestId("verify-account-confirm-input")).toBeInTheDocument();
    expect(screen.getByTestId("verify-account-submit-button")).toBeInTheDocument();
  });

  it("shows password strength indicator when typing", async () => {
    const user = userEvent.setup();
    renderWithProviders(<VerifyAccount />, {
      initialEntries: ["/verify-account?token=valid-token"],
    });
    await user.type(screen.getByTestId("verify-account-password-input"), "Str0ng!P@ssw0rd#2026Xyz");
    expect(await screen.findByText("Très fort")).toBeInTheDocument();
  });

  it("shows validation error on password mismatch", async () => {
    const user = userEvent.setup();
    renderWithProviders(<VerifyAccount />, {
      initialEntries: ["/verify-account?token=valid-token"],
    });
    await user.type(screen.getByTestId("verify-account-password-input"), "StrongPass1!");
    await user.type(screen.getByTestId("verify-account-confirm-input"), "DifferentPass1!");
    await user.click(screen.getByTestId("verify-account-submit-button"));
    expect(await screen.findByText("Les mots de passe ne correspondent pas")).toBeInTheDocument();
  });

  it("submits successfully and navigates to login", async () => {
    const user = userEvent.setup();
    renderWithProviders(<VerifyAccount />, {
      initialEntries: ["/verify-account?token=valid-token"],
    });
    await user.type(screen.getByTestId("verify-account-password-input"), "StrongPass1!");
    await user.type(screen.getByTestId("verify-account-confirm-input"), "StrongPass1!");
    await user.click(screen.getByTestId("verify-account-submit-button"));
    await vi.waitFor(() => {
      expect(toast.success).toHaveBeenCalled();
    });
    expect(mockNavigate).toHaveBeenCalledWith("/login");
  });

  it("shows error on invalid token", async () => {
    const user = userEvent.setup();
    renderWithProviders(<VerifyAccount />, {
      initialEntries: ["/verify-account?token=invalid-token"],
    });
    await user.type(screen.getByTestId("verify-account-password-input"), "StrongPass1!");
    await user.type(screen.getByTestId("verify-account-confirm-input"), "StrongPass1!");
    await user.click(screen.getByTestId("verify-account-submit-button"));
    await vi.waitFor(() => {
      expect(toast.error).toHaveBeenCalled();
    });
  });
});
