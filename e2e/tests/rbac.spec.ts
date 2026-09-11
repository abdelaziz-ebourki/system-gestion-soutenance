import { test, expect } from "@playwright/test";

test.describe("rbac", () => {
  test("unauthenticated /admin bounces to /login", async ({ page }) => {
    await page.goto("/admin");
    await expect(page).toHaveURL(/\/login/, { timeout: 10_000 });
  });

  test("unauthenticated /coordinator bounces to /login", async ({ page }) => {
    await page.goto("/coordinator");
    await expect(page).toHaveURL(/\/login/, { timeout: 10_000 });
  });

  test("student cannot open /admin", async ({ browser }) => {
    const context = await browser.newContext({
      storageState: "playwright/.auth/student.json",
    });
    const page = await context.newPage();
    await page.goto("/admin");
    await expect(page).toHaveURL(/\/login/, { timeout: 10_000 });
    await expect(page.getByTestId("admin-content")).toHaveCount(0);
    await context.close();
  });

  test("teacher cannot open /coordinator", async ({ browser }) => {
    const context = await browser.newContext({
      storageState: "playwright/.auth/teacher.json",
    });
    const page = await context.newPage();
    await page.goto("/coordinator");
    await expect(page).toHaveURL(/\/login/, { timeout: 10_000 });
    await context.close();
  });
});
