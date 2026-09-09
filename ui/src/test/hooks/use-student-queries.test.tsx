import { renderHook, act, waitFor } from "@testing-library/react";
import { describe, it, expect } from "vitest";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import {
  useStudentStats,
  useStudentDefense,
  useStudentGroup,
  useCreateStudentGroup,
  useJoinStudentGroup,
  useStudentDocuments,
  useUploadStudentDocument,
} from "@/hooks/use-student-queries";
import type { ReactNode } from "react";

function createWrapper() {
  const qc = new QueryClient({
    defaultOptions: { queries: { retry: false }, mutations: { retry: false } },
  });
  return function Wrapper({ children }: { children: ReactNode }) {
    return <QueryClientProvider client={qc}>{children}</QueryClientProvider>;
  };
}

describe("useStudentStats", () => {
  it("returns stats data", async () => {
    const { result } = renderHook(() => useStudentStats(), { wrapper: createWrapper() });
    await waitFor(() => expect(result.current.isSuccess).toBe(true));
    expect(result.current.data).toBeDefined();
    expect(result.current.data).toHaveProperty("documentCount");
  });
});

describe("useStudentDefense", () => {
  it("returns defense details", async () => {
    const { result } = renderHook(() => useStudentDefense(), { wrapper: createWrapper() });
    await waitFor(() => expect(result.current.isSuccess).toBe(true));
    expect(result.current.data).toHaveProperty("projectTitle");
  });
});

describe("useStudentGroup", () => {
  it("returns group data", async () => {
    const { result } = renderHook(() => useStudentGroup(), { wrapper: createWrapper() });
    await waitFor(() => expect(result.current.isSuccess).toBe(true));
    expect(result.current.data).toHaveProperty("currentGroup");
  });
});

describe("useCreateStudentGroup", () => {
  it("creates a student group", async () => {
    const { result } = renderHook(() => useCreateStudentGroup(), { wrapper: createWrapper() });
    act(() => { result.current.mutate(); });
    await waitFor(() => expect(result.current.isSuccess).toBe(true));
  });
});

describe("useJoinStudentGroup", () => {
  it("joins a student group", async () => {
    const { result } = renderHook(() => useJoinStudentGroup(), { wrapper: createWrapper() });
    act(() => { result.current.mutate(2); });
    await waitFor(() => expect(result.current.isSuccess).toBe(true));
  });
});

describe("useStudentDocuments", () => {
  it("returns documents data", async () => {
    const { result } = renderHook(() => useStudentDocuments(), { wrapper: createWrapper() });
    await waitFor(() => expect(result.current.isSuccess).toBe(true));
    expect(Array.isArray(result.current.data?.items)).toBe(true);
  });
});

describe("useUploadStudentDocument", () => {
  it("uploads a document", async () => {
    const { result } = renderHook(() => useUploadStudentDocument(), { wrapper: createWrapper() });
    const file = new File(["test"], "test.pdf", { type: "application/pdf" });
    act(() => { result.current.mutate({ documentId: 1, file }); });
    await waitFor(() => expect(result.current.isSuccess).toBe(true));
  });
});
