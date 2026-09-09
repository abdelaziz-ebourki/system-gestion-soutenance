import { type ReactElement } from "react";
import { render, type RenderOptions } from "@testing-library/react";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { MemoryRouter } from "react-router-dom";
import { AuthProvider } from "@/contexts/auth-context";
import type { User } from "@/types";
import { TooltipProvider } from "@/components/ui/tooltip";

function createTestQueryClient() {
  return new QueryClient({
    defaultOptions: {
      queries: {
        retry: false,
        gcTime: 0,
      },
      mutations: {
        retry: false,
      },
    },
  });
}

interface WrapperOptions {
  initialEntries?: string[];
  initialAuthState?: {
    user?: User;
  };
}

function createWrapper(options: WrapperOptions = {}) {
  const queryClient = createTestQueryClient();
  const { initialEntries = ["/"], initialAuthState } = options;

  if (initialAuthState?.user) {
    localStorage.setItem("user", JSON.stringify(initialAuthState.user));
    localStorage.setItem("token", "mock-test-token");
  } else {
    localStorage.clear();
  }

  return function Wrapper({ children }: { children: React.ReactNode }) {
    return (
      <QueryClientProvider client={queryClient}>
        <MemoryRouter initialEntries={initialEntries}>
          <AuthProvider>
            <TooltipProvider>
              {children}
            </TooltipProvider>
          </AuthProvider>
        </MemoryRouter>
      </QueryClientProvider>
    );
  };
}

export function renderWithProviders(
  ui: ReactElement,
  options: RenderOptions & WrapperOptions = {},
) {
  const { initialEntries, initialAuthState, ...renderOptions } = options;
  const wrapper = createWrapper({ initialEntries, initialAuthState });

  return {
    ...render(ui, { wrapper, ...renderOptions }),
  };
}
