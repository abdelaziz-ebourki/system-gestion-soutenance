import { renderHook, act } from "@testing-library/react";
import { describe, it, expect, vi, beforeEach } from "vitest";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { useDepartmentCrud } from "@/hooks/entities/use-department-crud";
import { toast } from "sonner";
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

const mockDepartment = {
  id: 1,
  name: "Informatique",
  code: "INFO",
  headId: 1,
};

describe("useDepartmentCrud", () => {
  beforeEach(() => {
    vi.clearAllMocks();
    localStorage.setItem("token", "fake-jwt-token");
  });

  it("initializes with default state", () => {
    const { result } = renderHook(() => useDepartmentCrud(), { wrapper: createWrapper() });
    expect(result.current.selected).toBeNull();
    expect(result.current.isDialogOpen).toBe(false);
    expect(result.current.formData).toEqual({ name: "", code: "", headId: "" });
  });

  it("openCreate resets form and opens dialog", () => {
    const { result } = renderHook(() => useDepartmentCrud(), { wrapper: createWrapper() });
    act(() => result.current.openCreate());
    expect(result.current.selected).toBeNull();
    expect(result.current.isDialogOpen).toBe(true);
    expect(result.current.formData.name).toBe("");
  });

  it("openEdit populates form", () => {
    const { result } = renderHook(() => useDepartmentCrud(), { wrapper: createWrapper() });
    act(() => result.current.openEdit(mockDepartment));
    expect(result.current.selected).toEqual(mockDepartment);
    expect(result.current.formData.name).toBe("Informatique");
    expect(result.current.formData.code).toBe("INFO");
    expect(result.current.formData.headId).toBe("1");
  });

  it("openDelete sets selected and opens dialog", () => {
    const { result } = renderHook(() => useDepartmentCrud(), { wrapper: createWrapper() });
    act(() => result.current.openDelete(mockDepartment));
    expect(result.current.selected).toEqual(mockDepartment);
    expect(result.current.isDeleteDialogOpen).toBe(true);
  });

  it("handleSubmit with invalid data does not proceed", async () => {
    const { result } = renderHook(() => useDepartmentCrud(), { wrapper: createWrapper() });
    act(() => result.current.openCreate());
    expect(result.current.isDialogOpen).toBe(true);
    const fakeEvent = { preventDefault: vi.fn() };
    await act(async () => {
      await result.current.handleSubmit(fakeEvent);
    });
    expect(fakeEvent.preventDefault).toHaveBeenCalled();
    expect(result.current.isDialogOpen).toBe(true);
  });

  it("handleSubmit with valid data creates a department", async () => {
    const { result } = renderHook(() => useDepartmentCrud(), { wrapper: createWrapper() });
    act(() => {
      result.current.setFormData({ name: "Mathématiques", code: "MATH", headId: "1" });
    });
    const fakeEvent = { preventDefault: vi.fn() };
    await act(async () => {
      await result.current.handleSubmit(fakeEvent);
    });
    expect(result.current.isDialogOpen).toBe(false);
    expect(result.current.selected).toBeNull();
    expect(result.current.formData).toEqual({ name: "", code: "", headId: "" });
    expect(toast.success).toHaveBeenCalledWith("Département ajouté avec succès");
  });

  it("handleDelete deletes department", async () => {
    const { result } = renderHook(() => useDepartmentCrud(), { wrapper: createWrapper() });
    act(() => result.current.openDelete(mockDepartment));
    await act(async () => {
      await result.current.handleDelete();
    });
    expect(result.current.isDeleteDialogOpen).toBe(false);
    expect(result.current.selected).toBeNull();
  });

  it("handleClose closes create/edit dialog", () => {
    const { result } = renderHook(() => useDepartmentCrud(), { wrapper: createWrapper() });
    act(() => result.current.openCreate());
    expect(result.current.isDialogOpen).toBe(true);
    act(() => result.current.handleClose());
    expect(result.current.isDialogOpen).toBe(false);
  });

  it("handleCloseDelete closes delete dialog", () => {
    const { result } = renderHook(() => useDepartmentCrud(), { wrapper: createWrapper() });
    act(() => result.current.openDelete(mockDepartment));
    expect(result.current.isDeleteDialogOpen).toBe(true);
    act(() => result.current.handleCloseDelete());
    expect(result.current.isDeleteDialogOpen).toBe(false);
  });
});
