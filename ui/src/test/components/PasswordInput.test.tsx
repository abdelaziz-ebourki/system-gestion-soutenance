import { render, screen, fireEvent } from "@testing-library/react";
import { describe, it, expect, vi } from "vitest";
import { PasswordInput } from "@/components/ui/password-input";

vi.mock("@zxcvbn-ts/core", () => ({
  zxcvbn: () => ({ score: 0, feedback: { warning: undefined } }),
  zxcvbnOptions: { setOptions: vi.fn() },
}));

vi.mock("@zxcvbn-ts/language-common", () => ({ adjacencyGraphs: {} }));
vi.mock("@zxcvbn-ts/language-en", () => ({ translations: {} }));

describe("PasswordInput", () => {
  it("renders password input by default", () => {
    render(<PasswordInput data-testid="pw" />);
    const input = screen.getByTestId("pw") as HTMLInputElement;
    expect(input.type).toBe("password");
  });

  it("toggles visibility on button click", () => {
    render(<PasswordInput data-testid="pw" />);
    const input = screen.getByTestId("pw") as HTMLInputElement;
    const toggleBtn = screen.getByRole("button");
    fireEvent.click(toggleBtn);
    expect(input.type).toBe("text");
    fireEvent.click(toggleBtn);
    expect(input.type).toBe("password");
  });

  it("shows label when provided", () => {
    render(<PasswordInput label="Mot de passe" data-testid="pw" />);
    expect(screen.getByText("Mot de passe")).toBeInTheDocument();
  });

  it("shows error when provided", () => {
    render(<PasswordInput error="Invalid password" data-testid="pw" />);
    expect(screen.getByText("Invalid password")).toBeInTheDocument();
  });

  it("shows description when provided", () => {
    render(<PasswordInput description="At least 8 chars" data-testid="pw" />);
    expect(screen.getByText("At least 8 chars")).toBeInTheDocument();
  });

  it("calls onChange when typing", () => {
    const onChange = vi.fn();
    render(<PasswordInput onChange={onChange} data-testid="pw" />);
    fireEvent.change(screen.getByTestId("pw"), { target: { value: "secret" } });
    expect(onChange).toHaveBeenCalledTimes(1);
  });
});
