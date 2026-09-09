import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import * as api from "@/lib/api";
import type { GroupDocumentType } from "@/types";

export function useStudentStats() {
  return useQuery({
    queryKey: ["student", "stats"],
    queryFn: api.getStudentStats,
  });
}

export function useStudentDefense() {
  return useQuery({
    queryKey: ["student", "defense"],
    queryFn: api.getStudentDefense,
  });
}

export function useStudentGroup() {
  return useQuery({
    queryKey: ["student", "group"],
    queryFn: api.getStudentGroup,
  });
}

export function useCreateStudentGroup() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: () => api.createStudentGroup(),
    onSuccess: () => qc.invalidateQueries({ queryKey: ["student", "group"], refetchType: "active" }),
  });
}

export function useJoinStudentGroup() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (groupId: number) => api.joinStudentGroup(groupId),
    onSuccess: () => qc.invalidateQueries({ queryKey: ["student", "group"], refetchType: "active" }),
  });
}

export function useStudentDocuments() {
  return useQuery({
    queryKey: ["student", "documents"],
    queryFn: api.getStudentDocuments,
  });
}

export function useUploadStudentDocument() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: ({ documentId, file }: { documentId: number; file: File }) =>
      api.uploadStudentDocument(documentId, file),
    onSuccess: () => qc.invalidateQueries({ queryKey: ["student", "documents"], refetchType: "active" }),
  });
}

export function useGroupDocuments(groupId: number | null) {
  return useQuery({
    queryKey: ["student", "group", "documents", groupId],
    queryFn: () => api.getGroupDocuments(groupId!),
    enabled: groupId != null,
  });
}

export function useUploadGroupDocument() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: ({ groupId, type, file }: { groupId: number; type: GroupDocumentType; file: File }) =>
      api.uploadGroupDocument(groupId, type, file),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ["student", "group", "documents"], refetchType: "active" });
    },
  });
}
