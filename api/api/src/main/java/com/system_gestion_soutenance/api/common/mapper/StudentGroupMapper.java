package com.system_gestion_soutenance.api.common.mapper;

import com.system_gestion_soutenance.api.coordinator.group.entity.Group;
import com.system_gestion_soutenance.api.coordinator.project.entity.Project;
import com.system_gestion_soutenance.api.student.group.dto.GroupDetailsResponse;
import com.system_gestion_soutenance.api.student.group.dto.GroupMemberResponse;
import com.system_gestion_soutenance.api.user.entity.Student;
import java.util.ArrayList;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
@SuppressWarnings("PMD")

@Mapper(config = CentralMapperConfig.class)
public interface StudentGroupMapper {

	@Mapping(target = "projectTitle", source = "group.project.title")
	@Mapping(target = "supervisorName", expression = "java(resolveSupervisorName(group))")
	@Mapping(target = "members", expression = "java(resolveMembers(group, currentStudentId))")
	GroupDetailsResponse toDetails(Group group, Long currentStudentId);

	default String resolveSupervisorName(Group group) {
		Project project = group.getProject();
		if (project == null || project.getSupervisor() == null)
			return null;
		return project.getSupervisor().getFirstName() + " " + project.getSupervisor().getLastName();
	}

	default List<GroupMemberResponse> resolveMembers(Group group, Long currentStudentId) {
		List<Student> students = group.getStudents();
		if (students == null)
			return List.of();
		Long leaderId = group.getLeaderId();
		List<GroupMemberResponse> members = new ArrayList<>();
		for (Student s : students) {
			String role = s.getId().equals(leaderId) ? "leader" : "member";
			members.add(
					new GroupMemberResponse(s.getId(), s.getFirstName() + " " + s.getLastName(), s.getEmail(), role));
		}
		return members;
	}
}