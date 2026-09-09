import { screen } from "@testing-library/react";
import { describe, it, expect, beforeEach } from "vitest";
import StudentDashboard from "@/pages/student/StudentDashboard";
import { renderWithProviders } from "@/test/utils";
import { server } from "@/test/mocks/server";
import { http, HttpResponse } from "msw";

const studentUser = {
  id: 4,
  email: "student@univh2c.ma",
  firstName: "Student",
  lastName: "User",
  role: "student" as const,
  isActive: true,
};

describe("StudentDashboard", () => {
  beforeEach(() => {
    server.resetHandlers();
  });

  it("renders hero section and stats cards", async () => {
    renderWithProviders(<StudentDashboard />, {
      initialAuthState: { user: studentUser },
    });
    expect(await screen.findByText("Application CI/CD")).toBeInTheDocument();
    expect(screen.getAllByText("Ahmed Benali")).toHaveLength(2);
    expect(screen.getByTestId("student-dashboard-stats-status")).toBeInTheDocument();
    expect(screen.getByTestId("student-dashboard-stats-members")).toHaveTextContent("3");
    expect(screen.getByTestId("student-dashboard-stats-documents")).toHaveTextContent("4");
    expect(screen.getByTestId("student-dashboard-stats-missing")).toHaveTextContent("1");
  });

  it("shows print convocation button when defense is scheduled", async () => {
    renderWithProviders(<StudentDashboard />, {
      initialAuthState: { user: studentUser },
    });
    expect(await screen.findByTestId("student-dashboard-print-btn")).toBeInTheDocument();
    expect(screen.queryByTestId("student-dashboard-group-link")).not.toBeInTheDocument();
  });

  it("shows group link when defense is pending", async () => {
    server.use(
      http.get("*/api/student/defenses", () =>
        HttpResponse.json({
          projectTitle: "Projet Test",
          projectDescription: "Description",
          supervisorName: "Prof X",
          juryMembers: [],
          status: "pending",
        }),
      ),
    );
    renderWithProviders(<StudentDashboard />, {
      initialAuthState: { user: studentUser },
    });
    expect(await screen.findByTestId("student-dashboard-group-link")).toBeInTheDocument();
    expect(screen.queryByTestId("student-dashboard-print-btn")).not.toBeInTheDocument();
  });

  it("renders jury members", async () => {
    renderWithProviders(<StudentDashboard />, {
      initialAuthState: { user: studentUser },
    });
    expect(await screen.findByText("President")).toBeInTheDocument();
  });

  it("shows result when available", async () => {
    server.use(
      http.get("*/api/student/defenses", () =>
        HttpResponse.json({
          projectTitle: "Application CI/CD",
          supervisorName: "Ahmed Benali",
          juryMembers: [],
          date: "2026-06-15",
          startTime: "08:00",
          endTime: "09:00",
          roomName: "Salle 101",
          status: "scheduled",
          result: "Admis",
        }),
      ),
    );
    renderWithProviders(<StudentDashboard />, {
      initialAuthState: { user: studentUser },
    });
    expect(await screen.findByText("Admis")).toBeInTheDocument();
  });

  it("shows no jury message when empty", async () => {
    server.use(
      http.get("*/api/student/defenses", () =>
        HttpResponse.json({
          projectTitle: "Application CI/CD",
          supervisorName: "Ahmed Benali",
          juryMembers: [],
          status: "pending",
        }),
      ),
    );
    renderWithProviders(<StudentDashboard />, {
      initialAuthState: { user: studentUser },
    });
    expect(await screen.findByText("Aucun membre du jury assigné.")).toBeInTheDocument();
  });
});
