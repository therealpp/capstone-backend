package com.upgrad.bookmyconsultation.service;

import com.upgrad.bookmyconsultation.entity.Appointment;
import com.upgrad.bookmyconsultation.exception.InvalidInputException;
import com.upgrad.bookmyconsultation.exception.ResourceUnAvailableException;
import com.upgrad.bookmyconsultation.exception.SlotUnavailableException;
import com.upgrad.bookmyconsultation.repository.AppointmentRepository;
import com.upgrad.bookmyconsultation.repository.UserRepository;
import com.upgrad.bookmyconsultation.util.ValidationUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;

@Service
@RequiredArgsConstructor
public class AppointmentService {

	@Autowired
	private AppointmentRepository appointmentRepository;

	@Autowired
	private UserRepository userRepository;


	public String appointment(Appointment appointment) throws InvalidInputException, SlotUnavailableException {

		ValidationUtils.validate(appointment);

		Appointment existingAppointment = appointmentRepository.findByDoctorIdAndTimeSlotAndAppointmentDate(appointment.getDoctorId(),
				appointment.getTimeSlot(), appointment.getAppointmentDate());

		if(existingAppointment != null){
			throw new SlotUnavailableException();
		}
		appointment.setAppointmentId("UUID-"+(UUID.randomUUID().toString()).getBytes()[0]);
		Appointment apt = appointmentRepository.save(appointment);
		return apt.getAppointmentId();
	}

	public Appointment getAppointment(String appointmentId){

		Appointment appointment = Optional.ofNullable(appointmentRepository.findById(appointmentId))
				.orElseThrow(() -> new ResourceUnAvailableException()).get();

		return appointment;

	}

	public List<Appointment> getAppointmentsForUser(String userId) {
		return appointmentRepository.findByUserId(userId);
	}
}
