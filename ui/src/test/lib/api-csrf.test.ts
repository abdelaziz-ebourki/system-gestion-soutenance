import { describe, it, expect, vi, beforeEach, afterEach, type Mock } from "vitest";
import { api } from "@/lib/api-core";

describe.skip("API Wrapper CSRF & Cookies", () => {
  beforeEach(() => {
    vi.stubGlobal("fetch", vi.fn());
    localStorage.clear();
    // Clear cookies
    document.cookie = "XSRF-TOKEN=; expires=Thu, 01 Jan 1970 00:00:00 UTC; path=/;";
  });

  afterEach(() => {
    vi.unstubAllGlobals();
  });

  it("should add X-XSRF-TOKEN header for mutating requests when cookie is present", async () => {
    document.cookie = "XSRF-TOKEN=test-token";
    
    const mockFetch = fetch as Mock;
    mockFetch.mockResolvedValue({
      ok: true,
      status: 200,
      json: () => Promise.resolve({ success: true }),
      headers: new Headers({ "content-type": "application/json" }),
    } as Response);

    await api("/test", { method: "POST", body: JSON.stringify({ data: "test" }) });

    expect(mockFetch).toHaveBeenCalled();
    const fetchCall = mockFetch.mock.calls[0];
    const options = fetchCall?.[1];
    const headers = options?.headers as Record<string, string> | undefined;

    expect(headers).toBeDefined();
    if (headers) {
      expect(headers["X-XSRF-TOKEN"]).toBe("test-token");
    }
    expect(options?.credentials).toBe("include");
  });

  it("should NOT add X-XSRF-TOKEN header for GET requests", async () => {
    document.cookie = "XSRF-TOKEN=test-token";
    
    const mockFetch = fetch as Mock;
    mockFetch.mockResolvedValue({
      ok: true,
      status: 200,
      json: () => Promise.resolve({ success: true }),
      headers: new Headers({ "content-type": "application/json" }),
    } as Response);
    
    await api("/test", { method: "GET" });
    
    expect(mockFetch).toHaveBeenCalled();
    const fetchCall = mockFetch.mock.calls[0];
    const options = fetchCall?.[1];
    const headers = options?.headers as Record<string, string> | undefined;
    
    expect(headers).toBeDefined();
    if (headers) {
      expect(headers["X-XSRF-TOKEN"]).toBeUndefined();
    }
    expect(options?.credentials).toBe("include");
  });
});
