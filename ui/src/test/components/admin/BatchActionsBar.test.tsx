import { render, screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { describe, it, expect, vi, beforeEach } from "vitest";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { BatchActionsBar } from "@/components/admin/BatchActionsBar";

vi.mock("sonner", () => ({ toast: { success: vi.fn(), error: vi.fn() } }));

function renderBatch(overrideProps = {}) {
  const queryClient = new QueryClient({
    defaultOptions: { queries: { retry: false }, mutations: { retry: false } },
  });
  const defaultProps = {
    selectedCount: 0,
    entityLabel: "étudiant(s)",
    actions: [{ key: "major", label: "Modifier la filière" }, { key: "delete", label: "Supprimer" }],
    fieldOptionsMap: {
      major: [{ value: "m1", label: "Génie Info" }, { value: "m2", label: "Génie Civil" }],
    },
    onUpdateField: vi.fn().mockResolvedValue(undefined),
    onDeleteSelected: vi.fn(),
    isPending: false,
    onClearSelection: vi.fn(),
  };
  return render(
    <QueryClientProvider client={queryClient}>
      <BatchActionsBar {...defaultProps} {...overrideProps} />
    </QueryClientProvider>,
  );
}

describe("BatchActionsBar", () => {
  beforeEach(() => vi.clearAllMocks());
  it("does not render when selectedCount is 0", () => {
    const { container } = renderBatch({ selectedCount: 0 });
    expect(container.firstChild).toBeNull();
  });

  it("renders with selected count", () => {
    renderBatch({ selectedCount: 3 });
    expect(screen.getByText(/3 étudiant\(s\) sélectionné\(s\)/)).toBeInTheDocument();
  });

  it("opens delete alert when delete action is clicked", async () => {
    const user = userEvent.setup();
    renderBatch({ selectedCount: 2 });
    await user.click(screen.getByRole("button", { name: /supprimer/i }));
    expect(screen.getByText(/Confirmation/)).toBeInTheDocument();
  });

  it("calls onDeleteSelected when delete is confirmed", async () => {
    const onDeleteSelected = vi.fn().mockResolvedValue(undefined);
    const user = userEvent.setup();
    renderBatch({ selectedCount: 2, onDeleteSelected });
    await user.click(screen.getByRole("button", { name: /supprimer/i }));
    await user.click(screen.getByRole("button", { name: /supprimer/i }));
    expect(onDeleteSelected).toHaveBeenCalled();
  });

  it("calls onClearSelection after delete", async () => {
    const onClearSelection = vi.fn();
    const user = userEvent.setup();
    renderBatch({ selectedCount: 2, onClearSelection });
    await user.click(screen.getByRole("button", { name: /supprimer/i }));
    await user.click(screen.getByRole("button", { name: /supprimer/i }));
    await waitFor(() => expect(onClearSelection).toHaveBeenCalled());
  });

  it("opens update dialog when non-delete action is clicked", async () => {
    const user = userEvent.setup();
    renderBatch({ selectedCount: 3 });
    await user.click(screen.getByRole("button", { name: /modifier la filière/i }));
    expect(screen.getByText(/Choisir\.\.\./)).toBeInTheDocument();
  });

  it("calls onUpdateField when field update is submitted", async () => {
    const onUpdateField = vi.fn().mockResolvedValue(undefined);
    const user = userEvent.setup();
    renderBatch({ selectedCount: 3, onUpdateField });
    await user.click(screen.getByRole("button", { name: /modifier la filière/i }));
    await user.click(screen.getByRole("combobox"));
    await user.click(screen.getByRole("option", { name: /génie info/i }));
    await user.click(screen.getByRole("button", { name: /enregistrer/i }));
    await waitFor(() => expect(onUpdateField).toHaveBeenCalledWith("major", "m1"));
  });

  it("shows success toast after field update", async () => {
    const { toast } = await import("sonner");
    const user = userEvent.setup();
    renderBatch({ selectedCount: 3 });
    await user.click(screen.getByRole("button", { name: /modifier la filière/i }));
    await user.click(screen.getByRole("combobox"));
    await user.click(screen.getByRole("option", { name: /génie info/i }));
    await user.click(screen.getByRole("button", { name: /enregistrer/i }));
    await waitFor(() => expect(toast.success).toHaveBeenCalled());
  });

  it("calls onClearSelection after successful update", async () => {
    const onClearSelection = vi.fn();
    const user = userEvent.setup();
    renderBatch({ selectedCount: 3, onClearSelection });
    await user.click(screen.getByRole("button", { name: /modifier la filière/i }));
    await user.click(screen.getByRole("combobox"));
    await user.click(screen.getByRole("option", { name: /génie info/i }));
    await user.click(screen.getByRole("button", { name: /enregistrer/i }));
    await waitFor(() => expect(onClearSelection).toHaveBeenCalled());
  });

  it("shows error toast when update fails", async () => {
    const onUpdateField = vi.fn().mockRejectedValue(new Error("fail"));
    const { toast } = await import("sonner");
    const user = userEvent.setup();
    renderBatch({ selectedCount: 3, onUpdateField });
    await user.click(screen.getByRole("button", { name: /modifier la filière/i }));
    await user.click(screen.getByRole("combobox"));
    await user.click(screen.getByRole("option", { name: /génie info/i }));
    await user.click(screen.getByRole("button", { name: /enregistrer/i }));
    await waitFor(() => expect(toast.error).toHaveBeenCalled());
  });
});
