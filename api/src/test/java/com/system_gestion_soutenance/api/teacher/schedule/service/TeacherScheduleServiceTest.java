package com.system_gestion_soutenance.api.teacher.schedule.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.system_gestion_soutenance.api.coordinator.group.repository.GroupRepository;
import com.system_gestion_soutenance.api.coordinator.jury.entity.Jury;
import com.system_gestion_soutenance.api.coordinator.jury.entity.JuryMember;
import com.system_gestion_soutenance.api.coordinator.jury.repository.JuryRepository;
import com.system_gestion_soutenance.api.coordinator.project.entity.Project;
import com.system_gestion_soutenance.api.coordinator.project.repository.ProjectRepository;
import com.system_gestion_soutenance.api.coordinator.schedule.entity.SlotAssignment;
import com.system_gestion_soutenance.api.coordinator.schedule.repository.SlotAssignmentRepository;
import com.system_gestion_soutenance.api.user.entity.Teacher;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class TeacherScheduleServiceTest {

  private final SlotAssignmentRepository slotAssignmentRepository =
      mock(SlotAssignmentRepository.class);
  private final JuryRepository juryRepository = mock(JuryRepository.class);
  private final ProjectRepository projectRepository = mock(ProjectRepository.class);
  private final GroupRepository groupRepository = mock(GroupRepository.class);

  private final TeacherScheduleService service =
      new TeacherScheduleService(
          slotAssignmentRepository, juryRepository, projectRepository, groupRepository);

  @Test
  void getSchedule_noData_returnsEmpty() {
    when(juryRepository.findAll()).thenReturn(List.of());
    when(projectRepository.findAll()).thenReturn(List.of());
    when(groupRepository.findAll()).thenReturn(List.of());
    when(slotAssignmentRepository.findAll()).thenReturn(List.of());

    assertTrue(service.getSchedule(1L).isEmpty());
  }

  @Test
  void getSchedule_withJuryAndSlot_returnsSchedule() {
    Project project = mock(Project.class);
    when(project.getId()).thenReturn(10L);
    when(project.getTitle()).thenReturn("Projet Test");
    when(project.getSupervisor()).thenReturn(null);

    Teacher teacher = mock(Teacher.class);
    when(teacher.getId()).thenReturn(1L);

    JuryMember member = mock(JuryMember.class);
    when(member.getTeacher()).thenReturn(teacher);
    when(member.getRoleName()).thenReturn("président");

    Jury jury = mock(Jury.class);
    when(jury.getProject()).thenReturn(project);
    when(jury.getMembers()).thenReturn(List.of(member));

    SlotAssignment slot = mock(SlotAssignment.class);
    when(slot.getProjectId()).thenReturn(10L);

    when(juryRepository.findAll()).thenReturn(List.of(jury));
    when(projectRepository.findAll()).thenReturn(List.of(project));
    when(projectRepository.findById(10L)).thenReturn(Optional.of(project));
    when(groupRepository.findAll()).thenReturn(List.of());
    when(slotAssignmentRepository.findAll()).thenReturn(List.of(slot));

    var result = service.getSchedule(1L);

    assertEquals(1, result.size());
    assertEquals("Projet Test", result.get(0).get("projectTitle"));
  }
}
