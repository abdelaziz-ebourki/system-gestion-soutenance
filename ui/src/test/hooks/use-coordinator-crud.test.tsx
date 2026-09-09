import { renderHook, act } from "@testing-library/react";
import { describe, it, expect, vi, beforeEach } from "vitest";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { useCoordinatorCrud } from "@/hooks/entities/use-coordinator-crud";
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

const mockCoordinator = {
  id: 1,
  lastName: "Idrissi",
  firstName: "Hassan",
  email: "hassan.idrissi@example.com",
  isActive: true,
  role: "coordinator" as const,
};

describe("useCoordinatorCrud", () => {
  beforeEach(() => {
    vi.clearAllMocks();
    localStorage.setItem("token", "fake-jwt-token");
  });

  it("initializes with default state", () => {
    const { result } = renderHook(() => useCoordinatorCrud(), { wrapper: createWrapper() });
    expect(result.current.selected).toBeNull();
    expect(result.current.isDialogOpen).toBe(false);
    expect(result.current.isDeleteDialogOpen).toBe(false);
    expect(result.current.formData).toEqual({ lastName: "", firstName: "", email: "" });
  });

  it("openCreate resets form and opens dialog", () => {
    const { result } = renderHook(() => useCoordinatorCrud(), { wrapper: createWrapper() });
    act(() => result.current.setFormData({ lastName: "test", firstName: "test", email: "t@t.com" }));
    act(() => result.current.openCreate());
    expect(result.current.selected).toBeNull();
    expect(result.current.isDialogOpen).toBe(true);
    expect(result.current.formData.lastName).toBe("");
  });

  it("openEdit populates form and opens dialog", () => {
    const { result } = renderHook(() => useCoordinatorCrud(), { wrapper: createWrapper() });
    act(() => result.current.openEdit(mockCoordinator));
    expect(result.current.selected).toEqual(mockCoordinator);
    expect(result.current.isDialogOpen).toBe(true);
    expect(result.current.formData.lastName).toBe("Idrissi");
    expect(result.current.formData.email).toBe("hassan.idrissi@example.com");
  });

  it("openDelete sets selected and opens delete dialog", () => {
    const { result } = renderHook(() => useCoordinatorCrud(), { wrapper: createWrapper() });
    act(() => result.current.openDelete(mockCoordinator));
    expect(result.current.selected).toEqual(mockCoordinator);
    expect(result.current.isDeleteDialogOpen).toBe(true);
  });

  it("handleSubmit with invalid data does not proceed", async () => {
    const { result } = renderHook(() => useCoordinatorCrud(), { wrapper: createWrapper() });
    act(() => result.current.openCreate());
    expect(result.current.isDialogOpen).toBe(true);
    const fakeEvent = { preventDefault: vi.fn() };
    await act(async () => {
      await result.current.handleSubmit(fakeEvent);
    });
    expect(fakeEvent.preventDefault).toHaveBeenCalled();
    expect(result.current.isDialogOpen).toBe(true);
  });

  it("handleSubmit with valid data creates a coordinator", async () => {
    const { result } = renderHook(() => useCoordinatorCrud(), { wrapper: createWrapper() });
    act(() => {
      result.current.setFormData({ lastName: "New", firstName: "Coord", email: "new@test.com" });
    });
    const fakeEvent = { preventDefault: vi.fn() };
    await act(async () => {
      await result.current.handleSubmit(fakeEvent);
    });
    expect(result.current.isDialogOpen).toBe(false);
    expect(result.current.selected).toBeNull();
    expect(result.current.formData).toEqual({ lastName: "", firstName: "", email: "" });
    expect(toast.success).toHaveBeenCalledWith("Coordinateur ajouté avec succès");
  });

  it("handleSubmit with valid data updates a coordinator", async () => {
    const { result } = renderHook(() => useCoordinatorCrud(), { wrapper: createWrapper() });
    act(() => result.current.openEdit(mockCoordinator));
    act(() => {
      result.current.setFormData({ lastName: "Updated", firstName: "Coord", email: "updated@test.com" });
    });
    const fakeEvent = { preventDefault: vi.fn() };
    await act(async () => {
      await result.current.handleSubmit(fakeEvent);
    });
    expect(result.current.isDialogOpen).toBe(false);
    expect(result.current.selected).toBeNull();
    expect(toast.success).toHaveBeenCalledWith("Coordinateur modifié avec succès");
  });

  it("handleDelete deletes a coordinator", async () => {
    const { result } = renderHook(() => useCoordinatorCrud(), { wrapper: createWrapper() });
    act(() => result.current.openDelete(mockCoordinator));
    await act(async () => {
      await result.current.handleDelete();
    });
    expect(result.current.isDeleteDialogOpen).toBe(false);
    expect(result.current.selected).toBeNull();
  });

  it("handleClose closes create/edit dialog", () => {
    const { result } = renderHook(() => useCoordinatorCrud(), { wrapper: createWrapper() });
    act(() => result.current.openCreate());
    expect(result.current.isDialogOpen).toBe(true);
    act(() => result.current.handleClose());
    expect(result.current.isDialogOpen).toBe(false);
  });

  it("handleCloseDelete closes delete dialog", () => {
    const { result } = renderHook(() => useCoordinatorCrud(), { wrapper: createWrapper() });
    act(() => result.current.openDelete(mockCoordinator));
    expect(result.current.isDeleteDialogOpen).toBe(true);
    act(() => result.current.handleCloseDelete());
    expect(result.current.isDeleteDialogOpen).toBe(false);
  });
});
