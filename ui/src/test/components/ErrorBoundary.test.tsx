import { render, screen, fireEvent } from "@testing-library/react";
import { describe, it, expect, vi } from "vitest";
import { ErrorBoundary } from "@/components/ErrorBoundary";

vi.spyOn(console, "error").mockImplementation(() => {});

function ThrowError(): never {
  throw new Error("test error");
}

describe("ErrorBoundary", () => {
  it("renders children when no error", () => {
    render(<ErrorBoundary><div data-testid="child">ok</div></ErrorBoundary>);
    expect(screen.getByTestId("child")).toBeInTheDocument();
  });

  it("renders error UI when a child throws", () => {
    render(<ErrorBoundary><ThrowError /></ErrorBoundary>);
    expect(screen.getByTestId("error-boundary")).toBeInTheDocument();
    expect(screen.getByTestId("error-boundary-title")).toHaveTextContent("Une erreur est survenue");
    expect(screen.getByTestId("error-boundary-description")).toBeInTheDocument();
    expect(screen.getByTestId("error-boundary-retry-btn")).toBeInTheDocument();
  });

  it("resets error state on retry click", () => {
    let shouldThrow = true;
    function ConditionalThrow() {
      if (shouldThrow) throw new Error("test");
      return <div data-testid="child">ok</div>;
    }
    render(<ErrorBoundary><ConditionalThrow /></ErrorBoundary>);
    expect(screen.getByTestId("error-boundary")).toBeInTheDocument();
    shouldThrow = false;
    fireEvent.click(screen.getByTestId("error-boundary-retry-btn"));
    expect(screen.getByTestId("child")).toBeInTheDocument();
  });
});
