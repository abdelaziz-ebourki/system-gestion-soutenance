package com.system_gestion_soutenance.api.student.group.service;

import com.system_gestion_soutenance.api.admin.config.settings.defense.entity.DefenseSettings;
import com.system_gestion_soutenance.api.admin.config.settings.defense.repository.DefenseSettingsRepository;
import com.system_gestion_soutenance.api.coordinator.group.entity.Group;
import com.system_gestion_soutenance.api.coordinator.group.repository.GroupRepository;
import com.system_gestion_soutenance.api.user.entity.Student;
import com.system_gestion_soutenance.api.user.repository.StudentRepository;
import com.system_gestion_soutenance.api.student.group.dto.AvailableGroupResponse;
import com.system_gestion_soutenance.api.student.group.dto.GroupDetailsResponse;
import com.system_gestion_soutenance.api.student.group.dto.GroupMemberResponse;
import com.system_gestion_soutenance.api.student.group.dto.StudentGroupWorkspaceResponse;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class StudentGroupService {

	private final GroupRepository groupRepository;
	private final StudentRepository studentRepository;
	private final DefenseSettingsRepository defenseSettingsRepository;

	public StudentGroupService(GroupRepository groupRepository, StudentRepository studentRepository,
			DefenseSettingsRepository defenseSettingsRepository) {
		this.groupRepository = groupRepository;
		this.studentRepository = studentRepository;
		this.defenseSettingsRepository = defenseSettingsRepository;
	}

	@Transactional(readOnly = true)
	public StudentGroupWorkspaceResponse getWorkspace(Long studentId) {
		Group currentGroup = groupRepository.findByStudentId(studentId).orElse(null);

		GroupDetailsResponse currentDetails = currentGroup != null ? groupToDetails(currentGroup, studentId) : null;

		List<AvailableGroupResponse> available = new ArrayList<>();
		for (Group g : groupRepository.findAllWithDetails()) {
			if (currentGroup == null || !g.getId().equals(currentGroup.getId())) {
				available.add(new AvailableGroupResponse(g.getId(), g.getGroupName(),
						g.getStudents() != null ? g.getStudents().size() : 0));
			}
		}

		DefenseSettings ds = defenseSettingsRepository.findById(1L).orElse(null);
		String startDate = ds != null ? ds.getGroupCreationStartDate() : "";
		String endDate = ds != null ? ds.getGroupCreationEndDate() : "";

		return new StudentGroupWorkspaceResponse(currentDetails, available, startDate, endDate,
				isCreationOpen(startDate, endDate));
	}

	@Transactional
	public GroupDetailsResponse createGroup(Long studentId) {
		if (groupRepository.findByStudentId(studentId).isPresent()) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Vous êtes déjà membre d'un groupe");
		}
		if (!isCreationPeriodOpen()) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La période de création de groupes est fermée");
		}

		Student student = studentRepository.findById(studentId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Étudiant introuvable"));

		Group group = new Group();
		group.setGroupName("Groupe de " + student.getFirstName() + " " + student.getLastName());
		group.setStudents(new ArrayList<>(List.of(student)));
		group.setSessionId(null);
		group = groupRepository.save(group);

		return groupToDetails(group, studentId);
	}

	@Transactional
	public GroupDetailsResponse joinGroup(Long groupId, Long studentId) {
		if (groupRepository.findByStudentId(studentId).isPresent()) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Vous êtes déjà membre d'un groupe");
		}
		if (!isCreationPeriodOpen()) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La période de création de groupes est fermée");
		}

		Group group = groupRepository.findById(groupId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Groupe non trouvé"));

		Student student = studentRepository.findById(studentId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Étudiant introuvable"));

		if (group.getStudents() == null) {
			group.setStudents(new ArrayList<>());
		}
		if (group.getStudents().stream().anyMatch(s -> s.getId().equals(studentId))) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Vous êtes déjà dans ce groupe");
		}
		group.getStudents().add(student);
		group = groupRepository.save(group);

		return groupToDetails(group, studentId);
	}

	private GroupDetailsResponse groupToDetails(Group group, Long currentStudentId) {
		List<GroupMemberResponse> members = new ArrayList<>();
		if (group.getStudents() != null) {
			boolean first = true;
			for (Student s : group.getStudents()) {
				members.add(new GroupMemberResponse(s.getId(), s.getFirstName() + " " + s.getLastName(), s.getEmail(),
						s.getId().equals(currentStudentId) && first ? "leader" : "member"));
				first = false;
			}
		}

		return new GroupDetailsResponse(group.getId(), group.getGroupName(),
				group.getProject() != null ? group.getProject().getTitle() : null,
				group.getProject() != null && group.getProject().getSupervisor() != null
						? group.getProject().getSupervisor().getFirstName() + " "
								+ group.getProject().getSupervisor().getLastName()
						: null,
				members);
	}

	private boolean isCreationPeriodOpen() {
		DefenseSettings ds = defenseSettingsRepository.findById(1L).orElse(null);
		if (ds == null)
			return false;
		return isCreationOpen(ds.getGroupCreationStartDate(), ds.getGroupCreationEndDate());
	}

	private boolean isCreationOpen(String startDate, String endDate) {
		try {
			LocalDate now = LocalDate.now();
			LocalDate start = LocalDate.parse(startDate);
			LocalDate end = LocalDate.parse(endDate);
			return !now.isBefore(start) && !now.isAfter(end);
		} catch (Exception e) {
			return false;
		}
	}
}
