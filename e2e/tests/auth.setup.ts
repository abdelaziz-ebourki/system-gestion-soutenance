import { test as setup, expect, request } from "@playwright/test";
import path from "node:path";
import fs from "node:fs";
import { fileURLToPath } from "node:url";

const HERE = path.dirname(fileURLToPath(import.meta.url));
const AUTH_DIR = path.join(HERE, "../playwright/.auth");
const API_URL = process.env.API_URL || "http://localhost:8080";
const UI_ORIGIN = process.env.UI_URL || "http://localhost:5173";

const ACCOUNTS = {
  admin: { email: "admin@univh2c.ma", password: "1234" },
  coordinator: { email: "coord@univh2c.ma", password: "1234" },
  teacher: { email: "teacher@univh2c.ma", password: "1234" },
  student: { email: "student@univh2c.ma", password: "1234" },
} as const;

for (const [role, credentials] of Object.entries(ACCOUNTS)) {
  setup(`authenticate as ${role}`, async () => {
    const context = await request.newContext({ baseURL: API_URL });
    const response = await context.post("/api/auth/login", { data: credentials });
    expect(response.ok(), `login failed for ${role}: ${response.status()}`).toBeTruthy();
    const body = await response.json();
    const statePath = path.join(AUTH_DIR, `${role}.json`);
    await context.storageState({ path: statePath });
    await context.dispose();

    // The UI reads the user from localStorage (key "user"), not from cookies.
    const raw = JSON.parse(fs.readFileSync(statePath, "utf-8"));
    raw.origins = [
      {
        origin: UI_ORIGIN,
        localStorage: [{ name: "user", value: JSON.stringify(body.user ?? body.data?.user) }],
      },
    ];
    fs.writeFileSync(statePath, JSON.stringify(raw));
  });
}
