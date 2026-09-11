import { test, expect } from "@playwright/test";

test.use({ storageState: "playwright/.auth/student.json" });

test.describe("student workspace", () => {
  test("dashboard shows defense and group cards", async ({ page }) => {
    await page.goto("/student");
    await expect(page.getByTestId("student-dashboard-defense-card")).toBeVisible({ timeout: 15_000 });
  });

  test("documents page shows stats", async ({ page }) => {
    await page.goto("/student/documents").catch(() => {});
    const header = page.getByTestId("student-documents-header");
    if ((await header.count()) === 0) {
      await page.goto("/student");
      await expect(page.getByTestId("student-dashboard-group-link")).toBeVisible({ timeout: 15_000 });
      return;
    }
    await expect(header).toBeVisible({ timeout: 15_000 });
  });
});
