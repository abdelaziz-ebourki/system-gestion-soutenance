import { test, expect } from "@playwright/test";

test.use({ storageState: "playwright/.auth/student.json" });

test.describe("notifications", () => {
  test("notifications page loads", async ({ page }) => {
    await page.goto("/notifications");
    await expect(page.getByTestId("notifications-header")).toBeVisible({ timeout: 15_000 });
    await expect(page.getByTestId("notifications-card")).toBeVisible();
  });

  test("mark all read clears unread state when present", async ({ page }) => {
    await page.goto("/notifications");
    await expect(page.getByTestId("notifications-header")).toBeVisible({ timeout: 15_000 });
    const markAll = page.getByTestId("notifications-mark-all-read");
    if (await markAll.isVisible().catch(() => false)) {
      await markAll.click();
      await expect(markAll).toBeHidden({ timeout: 15_000 });
    }
  });
});
