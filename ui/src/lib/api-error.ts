export class ApiError extends Error {
  status: number | null;
  fieldErrors?: Record<string, string>;
  isNetworkError: boolean;
  isTimeout: boolean;

  constructor({
    status,
    message,
    fieldErrors,
    isNetworkError = false,
    isTimeout = false,
  }: {
    status: number | null;
    message: string;
    fieldErrors?: Record<string, string>;
    isNetworkError?: boolean;
    isTimeout?: boolean;
  }) {
    super(message);
    this.name = "ApiError";
    this.status = status;
    this.fieldErrors = fieldErrors;
    this.isNetworkError = isNetworkError;
    this.isTimeout = isTimeout;
  }
}
