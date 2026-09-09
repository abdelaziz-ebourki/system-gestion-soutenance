import { screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { describe, it, expect, vi, beforeEach } from "vitest";
import NotFound from "@/pages/NotFound";
import { renderWithProviders } from "@/test/utils";

const mockNavigate = vi.fn();
vi.mock("react-router-dom", async (importOriginal) => {
  const actual = await importOriginal<typeof import("react-router-dom")>();
  return { ...actual, useNavigate: () => mockNavigate };
});

describe("NotFound", () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it("renders 404 page", () => {
    renderWithProviders(<NotFound />);
    expect(screen.getByTestId("not-found")).toBeInTheDocument();
    expect(screen.getByTestId("not-found-title")).toHaveTextContent("404");
    expect(screen.getByTestId("not-found-home-btn")).toBeInTheDocument();
  });

  it("navigates to admin home for admin users", async () => {
    const user = userEvent.setup();
    renderWithProviders(<NotFound />, {
      initialAuthState: {
        user: {
          id: 1,
          email: "admin@univh2c.ma",
          firstName: "Admin",
          lastName: "User",
          role: "admin" as const,
          isActive: true,
        },
      },
    });
    await user.click(await screen.findByTestId("not-found-home-btn"));
    expect(mockNavigate).toHaveBeenCalledWith("/admin");
  });

  it("navigates to student home for student users", async () => {
    const user = userEvent.setup();
    renderWithProviders(<NotFound />, {
      initialAuthState: {
        user: {
          id: 4,
          email: "student@univh2c.ma",
          firstName: "Student",
          lastName: "User",
          role: "student" as const,
          isActive: true,
        },
      },
    });
    await user.click(await screen.findByTestId("not-found-home-btn"));
    expect(mockNavigate).toHaveBeenCalledWith("/student");
  });

  it("navigates to login for unauthenticated users", async () => {
    const user = userEvent.setup();
    renderWithProviders(<NotFound />);
    await user.click(await screen.findByTestId("not-found-home-btn"));
    expect(mockNavigate).toHaveBeenCalledWith("/login");
  });
});
