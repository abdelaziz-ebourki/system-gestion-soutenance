import path from "path";
import tailwindcss from "@tailwindcss/vite";
import { defineConfig } from "vitest/config";
import react from "@vitejs/plugin-react";

export default defineConfig({
  plugins: [
    react(),
    tailwindcss(),
  ],
  resolve: {
    alias: {
      "@": path.resolve(__dirname, "./src"),
    },
  },
  server: {
    proxy: {
      "/api": {
        target: "http://localhost:8080",
        changeOrigin: true,
        cookieDomainRewrite: { "localhost": "" },
      },
    },
  },
  build: {
    target: "es2020",
    rollupOptions: {
      output: {
        manualChunks(id) {
          if (id.includes("node_modules/react")) return "react";
          if (id.includes("node_modules/@tanstack")) return "tanstack";
        },
      },
    },
  },
  test: {
    environment: "jsdom",
    globals: true,
    setupFiles: ["./src/test/setup.ts"],
    css: false,
    coverage: {
      provider: "v8",
      enabled: false,
      include: ["src/**/*.{ts,tsx}"],
      exclude: [
        "src/components/ui/**",
        "src/types/**",
        "src/main.tsx",
        "src/App.tsx",
        "src/lib/constants.ts",
        "src/config/**",
        "src/test/**",
        "**/*.d.ts",
      ],
      thresholds: {
        lines: 50,
        branches: 40,
        functions: 40,
        statements: 45,
      },
    },
  },
});
