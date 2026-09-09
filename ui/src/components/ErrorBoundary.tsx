import { Component, type ErrorInfo, type ReactNode } from "react";
import { Button } from "@/components/ui/button";
import { AlertTriangle } from "lucide-react";

interface ErrorBoundaryProps {
  children: ReactNode;
}

interface ErrorBoundaryState {
  hasError: boolean;
  error: Error | null;
}

export class ErrorBoundary extends Component<
  ErrorBoundaryProps,
  ErrorBoundaryState
> {
  constructor(props: ErrorBoundaryProps) {
    super(props);
    this.state = { hasError: false, error: null };
  }

  static getDerivedStateFromError(error: Error): ErrorBoundaryState {
    return { hasError: true, error };
  }

  componentDidCatch(error: Error, errorInfo: ErrorInfo) {
    console.error("ErrorBoundary caught:", error, errorInfo);
  }

  handleReset = () => {
    this.setState({ hasError: false, error: null });
  };

  render() {
    if (this.state.hasError) {
      return (
        <div className="min-h-screen bg-background flex items-center justify-center p-4" data-testid="error-boundary">
          <div className="flex flex-col items-center gap-6 text-center max-w-md">
            <AlertTriangle className="size-16 text-destructive" />
            <h1 className="font-heading text-3xl font-bold text-foreground" data-testid="error-boundary-title">
              Une erreur est survenue
            </h1>
            <p className="text-muted-foreground" data-testid="error-boundary-description">
              L'application a rencontré une erreur inattendue. Veuillez
              rafraîchir la page ou réessayer.
            </p>
            <Button onClick={this.handleReset} variant="default" size="lg" data-testid="error-boundary-retry-btn">
              Réessayer
            </Button>
          </div>
        </div>
      );
    }

    return this.props.children;
  }
}
