import { render, screen, act } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { describe, it, expect, beforeEach, vi } from "vitest";
import { AuthProvider, useAuth } from "@/contexts/auth-context";
import { STORAGE_KEYS } from "@/lib/constants";
import { MemoryRouter } from "react-router-dom";

vi.mock("@/lib/api-auth", () => ({
  logout: vi.fn().mockResolvedValue(undefined),
}));

function TestConsumer() {
  const { user, isAuthenticated, isLoading, wasExpired, login, logout } = useAuth();
  return (
    <div>
      <div data-testid="loading">{String(isLoading)}</div>
      <div data-testid="authenticated">{String(isAuthenticated)}</div>
      <div data-testid="was-expired">{String(wasExpired)}</div>
      <div data-testid="user-email">{user?.email ?? "null"}</div>
      <button
        data-testid="login-btn"
        onClick={() =>
          login({ id: 1, email: "test@test.com", firstName: "Test", lastName: "User", role: "admin", isActive: true })
        }
      />
      <button data-testid="logout-btn" onClick={() => logout()} />
    </div>
  );
}

function renderWithAuth() {
  return render(
    <MemoryRouter>
      <AuthProvider>
        <TestConsumer />
      </AuthProvider>
    </MemoryRouter>,
  );
}

describe("AuthProvider", () => {
  beforeEach(() => {
    localStorage.clear();
  });

  it("shows no authentication after mount", async () => {
    renderWithAuth();
    expect(await screen.findByTestId("loading")).toHaveTextContent("false");
    expect(screen.getByTestId("authenticated")).toHaveTextContent("false");
    expect(screen.getByTestId("user-email")).toHaveTextContent("null");
  });

  it("restores session from localStorage if not expired", async () => {
    localStorage.setItem(STORAGE_KEYS.USER,
      JSON.stringify({
        id: 2,
        email: "stored@test.com",
        firstName: "Stored",
        lastName: "User",
        role: "teacher",
        isActive: true,
      }),
    );

    renderWithAuth();

    expect(screen.getByTestId("user-email")).toHaveTextContent("stored@test.com");
    expect(screen.getByTestId("authenticated")).toHaveTextContent("true");
  });

  it("clears expired session and sets wasExpired", async () => {
    renderWithAuth();

    await act(async () => {
      window.dispatchEvent(new CustomEvent("auth:expired"));
    });

    expect(await screen.findByTestId("was-expired")).toHaveTextContent("true");
    expect(localStorage.getItem(STORAGE_KEYS.USER)).toBeNull();
  });

  it("clears invalid stored user data", async () => {
    localStorage.setItem(STORAGE_KEYS.USER, '"not a valid user object"');

    renderWithAuth();

    expect(localStorage.getItem(STORAGE_KEYS.USER)).toBeNull();
  });

  it("login stores user", async () => {
    const user = userEvent.setup();
    renderWithAuth();

    expect(await screen.findByTestId("loading")).toHaveTextContent("false");
    expect(screen.getByTestId("authenticated")).toHaveTextContent("false");

    await user.click(screen.getByTestId("login-btn"));

    expect(screen.getByTestId("authenticated")).toHaveTextContent("true");
    expect(screen.getByTestId("user-email")).toHaveTextContent("test@test.com");
    expect(localStorage.getItem(STORAGE_KEYS.USER)).not.toBeNull();
  });

  it("logout clears user", async () => {
    const user = userEvent.setup();
    renderWithAuth();

    expect(await screen.findByTestId("loading")).toHaveTextContent("false");
    await user.click(screen.getByTestId("login-btn"));
    expect(screen.getByTestId("authenticated")).toHaveTextContent("true");

    await user.click(screen.getByTestId("logout-btn"));

    expect(screen.getByTestId("authenticated")).toHaveTextContent("false");
    expect(localStorage.getItem(STORAGE_KEYS.USER)).toBeNull();
  });
});

describe("useAuth", () => {
  it("throws when used outside AuthProvider", () => {
    expect(() => render(<TestConsumer />)).toThrow(
      "useAuth must be used within an AuthProvider",
    );
  });
});
