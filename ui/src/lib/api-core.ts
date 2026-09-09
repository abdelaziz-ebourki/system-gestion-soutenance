import { ApiError } from "@/lib/api-error";
import type { AppNotification, PaginatedResponse } from "@/types";

const BASE_URL = import.meta.env.VITE_API_BASE_URL || "/api";

function getCookie(name: string) {
  const value = `; ${document.cookie}`;
  const parts = value.split(`; ${name}=`);
  if (parts.length === 2) return parts.pop()?.split(";").shift();
  return undefined;
}

export const DEFAULT_TIMEOUT = 15_000;

export interface ApiResponse<T> {
  success: boolean;
  message: string;
  data: T;
  timestamp: string;
  errors: string[];
}

interface ApiOptions extends RequestInit {
  responseType?: "json" | "blob";
  timeout?: number;
}

export async function api<T>(
  endpoint: string,
  options: ApiOptions = {},
): Promise<T> {
  const { responseType = "json", timeout = DEFAULT_TIMEOUT, ...customConfig } = options;

  const headers: Record<string, string> = {
    ...customConfig.headers as Record<string, string> | undefined,
  };

  const method = (customConfig.method || "GET").toUpperCase();

  if (["POST", "PUT", "PATCH", "DELETE"].includes(method)) {
    const csrfToken = getCookie("XSRF-TOKEN");
    if (csrfToken) {
      headers["X-XSRF-TOKEN"] = csrfToken;
    }
  }

  if (responseType === "json" && !(customConfig.body instanceof FormData)) {
    headers["Content-Type"] = "application/json";
  }

  const controller = new AbortController();
  const timeoutId = setTimeout(() => controller.abort(), timeout);

  const config: RequestInit = {
    ...customConfig,
    method,
    headers,
    credentials: "include",
    signal: controller.signal,
  };

  try {
    const response = await fetch(`${BASE_URL}${endpoint}`, config);
    clearTimeout(timeoutId);

    if (!response.ok) {
      if (response.status === 401 && !endpoint.startsWith("/auth/")) {
        window.dispatchEvent(new CustomEvent("auth:expired"));
      }
      let data: Record<string, unknown>;
      try {
        data = await (responseType === "blob"
          ? response.text().then((t) => { try { return JSON.parse(t); } catch { return {}; } })
          : response.json());
      } catch {
        data = {};
      }

      if (data && typeof data === "object" && "message" in data) {
        throw new ApiError({
          status: response.status,
          message: (data.message as string) || "Une erreur est survenue lors de la requête.",
          fieldErrors: data.fieldErrors as Record<string, string> | undefined,
        });
      }

      throw new ApiError({
        status: response.status,
        message: "Une erreur est survenue lors de la requête.",
      });
    }

    if (
      response.status === 204 ||
      response.headers.get("content-length") === "0"
    ) {
      return {} as T;
    }

    if (responseType === "blob") {
      return await response.blob() as T;
    }

    const json = await response.json();

    if (json && typeof json === "object" && "success" in json && "data" in json) {
      const envelope = json as ApiResponse<T>;
      if (!envelope.success) {
        throw new ApiError({
          status: response.status,
          message: envelope.message || "Une erreur est survenue.",
          fieldErrors: envelope.errors?.[0] ? { general: envelope.errors[0] } : undefined,
        });
      }
      return envelope.data;
    }

    return json as T;
  } catch (error: unknown) {
    clearTimeout(timeoutId);

    if (controller.signal.aborted) {
      throw new ApiError({
        status: null,
        message: "La requête a expiré. Veuillez réessayer.",
        isTimeout: true,
      });
    }

    if (error instanceof ApiError) {
      throw error;
    }

    if (error instanceof TypeError) {
      throw new ApiError({
        status: null,
        message: "Impossible de contacter le serveur. Vérifiez votre connexion.",
        isNetworkError: true,
      });
    }

    throw new ApiError({
      status: null,
      message: "Une erreur inattendue est survenue.",
    });
  }
}

export const getNotifications = () => api<PaginatedResponse<AppNotification>>("/notifications");

export const markNotificationRead = (id: number) =>
  api<void>(`/notifications/${id}/read`, { method: "PATCH" });

export const markAllNotificationsRead = () =>
  api<void>("/notifications/read-all", { method: "PATCH" });

export const sendNotificationEmail = (id: number) =>
  api<void>(`/notifications/${id}/send-email`, { method: "POST" });

export interface DefenseSettings {
  id: number;
  startTime: string;
  endTime: string;
  defenseDuration: number;
  breakDuration: number;
  groupCreationStartDate: string;
  groupCreationEndDate: string;
}

