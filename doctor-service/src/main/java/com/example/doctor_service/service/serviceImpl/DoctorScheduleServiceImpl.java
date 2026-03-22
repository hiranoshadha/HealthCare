package com.example.doctor_service.service.serviceImpl;

import com.example.doctor_service.model.DoctorSchedule;
import com.example.doctor_service.repository.DoctorScheduleRepository;
import com.example.doctor_service.service.DoctorScheduleService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class DoctorScheduleServiceImpl implements DoctorScheduleService {

    private final DoctorScheduleRepository doctorScheduleRepository;
    private final RestTemplate restTemplate;

    @Value("${appointment.service.url}")
    private String appointmentServiceUrl;

    @Value("${user.service.url}")
    private String userServiceUrl;

    @Value("${hospital.service.url}")
    private String hospitalServiceUrl;

    private void verifyDoctorExists(Long doctorId) {
        String url = userServiceUrl + "/api/users/doctors/" + doctorId;
        try {
            restTemplate.getForObject(url, Object.class);
        } catch (HttpClientErrorException.NotFound e) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Doctor not found with ID: " + doctorId
            );
        } catch (RestClientException e) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "Unable to verify doctor with user-service",
                    e
            );
        }
    }

    private void verifyHospitalExists(Long hospitalId) {
        String url = hospitalServiceUrl + "/api/hospitals/" + hospitalId;
        try {
            restTemplate.getForObject(url, Object.class);
        } catch (HttpClientErrorException.NotFound e) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Hospital not found with ID: " + hospitalId
            );
        } catch (RestClientException e) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "Unable to verify hospital with hospital-service",
                    e
            );
        }
    }

    @Override
    public DoctorSchedule createSchedule(DoctorSchedule schedule) {
        validateScheduleInput(schedule);

        verifyDoctorExists(schedule.getDoctorId());

        if (schedule.getHospitalId() != null) {
            verifyHospitalExists(schedule.getHospitalId());
        }

        List<DoctorSchedule> existingSchedules = doctorScheduleRepository.findByDoctorIdAndDayOfWeek(
                schedule.getDoctorId(),
                schedule.getDayOfWeek()
        );

        boolean overlappingExists = existingSchedules.stream().anyMatch(existing ->
                isOverlapping(
                        existing.getStartTime(), existing.getEndTime(),
                        schedule.getStartTime(), schedule.getEndTime()
                )
        );

        if (overlappingExists) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Schedule overlaps with an existing time slot for doctor ID: " + schedule.getDoctorId()
            );
        }

        return doctorScheduleRepository.save(schedule);
    }

    @Override
    public DoctorSchedule getScheduleById(Long scheduleId) {
        return doctorScheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Schedule not found with ID: " + scheduleId
                ));
    }

    @Override
    public List<DoctorSchedule> getSchedulesByDoctorId(Long doctorId) {
        verifyDoctorExists(doctorId);
        return doctorScheduleRepository.findByDoctorId(doctorId);
    }

    @Override
    public List<DoctorSchedule> getAllSchedules() {
        return doctorScheduleRepository.findAll();
    }

    @Override
    public List<DoctorSchedule> getSchedulesByHospitalId(Long hospitalId) {
        verifyHospitalExists(hospitalId);
        return doctorScheduleRepository.findByHospitalId(hospitalId);
    }

    @Override
    public DoctorSchedule updateScheduleDay(Long scheduleId, DayOfWeek dayOfWeek) {
        if (dayOfWeek == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "dayOfWeek is required");
        }

        DoctorSchedule existingSchedule = doctorScheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Schedule not found with ID: " + scheduleId
                ));

        List<DoctorSchedule> schedulesOnDay = doctorScheduleRepository.findByDoctorIdAndDayOfWeek(
                existingSchedule.getDoctorId(),
                dayOfWeek
        );

        boolean overlappingExists = schedulesOnDay.stream()
                .filter(s -> !s.getScheduleId().equals(scheduleId))
                .anyMatch(s -> isOverlapping(
                        s.getStartTime(), s.getEndTime(),
                        existingSchedule.getStartTime(), existingSchedule.getEndTime()
                ));

        if (overlappingExists) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Updating to " + dayOfWeek + " would overlap with an existing slot for doctor ID: "
                            + existingSchedule.getDoctorId()
            );
        }

        existingSchedule.setDayOfWeek(dayOfWeek);
        return doctorScheduleRepository.save(existingSchedule);
    }

    @Override
    public void deleteSchedule(Long scheduleId) {
        if (!doctorScheduleRepository.existsById(scheduleId)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Schedule not found with ID: " + scheduleId
            );
        }
        doctorScheduleRepository.deleteById(scheduleId);
    }

    @Override
    public int calculateTotalSlots(Long scheduleId) {
        DoctorSchedule schedule = doctorScheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Schedule not found with ID: " + scheduleId
                ));

        if (schedule.getStartTime() == null || schedule.getEndTime() == null
                || schedule.getSlotDuration() <= 0) {
            throw new ResponseStatusException(
                    HttpStatus.UNPROCESSABLE_ENTITY,
                    "Invalid schedule data for ID: " + scheduleId
            );
        }

        long minutes = Duration.between(schedule.getStartTime(), schedule.getEndTime()).toMinutes();

        if (minutes <= 0) {
            throw new ResponseStatusException(
                    HttpStatus.UNPROCESSABLE_ENTITY,
                    "End time must be after start time for schedule ID: " + scheduleId
            );
        }

        return (int) (minutes / schedule.getSlotDuration());
    }

    @Override
    public long getBookedSlots(Long scheduleId) {
        String url = appointmentServiceUrl + "/api/appointments/count/" + scheduleId;
        try {
            Long booked = restTemplate.getForObject(url, Long.class);
            return booked == null ? 0L : booked;
        } catch (RestClientException e) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "Unable to fetch booked slots from appointment-service",
                    e
            );
        }
    }

    @Override
    public int getRemainingSlots(Long scheduleId) {
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