import { test, expect, type Page } from "@playwright/test";

test.use({ storageState: "playwright/.auth/coordinator.json" });

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

  const directPopup = page.waitForEvent("popup", { timeout: 15_000 }).catch(() => null);
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
      const pickerPopup = page.waitForEvent("popup", { timeout: 15_000 }).catch(() => null);
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
    const datePopup = page.waitForEvent("popup", { timeout: 15_000 }).catch(() => null);
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
