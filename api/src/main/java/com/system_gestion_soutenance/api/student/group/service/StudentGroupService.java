package com.system_gestion_soutenance.api.student.group.service;

import com.system_gestion_soutenance.api.admin.config.settings.defense.entity.DefenseSettings;
import com.system_gestion_soutenance.api.admin.config.settings.defense.repository.DefenseSettingsRepository;
import com.system_gestion_soutenance.api.common.mapper.StudentGroupMapper;
import com.system_gestion_soutenance.api.coordinator.group.entity.Group;
import com.system_gestion_soutenance.api.coordinator.group.repository.GroupRepository;
import com.system_gestion_soutenance.api.user.entity.Student;
import com.system_gestion_soutenance.api.user.repository.StudentRepository;
import com.system_gestion_soutenance.api.student.group.dto.AvailableGroupResponse;
import com.system_gestion_soutenance.api.student.group.dto.StudentGroupWorkspaceResponse;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import com.system_gestion_soutenance.api.common.exception.EntityNotFoundException;
import com.system_gestion_soutenance.api.common.exception.InvalidBusinessStateException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StudentGroupService {

	private final GroupRepository groupRepository;
	private final StudentRepository studentRepository;
	private final DefenseSettingsRepository defenseSettingsRepository;
	private final StudentGroupMapper studentGroupMapper;

	public StudentGroupService(GroupRepository groupRepository, StudentRepository studentRepository,
			DefenseSettingsRepository defenseSettingsRepository, StudentGroupMapper studentGroupMapper) {
		this.groupRepository = groupRepository;
		this.studentRepository = studentRepository;
		this.defenseSettingsRepository = defenseSettingsRepository;
		this.studentGroupMapper = studentGroupMapper;
	}

	@Transactional(readOnly = true)
	public StudentGroupWorkspaceResponse getWorkspace(Long studentId) {
		Group currentGroup = groupRepository.findByStudentId(studentId).orElse(null);

		com.system_gestion_soutenance.api.student.group.dto.GroupDetailsResponse currentDetails = currentGroup != null
				? studentGroupMapper.toDetails(currentGroup, studentId)
				: null;

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
	public Group createGroup(Long studentId) {
		if (groupRepository.findByStudentId(studentId).isPresent()) {
			throw new InvalidBusinessStateException("Vous êtes déjà membre d'un groupe");
		}
		if (!isCreationPeriodOpen()) {
			throw new InvalidBusinessStateException("La période de création de groupes est fermée");
		}

		Student student = studentRepository.findById(studentId)
				.orElseThrow(() -> new InvalidBusinessStateException("Étudiant introuvable"));

		Group group = new Group();
		group.setGroupName("Groupe de " + student.getFirstName() + " " + student.getLastName());
		group.setStudents(new ArrayList<>(List.of(student)));
		group.setSessionId(null);
		return groupRepository.save(group);
	}

	@Transactional
	public Group joinGroup(Long groupId, Long studentId) {
		if (groupRepository.findByStudentId(studentId).isPresent()) {
			throw new InvalidBusinessStateException("Vous êtes déjà membre d'un groupe");
		}
		if (!isCreationPeriodOpen()) {
			throw new InvalidBusinessStateException("La période de création de groupes est fermée");
		}

		Group group = groupRepository.findById(groupId)
				.orElseThrow(() -> new EntityNotFoundException("Groupe non trouvé"));

		Student student = studentRepository.findById(studentId)
				.orElseThrow(() -> new InvalidBusinessStateException("Étudiant introuvable"));

		if (group.getStudents() == null) {
			group.setStudents(new ArrayList<>());
		}
		if (group.getStudents().stream().anyMatch(s -> s.getId().equals(studentId))) {
			throw new InvalidBusinessStateException("Vous êtes déjà dans ce groupe");
		}
		group.getStudents().add(student);
		return groupRepository.save(group);
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
