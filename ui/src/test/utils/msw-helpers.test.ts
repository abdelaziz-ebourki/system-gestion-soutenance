import { describe, it, expect } from "vitest";
import { errorResponse, paginatedResponse } from "./msw-helpers";

describe("errorResponse", () => {
  it("returns a 500 response with default message", async () => {
    const res = errorResponse();
    expect(res.status).toBe(500);
    const body = await res.json();
    expect(body).toEqual({ message: "Erreur serveur" });
  });

  it("returns a custom status and message", async () => {
    const res = errorResponse("Not found", 404);
    expect(res.status).toBe(404);
    const body = await res.json();
    expect(body).toEqual({ message: "Not found" });
  });
});

describe("paginatedResponse", () => {
  const items = [{ id: 1 }, { id: 2 }, { id: 3 }];

  it("returns all items when total matches length", async () => {
    const res = paginatedResponse(items);
    expect(res.status).toBe(200);
    const body = await res.json();
    expect(body.items).toEqual(items);
    expect(body.total).toBe(3);
    expect(body.pageCount).toBe(1);
  });

  it("returns paginated items for a given page", async () => {
    const manyItems = Array.from({ length: 25 }, (_, i) => ({ id: String(i + 1) }));
    const res = paginatedResponse(manyItems, 25, 1);
    const body = await res.json();
    expect(body.items).toHaveLength(10);
    expect(body.items[0].id).toBe("11");
    expect(body.total).toBe(25);
    expect(body.pageCount).toBe(3);
  });

  it("uses explicit total when provided", async () => {
    const res = paginatedResponse(items, 50);
    const body = await res.json();
    expect(body.items).toEqual(items);
    expect(body.total).toBe(50);
    expect(body.pageCount).toBe(5);
  });
});
