import { renderHook, act } from "@testing-library/react";
import { describe, it, expect, vi, beforeEach } from "vitest";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { useRoomCrud } from "@/hooks/entities/use-room-crud";
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

const mockRoom = {
  id: 1,
  name: "Salle 101",
  capacity: 30,
  departmentId: 1,
};

describe("useRoomCrud", () => {
  beforeEach(() => {
    vi.clearAllMocks();
    localStorage.setItem("token", "fake-jwt-token");
  });

  it("initializes with default state", () => {
    const { result } = renderHook(() => useRoomCrud(), { wrapper: createWrapper() });
    expect(result.current.selected).toBeNull();
    expect(result.current.isDialogOpen).toBe(false);
    expect(result.current.isDeleteDialogOpen).toBe(false);
    expect(result.current.formData).toEqual({ name: "", capacity: 0, departmentId: "" });
  });

  it("openCreate resets form and opens dialog", () => {
    const { result } = renderHook(() => useRoomCrud(), { wrapper: createWrapper() });
    act(() => result.current.openCreate());
    expect(result.current.selected).toBeNull();
    expect(result.current.isDialogOpen).toBe(true);
    expect(result.current.formData.name).toBe("");
    expect(result.current.formData.capacity).toBe(0);
  });

  it("openEdit populates form and opens dialog", () => {
    const { result } = renderHook(() => useRoomCrud(), { wrapper: createWrapper() });
    act(() => result.current.openEdit(mockRoom));
    expect(result.current.selected).toEqual(mockRoom);
    expect(result.current.isDialogOpen).toBe(true);
    expect(result.current.formData.name).toBe("Salle 101");
    expect(result.current.formData.capacity).toBe(30);
    expect(result.current.formData.departmentId).toBe("1");
  });

  it("openDelete sets selected and opens delete dialog", () => {
    const { result } = renderHook(() => useRoomCrud(), { wrapper: createWrapper() });
    act(() => result.current.openDelete(mockRoom));
    expect(result.current.selected).toEqual(mockRoom);
    expect(result.current.isDeleteDialogOpen).toBe(true);
  });

  it("handleSubmit with invalid data does not proceed", async () => {
    const { result } = renderHook(() => useRoomCrud(), { wrapper: createWrapper() });
    act(() => result.current.openCreate());
    expect(result.current.isDialogOpen).toBe(true);
    const fakeEvent = { preventDefault: vi.fn() };
    await act(async () => {
      await result.current.handleSubmit(fakeEvent);
    });
    expect(fakeEvent.preventDefault).toHaveBeenCalled();
    expect(result.current.isDialogOpen).toBe(true);
  });

  it("handleSubmit with valid data creates a room", async () => {
    const { result } = renderHook(() => useRoomCrud(), { wrapper: createWrapper() });
    act(() => {
      result.current.setFormData({ name: "New Room", capacity: 20, departmentId: "1" });
    });
    const fakeEvent = { preventDefault: vi.fn() };
    await act(async () => {
      await result.current.handleSubmit(fakeEvent);
    });
    expect(result.current.isDialogOpen).toBe(false);
    expect(result.current.selected).toBeNull();
    expect(result.current.formData).toEqual({ name: "", capacity: 0, departmentId: "" });
    expect(toast.success).toHaveBeenCalledWith("Salle ajoutée avec succès");
  });

  it("handleSubmit with valid data updates a room", async () => {
    const { result } = renderHook(() => useRoomCrud(), { wrapper: createWrapper() });
    act(() => result.current.openEdit(mockRoom));
    act(() => {
      result.current.setFormData({ name: "Updated Room", capacity: 25, departmentId: "1" });
    });
    const fakeEvent = { preventDefault: vi.fn() };
    await act(async () => {
      await result.current.handleSubmit(fakeEvent);
    });
    expect(result.current.isDialogOpen).toBe(false);
    expect(result.current.selected).toBeNull();
    expect(toast.success).toHaveBeenCalledWith("Salle modifiée avec succès");
  });

  it("handleDelete deletes a room", async () => {
    const { result } = renderHook(() => useRoomCrud(), { wrapper: createWrapper() });
    act(() => result.current.openDelete(mockRoom));
    await act(async () => {
      await result.current.handleDelete();
    });
    expect(result.current.isDeleteDialogOpen).toBe(false);
    expect(result.current.selected).toBeNull();
  });

  it("handleClose closes create/edit dialog", () => {
    const { result } = renderHook(() => useRoomCrud(), { wrapper: createWrapper() });
    act(() => result.current.openCreate());
    expect(result.current.isDialogOpen).toBe(true);
    act(() => result.current.handleClose());
    expect(result.current.isDialogOpen).toBe(false);
  });

  it("handleCloseDelete closes delete dialog", () => {
    const { result } = renderHook(() => useRoomCrud(), { wrapper: createWrapper() });
    act(() => result.current.openDelete(mockRoom));
    expect(result.current.isDeleteDialogOpen).toBe(true);
    act(() => result.current.handleCloseDelete());
    expect(result.current.isDeleteDialogOpen).toBe(false);
  });
});
