import { StrictMode } from "react";
import { createRoot } from "react-dom/client";
import { BrowserRouter } from "react-router-dom";
import "./index.css";
import App from "./App.tsx";
import { TooltipProvider } from "./components/ui/tooltip";
import { Toaster } from "./components/ui/sonner";
import { ThemeProvider } from "./components/theme-provider";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { AuthProvider } from "./contexts/auth-context";
import { DEFAULT_RETRY, DEFAULT_STALE_TIME } from "@/lib/constants";

const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      retry: DEFAULT_RETRY,
      refetchOnWindowFocus: false,
      staleTime: DEFAULT_STALE_TIME,
    },
  },
});

createRoot(document.getElementById("root")!).render(
  <StrictMode>
    <ThemeProvider>
      <QueryClientProvider client={queryClient}>
        <BrowserRouter>
          <TooltipProvider>
            <AuthProvider>
                <App />
              <Toaster position="top-center" />
            </AuthProvider>
          </TooltipProvider>
        </BrowserRouter>
      </QueryClientProvider>
    </ThemeProvider>
  </StrictMode>,
);
