import { test, expect, type Page, type APIRequestContext, request } from "@playwright/test";

test.use({ storageState: "playwright/.auth/coordinator.json" });

const API_URL = process.env.API_URL || "http://localhost:8080";

async function apiContext(): Promise<APIRequestContext> {
  return request.newContext({
    baseURL: API_URL,
    storageState: "playwright/.auth/coordinator.json",
  });
}

async function closePopupIfAny(popupPromise: Promise<unknown>) {
  const popup = (await popupPromise.catch(() => null)) as { close?: () => Promise<void> } | null;
  if (popup?.close) {
    await popup.close().catch(() => {});
    return true;
  }
  return false;
}

async function triggerPdfFromCard(page: Page, cardId: string) {
  await page.goto("/coordinator/documents");
  await expect(page.getByTestId("coord-documents-page")).toBeVisible({ timeout: 15_000 });
  const card = page.getByTestId(`coord-documents-card-${cardId}`);
  await expect(card).toBeVisible();

  const directPopup = page.waitForEvent("popup", { timeout: 8_000 }).catch(() => null);
  await card.getByRole("button").first().click();

  // Direct-download cards (schedule) open the PDF immediately.
  if (await closePopupIfAny(directPopup)) {
    return;
  }

  // Picker-based cards: choose the first project.
  const picker = page.getByTestId("coord-documents-picker-dialog");
  if (await picker.isVisible().catch(() => false)) {
    const firstItem = page
      .getByTestId("coord-documents-picker-list")
      .locator("[data-testid^='coord-documents-picker-item-']")
      .first();
    if ((await firstItem.count()) > 0) {
      const pickerPopup = page.waitForEvent("popup", { timeout: 8_000 }).catch(() => null);
      await firstItem.click();
      await closePopupIfAny(pickerPopup);
      return;
    }
    await page.getByTestId("coord-documents-picker-cancel").click();
    test.skip(true, "no projects available for picker-based PDF");
    return;
  }

  // Date-based card (attendance-list): fill a date and generate.
  const dateDialog = page.getByTestId("coord-documents-date-dialog");
  if (await dateDialog.isVisible().catch(() => false)) {
    await page.getByTestId("coord-documents-date-input").fill("2026-06-15");
    const datePopup = page.waitForEvent("popup", { timeout: 8_000 }).catch(() => null);
    await page.getByTestId("coord-documents-date-generate").click();
    await closePopupIfAny(datePopup);
  }
}

test.describe("coordinator documents (PDF)", () => {
  for (const doc of ["schedule", "evaluation-sheet", "proces-verbal", "attendance-list", "jury-convocation"]) {
    test(`opens PDF flow for ${doc}`, async ({ page }) => {
      await triggerPdfFromCard(page, doc);
    });
  }
});

test.describe("coordinator documents API (PDF bytes)", () => {
  test("all five PDF endpoints render non-empty PDFs", async () => {
    const ctx = await apiContext();
    try {
      const projectsRes = await ctx.get("/api/coordinator/projects?page=0&limit=5");
      expect(projectsRes.ok()).toBeTruthy();
      const projectsBody = await projectsRes.json();
      const projects = projectsBody.data?.items ?? projectsBody.items ?? [];
      test.skip(projects.length === 0, "no projects in seed data");

      const sessionsRes = await ctx.get("/api/coordinator/defense-sessions?page=0&limit=5");
      expect(sessionsRes.ok()).toBeTruthy();
      const sessionsBody = await sessionsRes.json();
      const sessions = sessionsBody.data?.items ?? sessionsBody.items ?? [];
      test.skip(sessions.length === 0, "no sessions in seed data");

      const projectId = projects[0].id as number;
      const sessionId = sessions[0].id as number;

      const cases: Array<{ name: string; path: string; body: unknown }> = [
        { name: "evaluation-sheet", path: "/api/coordinator/documents/evaluation-sheets/pdf", body: { projectId } },
        { name: "proces-verbal", path: "/api/coordinator/documents/proces-verbal/pdf", body: { projectId } },
        { name: "jury-convocation", path: "/api/coordinator/documents/jury-convocations/pdf", body: { projectId } },
        { name: "schedule", path: "/api/coordinator/documents/schedule/pdf", body: { defenseSessionId: sessionId } },
        {
          name: "attendance-list",
          path: "/api/coordinator/documents/attendance-lists/pdf",
          body: { defenseSessionId: sessionId },
        },
      ];

      for (const c of cases) {
        const res = await ctx.post(c.path, { data: c.body });
        expect(res.ok(), `${c.name} returned ${res.status()}`).toBeTruthy();
        expect(res.headers()["content-type"]).toContain("application/pdf");
        const bytes = await res.body();
        expect(bytes.length, `${c.name} is empty`).toBeGreaterThan(500);
        expect(bytes.subarray(0, 4).toString(), `${c.name} is not a PDF`).toBe("%PDF");
      }
    } finally {
      await ctx.dispose();
    }
  });
});
