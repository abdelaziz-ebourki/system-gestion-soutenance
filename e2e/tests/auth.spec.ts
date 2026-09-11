import { test, expect } from "@playwright/test";

test.describe("auth", () => {
  const accounts = [
    { role: "admin", email: "admin@univh2c.ma", landing: "/admin" },
    { role: "coordinator", email: "coord@univh2c.ma", landing: "/coordinator" },
    { role: "teacher", email: "teacher@univh2c.ma", landing: "/teacher" },
    { role: "student", email: "student@univh2c.ma", landing: "/student" },
  ] as const;

  for (const { role, email, landing } of accounts) {
    test(`login as ${role} lands on ${landing}`, async ({ page }) => {
      await page.goto("/login");
      await expect(page.getByTestId("login-email-input")).toBeVisible({ timeout: 10_000 });
      await page.getByTestId("login-email-input").fill(email);
      await page.getByTestId("login-password-input").fill("1234");
      await page.getByTestId("login-submit-button").click();
      await expect(page).toHaveURL(new RegExp(`${landing}(/|$)`), { timeout: 15_000 });
    });
  }

  test("login with wrong password shows an error", async ({ page }) => {
    await page.goto("/login");
    await page.getByTestId("login-email-input").fill("admin@univh2c.ma");
    await page.getByTestId("login-password-input").fill("wrong-password");
    await page.getByTestId("login-submit-button").click();
    await expect(page).toHaveURL(/\/login/);
  });

  test("logout returns to login", async ({ page }) => {
    await page.goto("/login");
    await page.getByTestId("login-email-input").fill("admin@univh2c.ma");
    await page.getByTestId("login-password-input").fill("1234");
    await page.getByTestId("login-submit-button").click();
    await expect(page).toHaveURL(/\/admin(\/|$)/, { timeout: 15_000 });

    const userMenu = page.getByTestId("nav-user-trigger");
    if (await userMenu.isVisible().catch(() => false)) {
      await userMenu.click();
    }
    const logoutTrigger = page.getByTestId("nav-user-logout-trigger");
    if (await logoutTrigger.isVisible().catch(() => false)) {
      await logoutTrigger.click();
      const confirm = page.getByTestId("nav-user-logout-confirm");
      if (await confirm.isVisible().catch(() => false)) {
        await confirm.click();
      }
      await expect(page).toHaveURL(/\/login/, { timeout: 10_000 });
    }
  });
});
