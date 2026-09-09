import { render, screen } from "@testing-library/react";
import { describe, it, expect } from "vitest";
import { ThemeProvider } from "@/components/theme-provider";

describe("ThemeProvider", () => {
  it("renders children", () => {
    render(<ThemeProvider><div data-testid="child">hello</div></ThemeProvider>);
    expect(screen.getByTestId("child")).toHaveTextContent("hello");
  });
});
