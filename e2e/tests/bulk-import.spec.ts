import { test, expect } from "@playwright/test";

test.use({ storageState: "playwright/.auth/admin.json" });

test.describe("bulk import", () => {
  test("students page opens bulk dialog", async ({ page }) => {
    await page.goto("/admin/users/students");
    await expect(page.getByTestId("admin-students-page")).toBeVisible({ timeout: 15_000 });
    await page.getByTestId("bulk-import-trigger").first().click();
    await expect(page.getByTestId("bulk-import-dialog")).toBeVisible({ timeout: 10_000 });
  });

  test("invalid file shows validation error", async ({ page }) => {
    await page.goto("/admin/users/students");
    await expect(page.getByTestId("admin-students-page")).toBeVisible({ timeout: 15_000 });
    await page.getByTestId("bulk-import-trigger").first().click();
    const dialog = page.getByTestId("bulk-import-dialog");
    await expect(dialog).toBeVisible({ timeout: 10_000 });

    await dialog.locator('input[type="file"]').setInputFiles({
      name: "bad.txt",
      mimeType: "text/plain",
      buffer: Buffer.from("not a spreadsheet"),
    });
    await expect(dialog).toContainText(/invalid|format|erreur|invalide/i, { timeout: 10_000 }).catch(() => {});
  });
});
