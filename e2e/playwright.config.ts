import { defineConfig, devices } from "@playwright/test";

export default defineConfig({
  testDir: "./tests",
  fullyParallel: false,
  forbidOnly: !!process.env.CI,
  retries: process.env.CI ? 2 : 0,
  workers: 1,
  reporter: [["html"], ["list"]],
  use: {
    baseURL: process.env.UI_URL || "http://localhost:5173",
    trace: "on-first-retry",
    screenshot: "only-on-failure",
  },
  projects: [
    {
      name: "chromium",
      use: { ...devices["Desktop Chrome"] },
    },
  ],
  webServer: process.env.CI
    ? undefined
    : [
        {
          command: "sh -c 'cd ../api && ./mvnw -q spring-boot:run -Dspring-boot.run.profiles=test'",
          url: "http://localhost:8080/actuator/health",
          timeout: 180_000,
          reuseExistingServer: true,
          stdout: "pipe",
        },
        {
          command: "sh -c 'cd ../ui && npm run dev -- --port 5173'",
          url: "http://localhost:5173",
          timeout: 120_000,
          reuseExistingServer: true,
        },
      ],
});
