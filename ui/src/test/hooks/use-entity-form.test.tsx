import { renderHook, act } from "@testing-library/react";
import { describe, it, expect } from "vitest";
import { z } from "zod";
import { useEntityForm } from "@/hooks/use-entity-form";

const testSchema = z.object({
  name: z.string().min(1, "Name required"),
  age: z.number().min(18, "Must be 18+"),
});

type TestForm = z.infer<typeof testSchema>;
const defaultForm: TestForm = { name: "", age: 0 };

describe("useEntityForm", () => {
  it("returns default form data", () => {
    const { result } = renderHook(() => useEntityForm(testSchema, defaultForm));
    expect(result.current.formData).toEqual({ name: "", age: 0 });
    expect(result.current.fieldErrors).toEqual({});
  });

  it("updates form data via setFormData", () => {
    const { result } = renderHook(() => useEntityForm(testSchema, defaultForm));
    act(() => result.current.setFormData({ name: "John", age: 25 }));
    expect(result.current.formData).toEqual({ name: "John", age: 25 });
  });

  it("validateForm returns true for valid data", () => {
    const { result } = renderHook(() => useEntityForm(testSchema, defaultForm));
    act(() => result.current.setFormData({ name: "John", age: 25 }));
    let valid = false;
    act(() => { valid = result.current.validateForm(); });
    expect(valid).toBe(true);
    expect(result.current.fieldErrors).toEqual({});
  });

  it("validateForm returns false for invalid data and sets fieldErrors", () => {
    const { result } = renderHook(() => useEntityForm(testSchema, defaultForm));
    // don't set data — keep defaults which are invalid
    let valid = true;
    act(() => { valid = result.current.validateForm(); });
    expect(valid).toBe(false);
    expect(result.current.fieldErrors.name).toBeDefined();
  });

  it("resetForm clears data and errors", () => {
    const { result } = renderHook(() => useEntityForm(testSchema, defaultForm));
    act(() => result.current.setFormData({ name: "John", age: 25 }));
    act(() => { result.current.validateForm(); });
    act(() => result.current.resetForm());
    expect(result.current.formData).toEqual(defaultForm);
    expect(result.current.fieldErrors).toEqual({});
  });

  it("setFieldErrors updates errors directly", () => {
    const { result } = renderHook(() => useEntityForm(testSchema, defaultForm));
    act(() => result.current.setFieldErrors({ name: "Custom error" }));
    expect(result.current.fieldErrors).toEqual({ name: "Custom error" });
  });
});
