package utc.englishlearning.Encybara.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import utc.englishlearning.Encybara.domain.request.schedule.ReqCreateScheduleDTO;
import utc.englishlearning.Encybara.domain.response.schedule.ResScheduleDTO;
import utc.englishlearning.Encybara.service.StudyScheduleService;
import utc.englishlearning.Encybara.domain.response.RestResponse;

@RestController
@RequestMapping("/api/v1/schedules")
public class StudyScheduleController {

    @Autowired
    private StudyScheduleService studyScheduleService;

    @PostMapping
    public ResponseEntity<RestResponse<ResScheduleDTO>> createSchedule(
            @RequestBody ReqCreateScheduleDTO reqCreateScheduleDTO) {
        ResScheduleDTO scheduleDTO = studyScheduleService.createSchedule(reqCreateScheduleDTO);
        RestResponse<ResScheduleDTO> response = new RestResponse<>();
        response.setStatusCode(200);
        response.setMessage("Schedule created successfully");
        response.setData(scheduleDTO);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<RestResponse<ResScheduleDTO>> updateSchedule(
            @PathVariable("id") Long id, @RequestBody ReqCreateScheduleDTO reqCreateScheduleDTO) {
        ResScheduleDTO scheduleDTO = studyScheduleService.updateSchedule(id, reqCreateScheduleDTO);
        RestResponse<ResScheduleDTO> response = new RestResponse<>();
        response.setStatusCode(200);
        response.setMessage("Schedule updated successfully");
        response.setData(scheduleDTO);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<RestResponse<Void>> deleteSchedule(@PathVariable("id") Long id) {
        studyScheduleService.deleteSchedule(id);
        RestResponse<Void> response = new RestResponse<>();
        response.setStatusCode(200);
        response.setMessage("Schedule deleted successfully");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<RestResponse<ResScheduleDTO>> getScheduleById(@PathVariable("id") Long id) {
        ResScheduleDTO scheduleDTO = studyScheduleService.getScheduleById(id);
        RestResponse<ResScheduleDTO> response = new RestResponse<>();
        response.setStatusCode(200);
        response.setMessage("Schedule retrieved successfully");
        response.setData(scheduleDTO);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<RestResponse<Page<ResScheduleDTO>>> getAllSchedulesByUserId(
            @PathVariable("userId") Long userId, Pageable pageable) {
        Page<ResScheduleDTO> schedules = studyScheduleService.getAllSchedulesByUserId(userId, pageable);
        RestResponse<Page<ResScheduleDTO>> response = new RestResponse<>();
        response.setStatusCode(200);
        response.setMessage("Schedules retrieved successfully");
        response.setData(schedules);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/course/{courseId}/user/{userId}")
    public ResponseEntity<RestResponse<List<ResScheduleDTO>>> getSchedulesByCourseIdAndUserId(
            @PathVariable("courseId") Long courseId, @PathVariable("userId") Long userId) {
        List<ResScheduleDTO> schedules = studyScheduleService.getAllSchedulesByCourseIdAndUserId(courseId, userId);
        RestResponse<List<ResScheduleDTO>> response = new RestResponse<>();
        response.setStatusCode(200);
        response.setMessage("Schedules retrieved successfully");
        response.setData(schedules);
        return ResponseEntity.ok(response);
    }
}