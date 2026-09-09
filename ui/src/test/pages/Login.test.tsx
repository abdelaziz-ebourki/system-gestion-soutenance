import { render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { describe, it, expect, vi, beforeEach } from "vitest";
import { MemoryRouter } from "react-router-dom";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { AuthProvider } from "@/contexts/auth-context";
import Login from "@/pages/Login";
import { toast } from "sonner";

vi.mock("sonner", () => ({
  toast: { success: vi.fn(), error: vi.fn() },
}));

const mockNavigate = vi.fn();
vi.mock("react-router-dom", async (importOriginal) => {
  const actual = await importOriginal<typeof import("react-router-dom")>();
  return { ...actual, useNavigate: () => mockNavigate };
});

function renderLogin() {
  const queryClient = new QueryClient({
    defaultOptions: { queries: { retry: false }, mutations: { retry: false } },
  });
  return render(
    <QueryClientProvider client={queryClient}>
      <AuthProvider>
        <MemoryRouter initialEntries={["/login"]}>
          <Login />
        </MemoryRouter>
      </AuthProvider>
    </QueryClientProvider>,
  );
}

describe("Login page", () => {
  beforeEach(() => {
    vi.clearAllMocks();
    localStorage.clear();
  });

  it("renders the login form", () => {
    renderLogin();
    expect(screen.getByPlaceholderText(/nom\.prenom@univh2c\.ma/)).toBeInTheDocument();
    expect(screen.getByPlaceholderText("••••••••")).toBeInTheDocument();
    expect(screen.getByRole("button", { name: /se connecter/i })).toBeInTheDocument();
  });

  it("shows validation errors on empty submit", async () => {
    const user = userEvent.setup();
    renderLogin();
    await user.click(screen.getByRole("button", { name: /se connecter/i }));
    await screen.findByText(/email/i);
  });

  it("navigates to admin on successful admin login", async () => {
    const user = userEvent.setup();
    renderLogin();

    await user.type(screen.getByPlaceholderText(/nom\.prenom@univh2c\.ma/), "admin@univh2c.ma");
    await user.type(screen.getByPlaceholderText("••••••••"), "1234");
    await user.click(screen.getByRole("button", { name: /se connecter/i }));

    await vi.waitFor(() => {
      expect(toast.success).toHaveBeenCalled();
    });
    expect(mockNavigate).toHaveBeenCalledWith("/admin");
  });

  it("navigates to teacher on successful teacher login", async () => {
    const user = userEvent.setup();
    renderLogin();

    await user.type(screen.getByPlaceholderText(/nom\.prenom@univh2c\.ma/), "teacher@univh2c.ma");
    await user.type(screen.getByPlaceholderText("••••••••"), "1234");
    await user.click(screen.getByRole("button", { name: /se connecter/i }));

    await vi.waitFor(() => {
      expect(toast.success).toHaveBeenCalled();
    });
    expect(mockNavigate).toHaveBeenCalledWith("/teacher");
  });

  it("shows error toast on failed login", async () => {
    const user = userEvent.setup();
    renderLogin();

    await user.type(screen.getByPlaceholderText(/nom\.prenom@univh2c\.ma/), "wrong@test.com");
    await user.type(screen.getByPlaceholderText("••••••••"), "wrong-password");
    await user.click(screen.getByRole("button", { name: /se connecter/i }));

    await vi.waitFor(() => {
      expect(toast.error).toHaveBeenCalled();
    });
  });
});
