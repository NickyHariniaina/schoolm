package hei.student.schoolm.repository.jpa;

import hei.student.schoolm.model.Semester;
import hei.student.schoolm.repository.model.JCourseAssignment;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface JCourseAssignmentRepository extends JpaRepository<JCourseAssignment, UUID> {
  @Query(
      """
      select ca from JCourseAssignment ca
      where (cast(:groupId as uuid) is null or ca.group.id = :groupId)
        and (cast(:teacherId as uuid) is null
          or exists (select t from ca.teachers t where t.id = :teacherId))
        and (cast(:courseId as uuid) is null or ca.course.id = :courseId)
        and (cast(:academicYear as integer) is null or ca.academicYear = :academicYear)
      """)
  Page<JCourseAssignment> findFilterPaged(
      @Param("groupId") UUID groupId,
      @Param("teacherId") UUID teacherId,
      @Param("courseId") UUID courseId,
      @Param("academicYear") Integer academicYear,
      Pageable pageable);

  List<JCourseAssignment> findByGroupIdAndAcademicYearAndSemester(
      UUID groupId, int academicYear, Semester semester);

  List<JCourseAssignment> findByGroupIdInAndSemesterIn(
      Collection<UUID> groupIds, Collection<Semester> semesters);

  boolean existsByCourseIdAndGroupIdAndAcademicYearAndSemester(
      UUID courseId, UUID groupId, int academicYear, Semester semester);
}
