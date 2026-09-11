import { describe, it, expect, vi, beforeEach, afterEach, type Mock } from "vitest";
import { api } from "@/lib/api-core";

function jsonResponse(body: unknown, status = 200) {
  return {
    ok: status >= 200 && status < 300,
    status,
    json: () => Promise.resolve(body),
    text: () => Promise.resolve(JSON.stringify(body)),
    headers: new Headers({ "content-type": "application/json" }),
  } as Response;
}

describe("API silent refresh", () => {
  beforeEach(() => {
    vi.stubGlobal("fetch", vi.fn());
  });

  afterEach(() => {
    vi.unstubAllGlobals();
    vi.restoreAllMocks();
  });

  it("retries once after a successful refresh without dispatching auth:expired", async () => {
    const dispatch = vi.spyOn(window, "dispatchEvent");
    const mockFetch = fetch as Mock;
    mockFetch
      .mockResolvedValueOnce(jsonResponse({ message: "expired" }, 401))
      .mockResolvedValueOnce(jsonResponse({}, 200))
      .mockResolvedValueOnce(jsonResponse({ success: true, data: { id: 1 } }, 200));

    const promise = api<{ id: number }>("/student/stats");
    const result = await promise;

    expect(result).toEqual({ id: 1 });
    expect(mockFetch).toHaveBeenCalledTimes(3);
    expect(mockFetch.mock.calls[1]?.[0]).toContain("/auth/refresh");
    expect(dispatch).not.toHaveBeenCalled();
  });

  it("dispatches auth:expired and throws when refresh fails", async () => {
    const dispatch = vi.spyOn(window, "dispatchEvent");
    const mockFetch = fetch as Mock;
    mockFetch
      .mockResolvedValueOnce(jsonResponse({ message: "expired" }, 401))
      .mockResolvedValueOnce(jsonResponse({ message: "invalid" }, 401));

    const promise = api("/student/stats");
    await expect(promise).rejects.toMatchObject({ status: 401 });

    expect(mockFetch).toHaveBeenCalledTimes(2);
    expect(dispatch).toHaveBeenCalledTimes(1);
    expect(dispatch.mock.calls[0]?.[0]).toBeInstanceOf(CustomEvent);
    expect((dispatch.mock.calls[0]?.[0] as CustomEvent).type).toBe("auth:expired");
  });

  it("does not refresh auth endpoints", async () => {
    const dispatch = vi.spyOn(window, "dispatchEvent");
    const mockFetch = fetch as Mock;
    mockFetch.mockResolvedValueOnce(jsonResponse({ message: "bad" }, 401));

    const promise = api("/auth/login", { method: "POST", body: "{}" });
    await expect(promise).rejects.toMatchObject({ status: 401 });

    expect(mockFetch).toHaveBeenCalledTimes(1);
    expect(dispatch).not.toHaveBeenCalled();
  });

  it("coalesces concurrent 401s into a single refresh", async () => {
    const dispatch = vi.spyOn(window, "dispatchEvent");
    const mockFetch = fetch as Mock;
    mockFetch.mockImplementation((url: string) => {
      if (String(url).includes("/auth/refresh")) {
        return Promise.resolve(jsonResponse({}, 200));
      }
      if (mockFetch.mock.calls.filter((c) => !String(c[0]).includes("/auth/refresh")).length <= 2) {
        return Promise.resolve(jsonResponse({ message: "expired" }, 401));
      }
      return Promise.resolve(jsonResponse({ success: true, data: {} }, 200));
    });

    const [r1, r2] = await Promise.all([api("/a"), api("/b")].map((p) => p.then(
      (v) => ({ ok: true as const, v }),
      (e) => ({ ok: false as const, e }),
    )));

    const refreshCalls = mockFetch.mock.calls.filter((c) => String(c[0]).includes("/auth/refresh"));
    expect(refreshCalls).toHaveLength(1);
    expect(dispatch).not.toHaveBeenCalled();
    expect(r1.ok && r2.ok).toBe(true);
  });
});
