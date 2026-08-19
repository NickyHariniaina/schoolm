package hei.student.schoolm.dto;

import hei.student.schoolm.model.Track;
import java.util.UUID;
import lombok.Builder;

@Builder
public record GroupResponse(UUID id, UUID cohortId, String cohortRef, String ref, Track track) {}
