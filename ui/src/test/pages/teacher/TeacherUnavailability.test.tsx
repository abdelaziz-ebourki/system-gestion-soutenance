import { render, screen, fireEvent, waitFor } from "@testing-library/react";
import { describe, it, expect, vi, beforeEach } from "vitest";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { MemoryRouter } from "react-router-dom";
import { toast } from "sonner";
import TeacherUnavailability from "@/pages/teacher/TeacherUnavailability";
import { useTeacherSchedule, useTeacherUnavailability, useSaveTeacherUnavailability } from "@/hooks/queries";
import type { TeacherUnavailability as UnavailabilityType, TeacherDefense } from "@/types";
import type { UseQueryResult, UseMutationResult } from "@tanstack/react-query";
import type { AvailabilityCalendarProps } from "@/components/coordinator/AvailabilityCalendar";

vi.mock("@/hooks/queries", async (importOriginal) => {
  const actual = await importOriginal<typeof import("@/hooks/queries")>();
  return {
    ...actual,
    useTeacherSchedule: vi.fn(),
    useTeacherUnavailability: vi.fn(),
    useSaveTeacherUnavailability: vi.fn(),
  };
});

vi.mock("sonner", () => ({
  toast: {
    success: vi.fn(),
    error: vi.fn(),
  },
}));



vi.mock("@/components/coordinator/AvailabilityCalendar", () => ({
  default: ({ onToggleSlot }: AvailabilityCalendarProps) => (
    <div data-testid="availability-calendar">
      <button 
        onClick={() => onToggleSlot("2025-06-16", "09:00-10:00")} 
        data-testid="calendar-toggle-slot"
      >
        Toggle Slot
      </button>
    </div>
  ),
}));

const mockSchedule = {
  slots: [
    {
      date: "2025-06-16",
      time: "09:00",
      roomName: "Room 1",
      projectTitle: "Project 1",
      studentNames: ["Student 1"],
    } as TeacherDefense,
  ],
};

const mockUnavailability: UnavailabilityType = {
  slotsByDate: {
    "2025-06-17": ["08:00-09:00", "09:00-10:00"],
    "2025-06-18": ["10:00-11:00"],
  },
};

const mockSaveMutation = {
  mutateAsync: vi.fn(),
  isPending: false,
} as unknown as UseMutationResult<UnavailabilityType, Error, { slots: Array<{ date: string; slots: string[] }> }, unknown>;

function renderUnavailability() {
  const queryClient = new QueryClient({
    defaultOptions: { queries: { retry: false }, mutations: { retry: false } },
  });
  return render(
    <QueryClientProvider client={queryClient}>
      <MemoryRouter>
        <TeacherUnavailability />
      </MemoryRouter>
    </QueryClientProvider>,
  );
}

describe("TeacherUnavailability", () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it("renders loading state", async () => {
    vi.mocked(useTeacherSchedule).mockReturnValue({ data: null, isLoading: true } as unknown as UseQueryResult<{ slots: TeacherDefense[] }, Error>);
    vi.mocked(useTeacherUnavailability).mockReturnValue({ data: null, isLoading: true } as unknown as UseQueryResult<UnavailabilityType, Error>);
    vi.mocked(useSaveTeacherUnavailability).mockReturnValue(mockSaveMutation);
    
    renderUnavailability();
    expect(screen.getByTestId("teacher-unavailability-header")).toBeInTheDocument();
    expect(screen.queryByTestId("availability-calendar")).not.toBeInTheDocument();
  });

  it("renders header and save button", async () => {
    vi.mocked(useTeacherSchedule).mockReturnValue({ data: mockSchedule, isLoading: false } as unknown as UseQueryResult<{ slots: TeacherDefense[] }, Error>);
    vi.mocked(useTeacherUnavailability).mockReturnValue({ data: mockUnavailability, isLoading: false } as unknown as UseQueryResult<UnavailabilityType, Error>);
    vi.mocked(useSaveTeacherUnavailability).mockReturnValue(mockSaveMutation);
    
    renderUnavailability();
    expect(screen.getByTestId("teacher-unavailability-header")).toHaveTextContent("Mes indisponibilités");
    expect(screen.getByTestId("teacher-unavailability-description")).toBeInTheDocument();
    expect(screen.getByTestId("teacher-unavailability-save")).toBeInTheDocument();
  });

  it("calculates and renders stats correctly", async () => {
    vi.mocked(useTeacherSchedule).mockReturnValue({ data: mockSchedule, isLoading: false } as unknown as UseQueryResult<{ slots: TeacherDefense[] }, Error>);
    vi.mocked(useTeacherUnavailability).mockReturnValue({ data: mockUnavailability, isLoading: false } as unknown as UseQueryResult<UnavailabilityType, Error>);
    vi.mocked(useSaveTeacherUnavailability).mockReturnValue(mockSaveMutation);
    
    renderUnavailability();
    
    expect(screen.getByTestId("teacher-unavailability-stats-blocked")).toHaveTextContent("3");
    expect(screen.getByTestId("teacher-unavailability-stats-days")).toHaveTextContent("2");
  });

  it("renders AvailabilityCalendar and handles slot toggle", async () => {
    vi.mocked(useTeacherSchedule).mockReturnValue({ data: mockSchedule, isLoading: false } as unknown as UseQueryResult<{ slots: TeacherDefense[] }, Error>);
    vi.mocked(useTeacherUnavailability).mockReturnValue({ data: mockUnavailability, isLoading: false } as unknown as UseQueryResult<UnavailabilityType, Error>);
    vi.mocked(useSaveTeacherUnavailability).mockReturnValue(mockSaveMutation);
    
    renderUnavailability();
    
    expect(screen.getByTestId("availability-calendar")).toBeInTheDocument();
    
    const toggleBtn = screen.getByTestId("calendar-toggle-slot");
    fireEvent.click(toggleBtn);
    
    fireEvent.click(screen.getByTestId("teacher-unavailability-save"));
    
    expect(mockSaveMutation.mutateAsync).toHaveBeenCalledWith(expect.objectContaining({
      slots: expect.arrayContaining([
        expect.objectContaining({ date: "2025-06-16" })
      ])
    }));
  });

  it("saves unavailability and shows success toast", async () => {
    vi.mocked(useTeacherSchedule).mockReturnValue({ data: mockSchedule, isLoading: false } as unknown as UseQueryResult<{ slots: TeacherDefense[] }, Error>);
    vi.mocked(useTeacherUnavailability).mockReturnValue({ data: mockUnavailability, isLoading: false } as unknown as UseQueryResult<UnavailabilityType, Error>);
    
    const saveMutation = {
      mutateAsync: vi.fn().mockResolvedValue({}),
      isPending: false,
    } as unknown as UseMutationResult<UnavailabilityType, Error, { slots: { date: string; slots: string[] }[] }, unknown>;
    vi.mocked(useSaveTeacherUnavailability).mockReturnValue(saveMutation);
    
    const { toast } = await import("sonner");
    
    renderUnavailability();
    
    fireEvent.click(screen.getByTestId("teacher-unavailability-save"));
    
    await waitFor(() => {
      expect(saveMutation.mutateAsync).toHaveBeenCalled();
      expect(toast.success).toHaveBeenCalledWith("Indisponibilités enregistrées");
    });
  });

  it("shows error toast when save fails", async () => {
    vi.mocked(useTeacherSchedule).mockReturnValue({ data: mockSchedule, isLoading: false } as unknown as UseQueryResult<{ slots: TeacherDefense[] }, Error>);
    vi.mocked(useTeacherUnavailability).mockReturnValue({ data: mockUnavailability, isLoading: false } as unknown as UseQueryResult<UnavailabilityType, Error>);
    
    const saveMutation = {
      mutateAsync: vi.fn().mockRejectedValue(new Error("API Error")),
      isPending: false,
    } as unknown as UseMutationResult<UnavailabilityType, Error, { slots: { date: string; slots: string[] }[] }, unknown>;
    vi.mocked(useSaveTeacherUnavailability).mockReturnValue(saveMutation);
    
    renderUnavailability();
    
    fireEvent.click(screen.getByTestId("teacher-unavailability-save"));
    
    await waitFor(() => {
      expect(toast.error).toHaveBeenCalled();
    });
  });
});

