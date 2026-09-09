import { http, HttpResponse } from "msw";
import type { HttpHandler } from "msw";
import { DEFAULT_API_LIMIT } from "@/lib/constants";

let _nextId = 100;
function nextId(): number {
  return _nextId++;
}

export function mockJson(method: "get" | "post" | "put" | "delete" | "patch", url: string, data: unknown): HttpHandler {
  return http[method](url, () => HttpResponse.json(data as never));
}

export function mockPaginated(url: string, items: unknown[], total?: number): HttpHandler[] {
  const actualTotal = total ?? items.length;
  const pageCount = actualTotal > 0 ? Math.ceil(actualTotal / DEFAULT_API_LIMIT) : 1;
  return [
    http.get(url, () =>
      HttpResponse.json({ items, total: actualTotal, pageCount, currentPage: 0, size: DEFAULT_API_LIMIT } as never),
    ),
  ];
}

export function mockCrud(baseUrl: string): HttpHandler[] {
  return [
    http.post(baseUrl, async ({ request }) => {
      const body = await request.json();
      return HttpResponse.json(
        { id: nextId(), ...(body as Record<string, unknown>) } as never,
        { status: 201 },
      );
    }),
    http.put(`${baseUrl}/:id`, async ({ request }) => {
      const body = await request.json();
      return HttpResponse.json(body as never);
    }),
    http.delete(`${baseUrl}/:id`, () => new HttpResponse(null, { status: 204 })),
  ];
}

export function mockEcho(method: "post" | "put" | "patch", url: string): HttpHandler {
  return http[method](url, async ({ request }) => {
    try {
      const body = await request.json();
      return HttpResponse.json(body as never);
    } catch {
      return HttpResponse.json({} as never);
    }
  });
}
