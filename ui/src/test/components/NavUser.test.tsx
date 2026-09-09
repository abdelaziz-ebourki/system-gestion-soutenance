import { screen, waitFor, render, fireEvent } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { describe, it, expect, vi, beforeEach } from "vitest";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { MemoryRouter } from "react-router-dom";
import { AuthProvider } from "@/contexts/auth-context";
import { TooltipProvider } from "@/components/ui/tooltip";
import { SidebarProvider } from "@/components/ui/sidebar";
import { NavUser } from "@/components/nav-user";
import type { ReactNode } from "react";

const { mockSetTheme, mockUseUnreadCount } = vi.hoisted(() => ({
  mockSetTheme: vi.fn(),
  mockUseUnreadCount: vi.fn(() => 3),
}));

vi.mock("next-themes", () => ({
  useTheme: () => ({ setTheme: mockSetTheme, theme: "light" }),
}));

vi.mock("@/hooks/queries", () => ({
  useUnreadCount: mockUseUnreadCount,
}));

function renderNavUser(ui: ReactNode, options: { token?: string; user?: Record<string, unknown> } = {}) {
  const qc = new QueryClient({ defaultOptions: { queries: { retry: false } } });
  if (options.token) localStorage.setItem("token", options.token);
  if (options.user) localStorage.setItem("user", JSON.stringify(options.user));
  localStorage.setItem("expiresAt", String(Date.now() + 7200000));
  return render(
    <QueryClientProvider client={qc}>
      <AuthProvider>
        <TooltipProvider>
          <SidebarProvider>
            <MemoryRouter>{ui}</MemoryRouter>
          </SidebarProvider>
        </TooltipProvider>
      </AuthProvider>
    </QueryClientProvider>,
  );
}

const mockUser = {
  lastName: "Dupont",
  firstName: "Jean",
  email: "jean@example.com",
  avatar: "https://example.com/avatar.jpg",
};

const authState = { token: "mock", user: { id: 1, email: mockUser.email, role: "admin", lastName: mockUser.lastName, firstName: mockUser.firstName } };

describe("NavUser", () => {
  beforeEach(() => {
    vi.clearAllMocks();
    localStorage.clear();
  });

  it("renders user info", () => {
    renderNavUser(<NavUser user={mockUser} />, authState);
    expect(screen.getByText("Dupont Jean")).toBeInTheDocument();
    expect(screen.getByText(mockUser.email)).toBeInTheDocument();
  });

  it("opens dropdown menu on trigger click", async () => {
    const user = userEvent.setup();
    renderNavUser(<NavUser user={mockUser} />, authState);
    await user.click(screen.getByTestId("nav-user-trigger"));
    expect(await screen.findByText("Mon Profil")).toBeInTheDocument();
  });

  it("shows logout confirmation dialog", async () => {
    const user = userEvent.setup();
    renderNavUser(<NavUser user={mockUser} />, authState);
    await user.click(screen.getByTestId("nav-user-trigger"));
    await user.click(await screen.findByTestId("nav-user-logout-trigger"));
    expect(await screen.findByTestId("nav-user-logout-dialog")).toBeInTheDocument();
  });

  it("cancels logout dialog", async () => {
    const user = userEvent.setup();
    renderNavUser(<NavUser user={mockUser} />, authState);
    await user.click(screen.getByTestId("nav-user-trigger"));
    await user.click(await screen.findByTestId("nav-user-logout-trigger"));
    expect(await screen.findByTestId("nav-user-logout-dialog")).toBeInTheDocument();
    await user.click(screen.getByTestId("nav-user-logout-cancel"));
    await waitFor(() => expect(screen.queryByTestId("nav-user-logout-dialog")).not.toBeInTheDocument());
  });

  it("confirms logout", async () => {
    const user = userEvent.setup();
    renderNavUser(<NavUser user={mockUser} />, authState);
    await user.click(screen.getByTestId("nav-user-trigger"));
    await user.click(await screen.findByTestId("nav-user-logout-trigger"));
    await user.click(await screen.findByTestId("nav-user-logout-confirm"));
    await waitFor(() => expect(screen.queryByTestId("nav-user-logout-dialog")).not.toBeInTheDocument());
  });

  it("changes theme to light", async () => {
    renderNavUser(<NavUser user={mockUser} />, authState);
    await userEvent.click(screen.getByTestId("nav-user-trigger"));
    const themeSubTrigger = screen.getByRole("menuitem", { name: /thème/i });
    await userEvent.hover(themeSubTrigger);
    const lightOption = await screen.findByRole("menuitem", { name: /clair/i });
    fireEvent.click(lightOption);
    expect(mockSetTheme).toHaveBeenCalledWith("light");
  });

  it("changes theme to dark", async () => {
    renderNavUser(<NavUser user={mockUser} />, authState);
    await userEvent.click(screen.getByTestId("nav-user-trigger"));
    const themeSubTrigger = screen.getByRole("menuitem", { name: /thème/i });
    await userEvent.hover(themeSubTrigger);
    const darkOption = await screen.findByRole("menuitem", { name: /sombre/i });
    fireEvent.click(darkOption);
    expect(mockSetTheme).toHaveBeenCalledWith("dark");
  });

  it("changes theme to system", async () => {
    renderNavUser(<NavUser user={mockUser} />, authState);
    await userEvent.click(screen.getByTestId("nav-user-trigger"));
    const themeSubTrigger = screen.getByRole("menuitem", { name: /thème/i });
    await userEvent.hover(themeSubTrigger);
    const systemOption = await screen.findByRole("menuitem", { name: /système/i });
    fireEvent.click(systemOption);
    expect(mockSetTheme).toHaveBeenCalledWith("system");
  });

  it("navigates to profile", async () => {
    const user = userEvent.setup();
    renderNavUser(<NavUser user={mockUser} />, authState);
    await user.click(screen.getByTestId("nav-user-trigger"));
    await user.click(await screen.findByTestId("nav-user-profile"));
    expect(screen.queryByTestId("nav-user-trigger")).toBeInTheDocument();
  });

  it("navigates to notifications", async () => {
    const user = userEvent.setup();
    renderNavUser(<NavUser user={mockUser} />, authState);
    await user.click(screen.getByTestId("nav-user-trigger"));
    await user.click(await screen.findByTestId("nav-user-notifications"));
    expect(screen.queryByTestId("nav-user-trigger")).toBeInTheDocument();
  });

  it("shows unread badge when count > 0", async () => {
    mockUseUnreadCount.mockReturnValue(3);
    const user = userEvent.setup();
    renderNavUser(<NavUser user={mockUser} />, authState);
    await user.click(screen.getByTestId("nav-user-trigger"));
    expect(await screen.findByText("3")).toBeInTheDocument();
  });

  it("shows 9+ when unread count > 9", async () => {
    mockUseUnreadCount.mockReturnValue(15);
    const user = userEvent.setup();
    renderNavUser(<NavUser user={mockUser} />, authState);
    await user.click(screen.getByTestId("nav-user-trigger"));
    expect(await screen.findByText("9+")).toBeInTheDocument();
  });

  it("hides badge when unread count is 0", async () => {
    mockUseUnreadCount.mockReturnValue(0);
    const user = userEvent.setup();
    renderNavUser(<NavUser user={mockUser} />, authState);
    await user.click(screen.getByTestId("nav-user-trigger"));
    expect(screen.queryByText("3")).not.toBeInTheDocument();
    expect(screen.queryByText("9+")).not.toBeInTheDocument();
  });
});

