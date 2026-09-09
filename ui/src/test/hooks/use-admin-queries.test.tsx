import { renderHook, act, waitFor } from "@testing-library/react";
import { describe, it, expect, beforeEach } from "vitest";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import {
  useAdminStats,
  useAuditLogs,
  useRooms,
  useCreateRoom,
  useUpdateRoom,
  useDeleteRoom,
  useDepartments,
  useCreateDepartment,
  useUpdateDepartment,
  useDeleteDepartment,
  useMajors,
  useCreateMajor,
  useUpdateMajor,
  useDeleteMajor,
  useLevels,
  useCreateLevel,
  useUpdateLevel,
  useDeleteLevel,
  useJuryRoleTemplates,
  useUsers,
  useStudents,
  useTeachers,
  useCoordinators,
  useTeachersList,
  useCreateUser,
  useUpdateUser,
  useDeleteUser,
} from "@/hooks/use-admin-queries";
import type { ReactNode } from "react";

beforeEach(() => {
  localStorage.setItem("token", "mock-jwt-token");
});

function createWrapper() {
  const qc = new QueryClient({
    defaultOptions: { queries: { retry: false }, mutations: { retry: false } },
  });
  return function Wrapper({ children }: { children: ReactNode }) {
    return <QueryClientProvider client={qc}>{children}</QueryClientProvider>;
  };
}

describe("useAdminStats", () => {
  it("returns stats data", async () => {
    const { result } = renderHook(() => useAdminStats(), { wrapper: createWrapper() });
    await waitFor(() => expect(result.current.isSuccess).toBe(true));
    expect(result.current.data).toBeDefined();
    expect(result.current.data).toHaveProperty("totalStudents");
  });
});

describe("useAuditLogs", () => {
  it("returns paginated audit logs", async () => {
    const { result } = renderHook(() => useAuditLogs(), { wrapper: createWrapper() });
    await waitFor(() => expect(result.current.isSuccess).toBe(true));
    expect(Array.isArray(result.current.data?.items)).toBe(true);
  });
});

describe("useRooms", () => {
  it("returns rooms data", async () => {
    const { result } = renderHook(() => useRooms(), { wrapper: createWrapper() });
    await waitFor(() => expect(result.current.isSuccess).toBe(true));
    expect(result.current.data).toHaveProperty("items");
  });
});

describe("useDepartments", () => {
  it("returns departments data", async () => {
    const { result } = renderHook(() => useDepartments(), { wrapper: createWrapper() });
    await waitFor(() => expect(result.current.isSuccess).toBe(true));
    expect(Array.isArray(result.current.data?.items)).toBe(true);
  });
});

describe("useMajors", () => {
  it("returns majors data", async () => {
    const { result } = renderHook(() => useMajors(), { wrapper: createWrapper() });
    await waitFor(() => expect(result.current.isSuccess).toBe(true));
    expect(Array.isArray(result.current.data?.items)).toBe(true);
  });
});

describe("useLevels", () => {
  it("returns levels data", async () => {
    const { result } = renderHook(() => useLevels(), { wrapper: createWrapper() });
    await waitFor(() => expect(result.current.isSuccess).toBe(true));
    expect(Array.isArray(result.current.data?.items)).toBe(true);
  });
});

describe("useJuryRoleTemplates", () => {
  it("returns jury role templates data", async () => {
    const { result } = renderHook(() => useJuryRoleTemplates(), { wrapper: createWrapper() });
    await waitFor(() => expect(result.current.isSuccess).toBe(true));
    expect(Array.isArray(result.current.data?.items)).toBe(true);
  });
});

describe("useUsers", () => {
  it("returns paginated users with params", async () => {
    const { result } = renderHook(() => useUsers({ role: "admin" }), { wrapper: createWrapper() });
    await waitFor(() => expect(result.current.isSuccess).toBe(true));
    expect(result.current.data).toHaveProperty("items");
  });
});

describe("useStudents", () => {
  it("returns paginated students", async () => {
    const { result } = renderHook(() => useStudents({ page: 0, limit: 10 }), { wrapper: createWrapper() });
    await waitFor(() => expect(result.current.isSuccess).toBe(true));
    expect(result.current.data).toHaveProperty("items");
  });
});

describe("useTeachers", () => {
  it("returns paginated teachers", async () => {
    const { result } = renderHook(() => useTeachers(), { wrapper: createWrapper() });
    await waitFor(() => expect(result.current.isSuccess).toBe(true));
    expect(result.current.data).toHaveProperty("items");
  });
});

describe("useCoordinators", () => {
  it("returns paginated coordinators", async () => {
    const { result } = renderHook(() => useCoordinators(), { wrapper: createWrapper() });
    await waitFor(() => expect(result.current.isSuccess).toBe(true));
    expect(result.current.data).toHaveProperty("items");
  });
});

describe("useTeachersList", () => {
  it("returns flat teachers array", async () => {
    const { result } = renderHook(() => useTeachersList(), { wrapper: createWrapper() });
    await waitFor(() => expect(result.current.isSuccess).toBe(true));
    expect(Array.isArray(result.current.data?.items)).toBe(true);
  });
});

describe("Admin mutations", () => {
  it("useCreateRoom creates a room", async () => {
    const { result } = renderHook(() => useCreateRoom(), { wrapper: createWrapper() });
    act(() => { result.current.mutate({ name: "Salle Test", capacity: 10, departmentId: 1 }); });
    await waitFor(() => expect(result.current.isSuccess).toBe(true));
  });

  it("useUpdateRoom updates a room", async () => {
    const { result } = renderHook(() => useUpdateRoom(), { wrapper: createWrapper() });
    act(() => { result.current.mutate({ id: 1, data: { name: "Updated", capacity: 25, departmentId: 1 } }); });
    await waitFor(() => expect(result.current.isSuccess).toBe(true));
  });

  it("useDeleteRoom deletes a room", async () => {
    const { result } = renderHook(() => useDeleteRoom(), { wrapper: createWrapper() });
    act(() => { result.current.mutate(1); });
    await waitFor(() => expect(result.current.isSuccess).toBe(true));
  });

  it("useCreateDepartment creates a department", async () => {
    const { result } = renderHook(() => useCreateDepartment(), { wrapper: createWrapper() });
    act(() => { result.current.mutate({ name: "Physics", code: "PHY" }); });
    await waitFor(() => expect(result.current.isSuccess).toBe(true));
  });

  it("useUpdateDepartment updates a department", async () => {
    const { result } = renderHook(() => useUpdateDepartment(), { wrapper: createWrapper() });
    act(() => { result.current.mutate({ id: 1, data: { name: "Updated", code: "UPD" } }); });
    await waitFor(() => expect(result.current.isSuccess).toBe(true));
  });

  it("useDeleteDepartment deletes a department", async () => {
    const { result } = renderHook(() => useDeleteDepartment(), { wrapper: createWrapper() });
    act(() => { result.current.mutate(1); });
    await waitFor(() => expect(result.current.isSuccess).toBe(true));
  });

  it("useCreateMajor creates a major", async () => {
    const { result } = renderHook(() => useCreateMajor(), { wrapper: createWrapper() });
    act(() => { result.current.mutate({ name: "AI" }); });
    await waitFor(() => expect(result.current.isSuccess).toBe(true));
  });

  it("useUpdateMajor updates a major", async () => {
    const { result } = renderHook(() => useUpdateMajor(), { wrapper: createWrapper() });
    act(() => { result.current.mutate({ id: 1, data: { name: "Updated" } }); });
    await waitFor(() => expect(result.current.isSuccess).toBe(true));
  });

  it("useDeleteMajor deletes a major", async () => {
    const { result } = renderHook(() => useDeleteMajor(), { wrapper: createWrapper() });
    act(() => { result.current.mutate(1); });
    await waitFor(() => expect(result.current.isSuccess).toBe(true));
  });

  it("useCreateLevel creates a level", async () => {
    const { result } = renderHook(() => useCreateLevel(), { wrapper: createWrapper() });
    act(() => { result.current.mutate({ name: "M2" }); });
    await waitFor(() => expect(result.current.isSuccess).toBe(true));
  });

  it("useUpdateLevel updates a level", async () => {
    const { result } = renderHook(() => useUpdateLevel(), { wrapper: createWrapper() });
    act(() => { result.current.mutate({ id: 1, data: { name: "Updated" } }); });
    await waitFor(() => expect(result.current.isSuccess).toBe(true));
  });

  it("useDeleteLevel deletes a level", async () => {
    const { result } = renderHook(() => useDeleteLevel(), { wrapper: createWrapper() });
    act(() => { result.current.mutate(1); });
    await waitFor(() => expect(result.current.isSuccess).toBe(true));
  });

  it("useCreateUser creates a user", async () => {
    const { result } = renderHook(() => useCreateUser(), { wrapper: createWrapper() });
    act(() => { result.current.mutate({ lastName: "Test", firstName: "User", email: "test@test.com", role: "teacher" }); });
    await waitFor(() => expect(result.current.isSuccess).toBe(true));
  });

  it("useUpdateUser updates a user", async () => {
    const { result } = renderHook(() => useUpdateUser(), { wrapper: createWrapper() });
    act(() => { result.current.mutate({ id: 1, data: { lastName: "Updated", firstName: "User", email: "updated@test.com", role: "teacher" } }); });
    await waitFor(() => expect(result.current.isSuccess).toBe(true));
  });

  it("useDeleteUser deletes a user", async () => {
    const { result } = renderHook(() => useDeleteUser(), { wrapper: createWrapper() });
    act(() => { result.current.mutate(1); });
    await waitFor(() => expect(result.current.isSuccess).toBe(true));
  });

});
