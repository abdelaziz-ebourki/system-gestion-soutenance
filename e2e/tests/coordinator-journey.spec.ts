import { test, expect } from "@playwright/test";

test.use({ storageState: "playwright/.auth/coordinator.json" });

test.describe("coordinator journey", () => {
  test("dashboard loads with planner entry", async ({ page }) => {
    await page.goto("/coordinator");
    await expect(page.getByTestId("coord-dashboard-page")).toBeVisible({ timeout: 15_000 });
    await expect(page.getByTestId("coord-dashboard-open-planner")).toBeVisible();
  });

  test("projects page lists content and opens create dialog", async ({ page }) => {
    await page.goto("/coordinator/projects");
    await expect(page.getByTestId("coord-projects-page")).toBeVisible({ timeout: 15_000 });
    await page.getByTestId("coord-projects-add-button").click();
    await expect(page.getByTestId("coord-project-dialog")).toBeVisible({ timeout: 10_000 });
    await expect(page.getByTestId("coord-project-dialog-title")).toBeVisible();
    await page.getByTestId("coord-project-dialog-cancel").click();
  });

  test("defense sessions page opens create dialog with all inputs", async ({ page }) => {
    await page.goto("/coordinator/defense-sessions");
    await expect(page.getByTestId("coord-sessions-page")).toBeVisible({ timeout: 15_000 });
    await page.getByTestId("coord-sessions-add-button").click();
    await expect(page.getByTestId("coord-sessions-dialog")).toBeVisible({ timeout: 10_000 });
    await expect(page.getByTestId("coord-sessions-input-name")).toBeVisible();
    await expect(page.getByTestId("coord-sessions-dialog-submit")).toBeVisible();
    await page.getByTestId("coord-sessions-dialog-cancel").click();
  });

  test("documents page shows all five document cards", async ({ page }) => {
    await page.goto("/coordinator/documents");
    await expect(page.getByTestId("coord-documents-page")).toBeVisible({ timeout: 15_000 });
    for (const doc of ["evaluation-sheet", "proces-verbal", "attendance-list", "jury-convocation", "schedule"]) {
      await expect(page.getByTestId(`coord-documents-card-${doc}`)).toBeVisible();
    }
  });
});
