package utc.englishlearning.Encybara.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import utc.englishlearning.Encybara.domain.StudySchedule;
import utc.englishlearning.Encybara.domain.request.schedule.ReqCreateScheduleDTO;
import utc.englishlearning.Encybara.domain.response.schedule.ResScheduleDTO;
import utc.englishlearning.Encybara.exception.ResourceNotFoundException;
import utc.englishlearning.Encybara.repository.StudyScheduleRepository;
import utc.englishlearning.Encybara.repository.UserRepository;
import utc.englishlearning.Encybara.repository.CourseRepository;
import utc.englishlearning.Encybara.exception.InvalidOperationException;

import java.util.List;

@Service
public class StudyScheduleService {

    @Autowired
    private StudyScheduleRepository studyScheduleRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Transactional
    public ResScheduleDTO createSchedule(ReqCreateScheduleDTO reqCreateScheduleDTO) {
        // Kiểm tra xem lịch học đã tồn tại chưa
        List<StudySchedule> existingSchedules = studyScheduleRepository.findByUserId(reqCreateScheduleDTO.getUserId());

        // Kiểm tra trùng thời gian
        for (StudySchedule existingSchedule : existingSchedules) {
            if (existingSchedule.getScheduleTime().equals(reqCreateScheduleDTO.getScheduleTime())) {
                throw new InvalidOperationException("Schedule time conflicts with an existing schedule");
            }
        }

        StudySchedule schedule = new StudySchedule();
        schedule.setUser(userRepository.findById(reqCreateScheduleDTO.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found")));
        schedule.setScheduleTime(reqCreateScheduleDTO.getScheduleTime());
        schedule.setDaily(reqCreateScheduleDTO.isDaily());

        if (reqCreateScheduleDTO.getCourseId() != null) {
            schedule.setCourse(courseRepository.findById(reqCreateScheduleDTO.getCourseId())
                    .orElseThrow(() -> new ResourceNotFoundException("Course not found")));
        }

        schedule = studyScheduleRepository.save(schedule);
        return convertToDTO(schedule);
    }

    public List<ResScheduleDTO> getAllSchedulesByUserId(Long userId) {
        List<StudySchedule> schedules = studyScheduleRepository.findByUserId(userId);
        return schedules.stream().map(this::convertToDTO).toList();
    }

    public Page<ResScheduleDTO> getAllSchedulesByUserId(Long userId, Pageable pageable) {
        return studyScheduleRepository.findByUserId(userId, pageable).map(this::convertToDTO);
    }

    public ResScheduleDTO getScheduleById(Long scheduleId) {
        StudySchedule schedule = studyScheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new ResourceNotFoundException("Schedule not found"));
        return convertToDTO(schedule);
    }

    @Transactional
    public void deleteSchedule(Long scheduleId) {
        StudySchedule schedule = studyScheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new ResourceNotFoundException("Schedule not found"));
        studyScheduleRepository.delete(schedule);
    }

    @Transactional
    public ResScheduleDTO updateSchedule(Long scheduleId, ReqCreateScheduleDTO reqCreateScheduleDTO) {
        StudySchedule schedule = studyScheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new ResourceNotFoundException("Schedule not found"));
        schedule.setScheduleTime(reqCreateScheduleDTO.getScheduleTime());
        schedule.setDaily(reqCreateScheduleDTO.isDaily());

        if (reqCreateScheduleDTO.getCourseId() != null) {
            schedule.setCourse(courseRepository.findById(reqCreateScheduleDTO.getCourseId())
                    .orElseThrow(() -> new ResourceNotFoundException("Course not found")));
        }

        schedule = studyScheduleRepository.save(schedule);
        return convertToDTO(schedule);
    }

    public List<ResScheduleDTO> getAllSchedulesByCourseIdAndUserId(Long courseId, Long userId) {
        List<StudySchedule> schedules = studyScheduleRepository.findByCourseIdAndUserId(courseId, userId);
        return schedules.stream().map(this::convertToDTO).toList();
    }

    private ResScheduleDTO convertToDTO(StudySchedule schedule) {
        ResScheduleDTO dto = new ResScheduleDTO();
        dto.setId(schedule.getId());
        dto.setScheduleTime(schedule.getScheduleTime());
        dto.setDaily(schedule.isDaily());
        dto.setUserId(schedule.getUser().getId());
        if (schedule.getCourse() != null) {
            dto.setCourseId(schedule.getCourse().getId());
        }
        return dto;
    }
}