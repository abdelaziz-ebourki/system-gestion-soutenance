import { renderHook, act } from "@testing-library/react";
import { describe, it, expect, vi, beforeEach } from "vitest";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { useTeacherCrud } from "@/hooks/entities/use-teacher-crud";
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

const mockTeacher = {
  id: 1,
  lastName: "Benali",
  firstName: "Ahmed",
  email: "ahmed.benali@example.com",
  departmentId: 1,
  isActive: true,
  role: "teacher" as const,
};

describe("useTeacherCrud", () => {
  beforeEach(() => {
    vi.clearAllMocks();
    localStorage.setItem("token", "fake-jwt-token");
  });

  it("initializes with default state", () => {
    const { result } = renderHook(() => useTeacherCrud(), { wrapper: createWrapper() });
    expect(result.current.selected).toBeNull();
    expect(result.current.isDialogOpen).toBe(false);
    expect(result.current.isDeleteDialogOpen).toBe(false);
    expect(result.current.formData).toEqual({ lastName: "", firstName: "", email: "", departmentId: "" });
  });

  it("openCreate resets form and opens dialog", () => {
    const { result } = renderHook(() => useTeacherCrud(), { wrapper: createWrapper() });
    act(() => result.current.setFormData({ lastName: "test", firstName: "test", email: "t@t.com", departmentId: "1" }));
    act(() => result.current.openCreate());
    expect(result.current.selected).toBeNull();
    expect(result.current.isDialogOpen).toBe(true);
    expect(result.current.formData.lastName).toBe("");
  });

  it("openEdit populates form and opens dialog", () => {
    const { result } = renderHook(() => useTeacherCrud(), { wrapper: createWrapper() });
    act(() => result.current.openEdit(mockTeacher));
    expect(result.current.selected).toEqual(mockTeacher);
    expect(result.current.isDialogOpen).toBe(true);
    expect(result.current.formData.lastName).toBe("Benali");
    expect(result.current.formData.email).toBe("ahmed.benali@example.com");
    expect(result.current.formData.departmentId).toBe("1");
  });

  it("openDelete sets selected and opens delete dialog", () => {
    const { result } = renderHook(() => useTeacherCrud(), { wrapper: createWrapper() });
    act(() => result.current.openDelete(mockTeacher));
    expect(result.current.selected).toEqual(mockTeacher);
    expect(result.current.isDeleteDialogOpen).toBe(true);
  });

  it("handleSubmit with invalid data does not proceed", async () => {
    const { result } = renderHook(() => useTeacherCrud(), { wrapper: createWrapper() });
    act(() => result.current.openCreate());
    expect(result.current.isDialogOpen).toBe(true);
    const fakeEvent = { preventDefault: vi.fn() };
    await act(async () => {
      await result.current.handleSubmit(fakeEvent);
    });
    expect(fakeEvent.preventDefault).toHaveBeenCalled();
    expect(result.current.isDialogOpen).toBe(true);
  });

  it("handleSubmit with valid data creates a teacher", async () => {
    const { result } = renderHook(() => useTeacherCrud(), { wrapper: createWrapper() });
    act(() => {
      result.current.setFormData({ lastName: "New", firstName: "Teacher", email: "new@test.com", departmentId: "1" });
    });
    const fakeEvent = { preventDefault: vi.fn() };
    await act(async () => {
      await result.current.handleSubmit(fakeEvent);
    });
    expect(result.current.isDialogOpen).toBe(false);
    expect(result.current.selected).toBeNull();
    expect(result.current.formData).toEqual({ lastName: "", firstName: "", email: "", departmentId: "" });
    expect(toast.success).toHaveBeenCalledWith("Enseignant créé avec succès");
  });

  it("handleSubmit with valid data updates a teacher", async () => {
    const { result } = renderHook(() => useTeacherCrud(), { wrapper: createWrapper() });
    act(() => result.current.openEdit(mockTeacher));
    act(() => {
      result.current.setFormData({ lastName: "Updated", firstName: "Teacher", email: "updated@test.com", departmentId: "1" });
    });
    const fakeEvent = { preventDefault: vi.fn() };
    await act(async () => {
      await result.current.handleSubmit(fakeEvent);
    });
    expect(result.current.isDialogOpen).toBe(false);
    expect(result.current.selected).toBeNull();
    expect(toast.success).toHaveBeenCalledWith("Enseignant modifié avec succès");
  });

  it("handleDelete deletes a teacher", async () => {
    const { result } = renderHook(() => useTeacherCrud(), { wrapper: createWrapper() });
    act(() => result.current.openDelete(mockTeacher));
    await act(async () => {
      await result.current.handleDelete();
    });
    expect(result.current.isDeleteDialogOpen).toBe(false);
    expect(result.current.selected).toBeNull();
  });

  it("handleClose closes create/edit dialog", () => {
    const { result } = renderHook(() => useTeacherCrud(), { wrapper: createWrapper() });
    act(() => result.current.openCreate());
    expect(result.current.isDialogOpen).toBe(true);
    act(() => result.current.handleClose());
    expect(result.current.isDialogOpen).toBe(false);
  });

  it("handleCloseDelete closes delete dialog", () => {
    const { result } = renderHook(() => useTeacherCrud(), { wrapper: createWrapper() });
    act(() => result.current.openDelete(mockTeacher));
    expect(result.current.isDeleteDialogOpen).toBe(true);
    act(() => result.current.handleCloseDelete());
    expect(result.current.isDeleteDialogOpen).toBe(false);
  });
});
