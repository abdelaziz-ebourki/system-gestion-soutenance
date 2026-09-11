import { test, expect } from "@playwright/test";

test.use({ storageState: "playwright/.auth/teacher.json" });

test.describe("teacher evaluations", () => {
  test("evaluations page loads with stats", async ({ page }) => {
    await page.goto("/teacher/evaluations");
    await expect(page.getByTestId("teacher-evaluations-header")).toBeVisible({ timeout: 15_000 });
  });

  test("pending evaluation opens the grading dialog", async ({ page }) => {
    await page.goto("/teacher/evaluations");
    await expect(page.getByTestId("teacher-evaluations-header")).toBeVisible({ timeout: 15_000 });
    const firstPending = page
      .locator("[data-testid^='teacher-evaluations-pending-btn-']")
      .first();
    if ((await firstPending.count()) === 0) {
      test.skip(true, "no pending evaluations in seed data");
      return;
    }
    await firstPending.click();
    await expect(page.getByTestId("teacher-evaluations-dialog")).toBeVisible({ timeout: 10_000 });
    await expect(page.getByTestId("teacher-evaluations-score")).toBeVisible();
  });
});
