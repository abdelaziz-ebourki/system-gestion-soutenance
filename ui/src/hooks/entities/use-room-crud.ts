import { useCreateRoom, useUpdateRoom, useDeleteRoom } from "@/hooks/queries";
import { roomSchema } from "@/lib/validations";
import { makeEntityCrudHook } from "./make-entity-crud";
import type { Room } from "@/types";

type RoomForm = {
  name: string;
  capacity: number;
  departmentId: string;
};

export const useRoomCrud = makeEntityCrudHook<Room, RoomForm, { name: string; capacity: number; departmentId: number }>({
  schema: roomSchema,
  defaultForm: { name: "", capacity: 0, departmentId: "" },
  useCreate: useCreateRoom,
  useUpdate: useUpdateRoom,
  useDelete: useDeleteRoom,
  toForm: (entity) => ({
    name: entity.name,
    capacity: entity.capacity,
    departmentId: String(entity.departmentId),
  }),
  toPayload: (form) => ({
    name: form.name,
    capacity: form.capacity,
    departmentId: Number(form.departmentId),
  }),
  messages: {
    created: "Salle ajoutée avec succès",
    updated: "Salle modifiée avec succès",
    deleted: "Salle supprimée",
  },
});
