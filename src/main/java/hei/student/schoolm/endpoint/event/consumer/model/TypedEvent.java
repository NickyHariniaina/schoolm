package hei.student.schoolm.endpoint.event.consumer.model;

import hei.student.schoolm.PojaGenerated;
import hei.student.schoolm.endpoint.event.model.PojaEvent;

@PojaGenerated
public record TypedEvent(String typeName, PojaEvent payload) {}
