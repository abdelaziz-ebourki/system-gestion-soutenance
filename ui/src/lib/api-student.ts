import { api } from "./api-core";
import type {
  StudentStats, StudentDefenseDetails, StudentGroupWorkspace,
  StudentGroupDetails, StudentDocument, GroupDocument,
  PaginatedResponse,
} from "@/types";
import type { GroupDocumentType } from "@/types";

export const getStudentStats = () => api<StudentStats>("/student/stats");

export const getStudentDefense = () =>
  api<StudentDefenseDetails>("/student/defenses");

export const getStudentGroup = () =>
  api<StudentGroupWorkspace>("/student/groups");

export const getStudentDocuments = () =>
  api<PaginatedResponse<StudentDocument>>("/student/documents");

export const getGroupDocuments = (groupId: number) =>
  api<GroupDocument[]>(`/groups/${groupId}/documents`);

export const uploadGroupDocument = (groupId: number, type: GroupDocumentType, file: File) => {
  const formData = new FormData();
  formData.append("file", file);
  return api<GroupDocument>(`/groups/${groupId}/documents/${type}/attachments`, {
    method: "POST",
    body: formData,
  });
};

export const createStudentGroup = () =>
  api<StudentGroupDetails>("/student/groups", {
    method: "POST",
  });

export const joinStudentGroup = (groupId: number) =>
  api<StudentGroupDetails>(`/student/groups/${groupId}/members`, {
    method: "POST",
  });

export const getStudentConvocation = () =>
  api<Blob>("/student/convocations", { responseType: "blob" });

export const uploadStudentDocument = (documentId: number, file: File) => {
  const formData = new FormData();
  formData.append("file", file);
  return api<StudentDocument>(`/student/documents/${documentId}/attachments`, {
    method: "POST",
    body: formData,
  });
};
