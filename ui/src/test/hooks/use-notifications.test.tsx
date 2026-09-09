import { renderHook, act, waitFor } from "@testing-library/react";
import { describe, it, expect } from "vitest";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import {
  useNotifications,
  useUnreadCount,
  useMarkNotificationRead,
  useMarkAllNotificationsRead,
} from "@/hooks/use-notifications";
import type { ReactNode } from "react";

function createWrapper() {
  const qc = new QueryClient({
    defaultOptions: { queries: { retry: false }, mutations: { retry: false } },
  });
  return function Wrapper({ children }: { children: ReactNode }) {
    return <QueryClientProvider client={qc}>{children}</QueryClientProvider>;
  };
}

describe("useNotifications", () => {
  it("returns notifications data", async () => {
    const { result } = renderHook(() => useNotifications(), { wrapper: createWrapper() });
    await waitFor(() => expect(result.current.isSuccess).toBe(true));
    expect(Array.isArray(result.current.data?.items)).toBe(true);
  });
});

describe("useUnreadCount", () => {
  it("returns count of unread notifications", async () => {
    const { result } = renderHook(() => useUnreadCount(), { wrapper: createWrapper() });
    await waitFor(() => expect(result.current).toBe(1));
  });
});

describe("useMarkNotificationRead", () => {
  it("mutates and invalidates query", async () => {
    const { result } = renderHook(() => useMarkNotificationRead(), { wrapper: createWrapper() });
    act(() => { result.current.mutate(1); });
    await waitFor(() => expect(result.current.isSuccess).toBe(true));
  });
});

describe("useMarkAllNotificationsRead", () => {
  it("mutates and invalidates query", async () => {
    const { result } = renderHook(() => useMarkAllNotificationsRead(), { wrapper: createWrapper() });
    act(() => { result.current.mutate(); });
    await waitFor(() => expect(result.current.isSuccess).toBe(true));
  });
});
