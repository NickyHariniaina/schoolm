package hei.student.schoolm.repository;

import hei.student.schoolm.model.Course;
import hei.student.schoolm.model.CourseAssignment;
import hei.student.schoolm.model.Semester;
import hei.student.schoolm.repository.jpa.JCourseAssignmentRepository;
import hei.student.schoolm.repository.mapper.JCourseAssignmentMapper;
import hei.student.schoolm.repository.mapper.JCourseMapper;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@RequiredArgsConstructor
public class CourseAssignmentRepository {
  private final JCourseAssignmentRepository jCourseAssignmentRepository;
  private final JCourseAssignmentMapper jCourseAssignmentMapper;
  private final JCourseMapper jCourseMapper;

  @Transactional(readOnly = true)
  public Page<CourseAssignment> findFilterPaged(
      UUID groupId, UUID teacherId, UUID courseId, Integer academicYear, Pageable pageable) {
    return jCourseAssignmentRepository
        .findFilterPaged(groupId, teacherId, courseId, academicYear, pageable)
        .map(jCourseAssignmentMapper::toDomain);
  }

  @Transactional(readOnly = true)
  public Optional<CourseAssignment> findById(UUID id) {
    return jCourseAssignmentRepository.findById(id).map(jCourseAssignmentMapper::toDomain);
  }

  @Transactional(readOnly = true)
  public List<CourseAssignment> findByGroupIdAndAcademicYearAndSemester(
      UUID groupId, int academicYear, Semester semester) {
    return jCourseAssignmentRepository
        .findByGroupIdAndAcademicYearAndSemester(groupId, academicYear, semester)
        .stream()
        .map(jCourseAssignmentMapper::toDomain)
        .toList();
  }

  @Transactional(readOnly = true)
  public List<CourseAssignment> findByGroupIdInAndSemesterIn(
      Collection<UUID> groupIds, Collection<Semester> semesters) {
    return jCourseAssignmentRepository.findByGroupIdInAndSemesterIn(groupIds, semesters).stream()
        .map(jCourseAssignmentMapper::toDomain)
        .toList();
  }

  @Transactional(readOnly = true)
  public List<Course> findCurriculumCourses(
      Collection<UUID> groupIds, Collection<Semester> semesters) {
    return jCourseAssignmentRepository.findCurriculumCourses(groupIds, semesters).stream()
        .map(jCourseMapper::toDomainWithoutGroups)
        .toList();
  }

  @Transactional
  public CourseAssignment save(CourseAssignment courseAssignment) {
    return jCourseAssignmentMapper.toDomain(
        jCourseAssignmentRepository.save(jCourseAssignmentMapper.toEntity(courseAssignment)));
  }

  @Transactional
  public void delete(CourseAssignment courseAssignment) {
    jCourseAssignmentRepository.delete(jCourseAssignmentMapper.toEntity(courseAssignment));
  }

  @Transactional(readOnly = true)
  public boolean existsByCourseIdAndGroupIdAndAcademicYearAndSemester(
      UUID courseId, UUID groupId, int academicYear, Semester semester) {
    return jCourseAssignmentRepository.existsByCourseIdAndGroupIdAndAcademicYearAndSemester(
        courseId, groupId, academicYear, semester);
  }

  @Transactional(readOnly = true)
  public boolean existsByCourseId(UUID courseId) {
    return jCourseAssignmentRepository.existsByCourseId(courseId);
  }
}
