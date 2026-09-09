import { render, screen, fireEvent } from "@testing-library/react";
import { describe, it, expect, vi } from "vitest";
import AvailabilityCalendar from "@/components/coordinator/AvailabilityCalendar";

const mockUnavailableSlots = {
  "2025-06-16": ["08:30 - 10:00", "10:15 - 11:45"],
  "2025-06-17": ["12:00 - 13:30"],
};

const mockSessions = [
  {
    dateKey: "2025-06-16",
    time: "13:45 - 15:15",
    student: "Student A",
    room: "Room A01",
  },
];

const mockSlots = [
  "08:30 - 10:00",
  "10:15 - 11:45",
  "12:00 - 13:30",
  "13:45 - 15:15",
  "15:30 - 17:00",
];

describe("AvailabilityCalendar", () => {
  const defaultProps = {
    initialMonth: 5, // June
    initialYear: 2025,
    unavailableSlots: mockUnavailableSlots,
    onToggleSlot: vi.fn(),
    sessions: mockSessions,
    onSave: vi.fn(),
    slots: mockSlots,
  };

  it("renders calendar header and controls", () => {
    render(<AvailabilityCalendar {...defaultProps} />);
    expect(screen.getByTestId("teacher-availability-title")).toHaveTextContent("Calendrier des Indisponibilités");
    expect(screen.getByTestId("teacher-availability-month-label")).toHaveTextContent("Juin 2025");
    expect(screen.getByTestId("teacher-availability-prev-month")).toBeInTheDocument();
    expect(screen.getByTestId("teacher-availability-next-month")).toBeInTheDocument();
  });

  it("renders days of the month", () => {
    render(<AvailabilityCalendar {...defaultProps} />);
    // June 16, 2025
    expect(screen.getByTestId("teacher-availability-day-2025-06-16")).toBeInTheDocument();
  });

  it("updates side panel when a day is clicked", () => {
    render(<AvailabilityCalendar {...defaultProps} />);
    
    // Click June 16
    fireEvent.click(screen.getByTestId("teacher-availability-day-2025-06-16"));
    
    expect(screen.getByTestId("teacher-availability-side-panel")).toHaveTextContent("Détails : 16 Juin");
  });

  it("renders slots in side panel and handles toggle", () => {
    render(<AvailabilityCalendar {...defaultProps} />);
    
    // Select June 16
    fireEvent.click(screen.getByTestId("teacher-availability-day-2025-06-16"));
    
    // Slot "15:30 - 17:00" is available for June 16
    const slotBtn = screen.getByTestId("teacher-availability-slot-15h30-17h00");
    expect(slotBtn).toBeInTheDocument();
    
    fireEvent.click(slotBtn);
    expect(defaultProps.onToggleSlot).toHaveBeenCalledWith("2025-06-16", "15:30 - 17:00");
  });

  it("disables slots that have an associated session", () => {
    render(<AvailabilityCalendar {...defaultProps} />);
    
    // Select June 16
    fireEvent.click(screen.getByTestId("teacher-availability-day-2025-06-16"));
    
    // 13:45 - 15:15 has a session
    const sessionSlot = screen.getByTestId("teacher-availability-slot-13h45-15h15");
    expect(sessionSlot).toBeDisabled();
    expect(sessionSlot).toHaveTextContent("Jury");
  });

  it("triggers onSave when save button is clicked", () => {
    render(<AvailabilityCalendar {...defaultProps} />);
    
    const saveBtn = screen.getByTestId("teacher-availability-save");
    fireEvent.click(saveBtn);
    expect(defaultProps.onSave).toHaveBeenCalled();
  });

  it("applies partial unavailability style", () => {
    render(<AvailabilityCalendar {...defaultProps} />);
    const day16 = screen.getByTestId("teacher-availability-day-2025-06-16");
    expect(day16.className).toContain("bg-primary/5");
    expect(day16.className).toContain("border-primary/20");
  });

  it("applies full unavailability style", () => {
    const fullUnavailable = { ...mockUnavailableSlots, "2025-06-20": [...mockSlots] };
    render(<AvailabilityCalendar {...defaultProps} unavailableSlots={fullUnavailable} />);
    const day20 = screen.getByTestId("teacher-availability-day-2025-06-20");
    expect(day20.className).toContain("bg-destructive/10");
  });

});

