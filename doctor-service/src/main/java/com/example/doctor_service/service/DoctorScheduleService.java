package com.example.doctor_service.service;

import com.example.doctor_service.model.DoctorSchedule;
import com.example.doctor_service.repository.DoctorScheduleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalTime;
import java.util.List;

@Service
public class DoctorScheduleService {

   
   //@Value("${APPOINTMENT_SERVICE_URL:http://appointment-service:8084}")

   @Value("${DOCTOR_SERVICE_URL:https://appointment-service-hxup.onrender.com}")
       

    private String appointmentServiceUrl;

    @Autowired
    private DoctorScheduleRepository repository;

    @Autowired
    private RestTemplate restTemplate;

    public DoctorSchedule createSchedule(DoctorSchedule schedule) {
        validateScheduleInput(schedule);

        List<DoctorSchedule> existingSchedules = repository.findByDoctorIdAndDayOfWeek(
            schedule.getDoctorId(),
            schedule.getDayOfWeek()
        );

        boolean overlappingExists = existingSchedules.stream().anyMatch(existing ->
            isOverlapping(existing.getStartTime(), existing.getEndTime(),
                schedule.getStartTime(), schedule.getEndTime())
        );

        if (overlappingExists) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Schedule overlaps with an existing time slot for this doctor"
            );
        }

        return repository.save(schedule);
    }

    public DoctorSchedule getSchedule(Long id) {
        return repository.findById(id).orElse(null);
    }

    public List<DoctorSchedule> getSchedulesByDoctorId(Long doctorId) {
        return repository.findByDoctorId(doctorId);
    }

    public List<DoctorSchedule> getAllSchedules() {
        return repository.findAll();
    }

    public List<DoctorSchedule> getSchedulesByHospitalId(Long hospitalId) {
        return repository.findByHospitalId(hospitalId);
    }

    public DoctorSchedule updateScheduleDay(Long scheduleId, DayOfWeek dayOfWeek) {
        if (dayOfWeek == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "dayOfWeek is required");
        }

        DoctorSchedule existingSchedule = repository.findById(scheduleId)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Schedule not found with ID: " + scheduleId
            ));

        List<DoctorSchedule> existingSchedules = repository.findByDoctorIdAndDayOfWeek(
            existingSchedule.getDoctorId(),
            dayOfWeek
        );

        boolean overlappingExists = existingSchedules.stream()
            .filter(s -> !s.getScheduleId().equals(scheduleId))
            .anyMatch(s -> isOverlapping(
                s.getStartTime(),
                s.getEndTime(),
                existingSchedule.getStartTime(),
                existingSchedule.getEndTime()
            ));

        if (overlappingExists) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Schedule overlaps with an existing time slot for this doctor"
            );
        }

        existingSchedule.setDayOfWeek(dayOfWeek);
        return repository.save(existingSchedule);
    }

    public void deleteSchedule(Long scheduleId) {
        if (!repository.existsById(scheduleId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Schedule not found with ID: " + scheduleId);
        }
        repository.deleteById(scheduleId);
    }

    public int calculateTotalSlots(Long scheduleId) {
        DoctorSchedule schedule = repository.findById(scheduleId)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Schedule not found with ID: " + scheduleId
            ));

        if (schedule.getStartTime() == null || schedule.getEndTime() == null || schedule.getSlotDuration() <= 0) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Invalid schedule data for ID: " + scheduleId
            );
        }

        long minutes = Duration.between(
                schedule.getStartTime(),
                schedule.getEndTime()
        ).toMinutes();

        if (minutes <= 0) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "End time must be after start time for schedule ID: " + scheduleId
            );
        }

        return (int) (minutes / schedule.getSlotDuration());
    }

    public long getBookedSlots(Long scheduleId) {
        String url = appointmentServiceUrl + "/appointments/count/" + scheduleId;

        Long booked;
        try {
            booked = restTemplate.getForObject(url, Long.class);
        } catch (RestClientException e) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "Unable to fetch booked slots from appointment-service",
                    e
            );
        }

        return booked == null ? 0L : booked;
    }

    public int remainingSlots(Long scheduleId) {

        int total = calculateTotalSlots(scheduleId);
        long booked = getBookedSlots(scheduleId);

        return (int) Math.max((long) total - booked, 0L);
    }

    private void validateScheduleInput(DoctorSchedule schedule) {
        if (schedule == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Schedule payload is required");
        }

        if (schedule.getDoctorId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "doctorId is required");
        }

        if (schedule.getDayOfWeek() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "dayOfWeek is required");
        }

        if (schedule.getStartTime() == null || schedule.getEndTime() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "startTime and endTime are required");
        }

        if (!schedule.getEndTime().isAfter(schedule.getStartTime())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "endTime must be after startTime");
        }

        if (schedule.getSlotDuration() <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "slotDuration must be greater than 0");
        }
    }

    private boolean isOverlapping(LocalTime existingStart, LocalTime existingEnd,
                                  LocalTime newStart, LocalTime newEnd) {
        return newStart.isBefore(existingEnd) && newEnd.isAfter(existingStart);
    }
}
