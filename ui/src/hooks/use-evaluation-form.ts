import { useState } from "react";
import { useSubmitTeacherEvaluation } from "@/hooks/queries";
import { validate, evaluationSchema } from "@/lib/validations";
import { toast } from "sonner";
import { getErrorMessage } from "@/lib/utils";
import type { TeacherEvaluation } from "@/types";

export function useEvaluationForm() {
  const submit = useSubmitTeacherEvaluation();
  const [selected, setSelected] = useState<TeacherEvaluation | null>(null);
  const [formData, setFormData] = useState({ score: 0, comment: "" });
  const [fieldErrors, setFieldErrors] = useState<Partial<Record<"score" | "comment", string>>>({});
  const [isDialogOpen, setIsDialogOpen] = useState(false);

  const openEdit = (evaluation: TeacherEvaluation) => {
    setSelected(evaluation);
    setFormData({ score: evaluation.finalGrade ?? 0, comment: evaluation.comment ?? "" });
    setFieldErrors({});
    setIsDialogOpen(true);
  };

  const handleSubmit = async (e: { preventDefault(): void }) => {
    e.preventDefault();
    const errors = validate(evaluationSchema, formData);
    if (errors) {
      setFieldErrors(errors);
      return;
    }
    setFieldErrors({});
    if (!selected) return;
    try {
      await submit.mutateAsync({ id: selected.id, data: formData });
      toast.success("Évaluation enregistrée");
      setIsDialogOpen(false);
      setSelected(null);
      setFormData({ score: 0, comment: "" });
    } catch (error) {
      toast.error(getErrorMessage(error, "Erreur lors de l'enregistrement"));
    }
  };

  return {
    selected,
    formData,
    setFormData,
    fieldErrors,
    isDialogOpen,
    setIsDialogOpen,
    openEdit,
    handleSubmit,
    isPending: submit.isPending,
  };
}

