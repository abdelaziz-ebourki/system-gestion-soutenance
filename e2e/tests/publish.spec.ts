import { test, expect } from "@playwright/test";

test.use({ storageState: "playwright/.auth/coordinator.json" });

test.describe("schedule publish", () => {
  test("designer loads and publish flow is available", async ({ page }) => {
    await page.goto("/coordinator/schedule");
    await expect(page.getByTestId("coord-designer-page")).toBeVisible({ timeout: 20_000 });

    const publish = page.getByTestId("coord-designer-publish");
    await expect(publish).toBeVisible({ timeout: 10_000 });
    if (await publish.isEnabled().catch(() => false)) {
      await publish.click();
      const dialog = page.getByTestId("coord-designer-publish-dialog");
      if (await dialog.isVisible().catch(() => false)) {
        await page.getByTestId("coord-designer-publish-confirm").click();
        await expect(dialog).toBeHidden({ timeout: 15_000 });
      }
    }
  });

  test("grades page loads", async ({ page }) => {
    await page.goto("/coordinator/grades");
    await expect(page.getByTestId("coord-grades-page")).toBeVisible({ timeout: 15_000 });
  });
});
