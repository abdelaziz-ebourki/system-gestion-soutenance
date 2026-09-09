import { renderHook, act } from "@testing-library/react";
import { describe, it, expect, vi, beforeEach } from "vitest";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { useEvaluationForm } from "@/hooks/use-evaluation-form";
import type { ReactNode } from "react";

vi.mock("sonner", () => ({ toast: { success: vi.fn(), error: vi.fn() } }));

function createWrapper() {
  const qc = new QueryClient({
    defaultOptions: { queries: { retry: false }, mutations: { retry: false } },
  });
  return function Wrapper({ children }: { children: ReactNode }) {
    return <QueryClientProvider client={qc}>{children}</QueryClientProvider>;
  };
}

const mockEvaluation = {
  id: 1,
  projectId: 1,
  projectTitle: "Test",
  finalGrade: 0,
  comment: "",
  status: "pending",
};

describe("useEvaluationForm", () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it("initializes with default state", () => {
    const { result } = renderHook(() => useEvaluationForm(), { wrapper: createWrapper() });
    expect(result.current.selected).toBeNull();
    expect(result.current.isDialogOpen).toBe(false);
    expect(result.current.formData).toEqual({ score: 0, comment: "" });
    expect(result.current.fieldErrors).toEqual({});
  });

  it("openEdit populates form with evaluation data", () => {
    const { result } = renderHook(() => useEvaluationForm(), { wrapper: createWrapper() });
    act(() => result.current.openEdit({ ...mockEvaluation, finalGrade: 15, comment: "Good" }));
    expect(result.current.selected).toBeDefined();
    expect(result.current.formData.score).toBe(15);
    expect(result.current.formData.comment).toBe("Good");
    expect(result.current.isDialogOpen).toBe(true);
  });

  it("handleSubmit with no selected returns early", async () => {
    const { result } = renderHook(() => useEvaluationForm(), { wrapper: createWrapper() });
    const fakeEvent = { preventDefault: vi.fn() };
    await act(async () => {
      await result.current.handleSubmit(fakeEvent);
    });
    expect(fakeEvent.preventDefault).toHaveBeenCalled();
  });

  it("handleSubmit with selected submits successfully", async () => {
    const { result } = renderHook(() => useEvaluationForm(), { wrapper: createWrapper() });
    act(() => result.current.openEdit({ ...mockEvaluation, finalGrade: 15, comment: "Good" }));
    const fakeEvent = { preventDefault: vi.fn() };
    await act(async () => {
      await result.current.handleSubmit(fakeEvent);
    });
    expect(result.current.isDialogOpen).toBe(false);
    expect(result.current.selected).toBeNull();
    expect(result.current.formData).toEqual({ score: 0, comment: "" });
  });

  it("handleSubmit sets field errors for invalid data", async () => {
    const { result } = renderHook(() => useEvaluationForm(), { wrapper: createWrapper() });
    act(() => result.current.openEdit({ ...mockEvaluation, finalGrade: 15, comment: "Good" }));
    act(() => result.current.setFormData({ score: -1, comment: "" }));
    const fakeEvent = { preventDefault: vi.fn() };
    await act(async () => {
      await result.current.handleSubmit(fakeEvent);
    });
    expect(Object.keys(result.current.fieldErrors).length).toBeGreaterThan(0);
  });
});
