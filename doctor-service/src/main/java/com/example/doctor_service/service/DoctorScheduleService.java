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

import java.time.Duration;
import java.util.List;

@Service
public class DoctorScheduleService {

    @Value("${appointment.service.url:http://localhost:8082}")
    private String appointmentServiceUrl;

    @Autowired
    private DoctorScheduleRepository repository;

    @Autowired
    private RestTemplate restTemplate;

    public DoctorSchedule createSchedule(DoctorSchedule schedule) {
        return repository.save(schedule);
    }

    public DoctorSchedule getSchedule(Long id) {
        return repository.findById(id).orElse(null);
    }

    public List<DoctorSchedule> getSchedulesByDoctorId(Long doctorId) {
        return repository.findByDoctorId(doctorId);
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

    public int getBookedSlots(Long scheduleId) {
        String url = appointmentServiceUrl + "/appointments/count/" + scheduleId;

        Integer booked;
        try {
            booked = restTemplate.getForObject(url, Integer.class);
        } catch (RestClientException e) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "Unable to fetch booked slots from appointment-service",
                    e
            );
        }

        return booked == null ? 0 : booked;
    }

    public int remainingSlots(Long scheduleId) {

        int total = calculateTotalSlots(scheduleId);
        int booked = getBookedSlots(scheduleId);

        return Math.max(total - booked, 0);
    }
}