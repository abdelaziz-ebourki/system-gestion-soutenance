import { HttpResponse, delay as mswDelay } from "msw";
import type { HttpHandler } from "msw";

export function withDelay<T extends HttpHandler>(handler: T, ms = 300): T {
  return {
    ...handler,
    resolver: async (...args: Parameters<NonNullable<typeof handler.run>>) => {
      await mswDelay(ms);
      return handler.run(...args);
    },
  } as unknown as T;
}

export function errorResponse(
  message = "Erreur serveur",
  status: number = 500,
) {
  return HttpResponse.json({ message }, { status });
}

export function paginatedResponse<T>(
  items: T[],
  total?: number,
  page?: number,
) {
  const pageSize = 10;
  const actualPage = page ?? 0;
  const actualTotal = total ?? items.length;
  const start = actualPage * pageSize;
  const pagedItems = items.slice(start, start + pageSize);
  return HttpResponse.json({
    items: pagedItems,
    total: actualTotal,
    pageCount: Math.ceil(actualTotal / pageSize),
  });
}
