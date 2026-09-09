import { renderHook, act } from "@testing-library/react";
import { describe, it, expect, vi, beforeEach } from "vitest";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { useStudentCrud } from "@/hooks/entities/use-student-crud";
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

const mockStudent = {
  id: 1,
  lastName: "Dupont",
  firstName: "Jean",
  email: "jean.dupont@example.com",
  cne: "CNE123",
  majorId: 1,
  levelId: 1,
  isActive: true,
  role: "student" as const,
};

describe("useStudentCrud", () => {
  beforeEach(() => {
    vi.clearAllMocks();
    localStorage.setItem("token", "fake-jwt-token");
  });

  it("initializes with default state", () => {
    const { result } = renderHook(() => useStudentCrud(), { wrapper: createWrapper() });
    expect(result.current.selected).toBeNull();
    expect(result.current.isDialogOpen).toBe(false);
    expect(result.current.isDeleteDialogOpen).toBe(false);
    expect(result.current.formData).toEqual({
      lastName: "", firstName: "", email: "", cne: "", majorId: "", levelId: "",
    });
  });

  it("openCreate resets form and opens dialog", () => {
    const { result } = renderHook(() => useStudentCrud(), { wrapper: createWrapper() });
    act(() => result.current.setFormData({ lastName: "test", firstName: "test", email: "t@t.com", cne: "X", majorId: "1", levelId: "1" }));
    act(() => result.current.openCreate());
    expect(result.current.selected).toBeNull();
    expect(result.current.isDialogOpen).toBe(true);
    expect(result.current.formData.lastName).toBe("");
  });

  it("openEdit populates form and opens dialog", () => {
    const { result } = renderHook(() => useStudentCrud(), { wrapper: createWrapper() });
    act(() => result.current.openEdit(mockStudent));
    expect(result.current.selected).toEqual(mockStudent);
    expect(result.current.isDialogOpen).toBe(true);
    expect(result.current.formData.lastName).toBe("Dupont");
    expect(result.current.formData.email).toBe("jean.dupont@example.com");
  });

  it("openDelete sets selected and opens delete dialog", () => {
    const { result } = renderHook(() => useStudentCrud(), { wrapper: createWrapper() });
    act(() => result.current.openDelete(mockStudent));
    expect(result.current.selected).toEqual(mockStudent);
    expect(result.current.isDeleteDialogOpen).toBe(true);
  });

  it("handleSubmit with invalid data does not proceed", async () => {
    const { result } = renderHook(() => useStudentCrud(), { wrapper: createWrapper() });
    act(() => result.current.openCreate());
    expect(result.current.isDialogOpen).toBe(true);
    const fakeEvent = { preventDefault: vi.fn() };
    await act(async () => {
      await result.current.handleSubmit(fakeEvent);
    });
    expect(fakeEvent.preventDefault).toHaveBeenCalled();
    expect(result.current.isDialogOpen).toBe(true);
  });

  it("handleSubmit with valid data creates a student", async () => {
    const { result } = renderHook(() => useStudentCrud(), { wrapper: createWrapper() });
    act(() => result.current.setFormData({
      lastName: "New", firstName: "Student", email: "new@example.com",
      cne: "CNE999", majorId: "1", levelId: "1",
    }));
    const fakeEvent = { preventDefault: vi.fn() };
    await act(async () => {
      await result.current.handleSubmit(fakeEvent);
    });
    expect(result.current.isDialogOpen).toBe(false);
    expect(result.current.selected).toBeNull();
    expect(result.current.formData).toEqual({
      lastName: "", firstName: "", email: "", cne: "", majorId: "", levelId: "",
    });
    expect(toast.success).toHaveBeenCalledWith("Étudiant créé avec succès");
  });

  it("handleSubmit with valid data updates an existing student", async () => {
    const { result } = renderHook(() => useStudentCrud(), { wrapper: createWrapper() });
    act(() => result.current.openEdit(mockStudent));
    act(() => result.current.setFormData({
      ...result.current.formData,
      lastName: "Updated",
    }));
    const fakeEvent = { preventDefault: vi.fn() };
    await act(async () => {
      await result.current.handleSubmit(fakeEvent);
    });
    expect(result.current.isDialogOpen).toBe(false);
    expect(result.current.selected).toBeNull();
    expect(toast.success).toHaveBeenCalledWith("Étudiant modifié avec succès");
  });

  it("handleDelete calls delete mutation", async () => {
    const { result } = renderHook(() => useStudentCrud(), { wrapper: createWrapper() });
    act(() => result.current.openDelete(mockStudent));
    await act(async () => {
      await result.current.handleDelete();
    });
    expect(result.current.isDeleteDialogOpen).toBe(false);
    expect(result.current.selected).toBeNull();
  });

  it("handleClose closes create/edit dialog", () => {
    const { result } = renderHook(() => useStudentCrud(), { wrapper: createWrapper() });
    act(() => result.current.openCreate());
    expect(result.current.isDialogOpen).toBe(true);
    act(() => result.current.handleClose());
    expect(result.current.isDialogOpen).toBe(false);
  });

  it("handleCloseDelete closes delete dialog", () => {
    const { result } = renderHook(() => useStudentCrud(), { wrapper: createWrapper() });
    act(() => result.current.openDelete(mockStudent));
    expect(result.current.isDeleteDialogOpen).toBe(true);
    act(() => result.current.handleCloseDelete());
    expect(result.current.isDeleteDialogOpen).toBe(false);
  });
});
