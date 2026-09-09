import { useState, useCallback, useMemo, useRef, useEffect } from "react";
import type { z } from "zod";
import { validate } from "@/lib/validations";

export function useEntityForm<TForm>(
  schema: z.ZodType<TForm>,
  defaultForm: TForm,
) {
  const [formData, setFormData] = useState<TForm>(defaultForm);
  const [fieldErrors, setFieldErrors] = useState<Partial<Record<keyof TForm, string>>>({});

  const formDataRef = useRef(formData);
  useEffect(() => {
    formDataRef.current = formData;
  }, [formData]);

  const resetForm = useCallback(() => {
    setFormData(defaultForm);
    setFieldErrors({});
  }, [defaultForm]);

  const validateForm = useCallback((data?: TForm): boolean => {
    const errors = validate(schema, data ?? formDataRef.current);
    if (errors) {
      setFieldErrors(errors);
      return false;
    }
    setFieldErrors({});
    return true;
  }, [schema]);

  return useMemo(() => ({
    formData,
    setFormData,
    fieldErrors,
    setFieldErrors,
    resetForm,
    validateForm,
  }), [formData, fieldErrors, resetForm, validateForm]);
}
