package com.upgrad.bookmyconsultation.service;

import com.upgrad.bookmyconsultation.entity.Address;
import com.upgrad.bookmyconsultation.entity.Doctor;
import com.upgrad.bookmyconsultation.enums.Speciality;
import com.upgrad.bookmyconsultation.exception.InvalidInputException;
import com.upgrad.bookmyconsultation.exception.ResourceUnAvailableException;
import com.upgrad.bookmyconsultation.model.TimeSlot;
import com.upgrad.bookmyconsultation.repository.AddressRepository;
import com.upgrad.bookmyconsultation.repository.AppointmentRepository;
import com.upgrad.bookmyconsultation.repository.DoctorRepository;
import com.upgrad.bookmyconsultation.util.ValidationUtils;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import springfox.documentation.annotations.Cacheable;

import java.util.*;
import java.util.stream.Collectors;

@Log4j2
@Service
public class DoctorService {
	@Autowired
	private AppointmentRepository appointmentRepository;
	@Autowired
	private DoctorRepository doctorRepository;
	@Autowired
	private AddressRepository addressRepository;


	//Work
	public Doctor register(Doctor doctor) throws InvalidInputException{
		ValidationUtils.validate(doctor);
		byte b = (UUID.randomUUID().toString().getBytes())[0];
		doctor.setId("UUID-" + b);
		if(doctor.getSpeciality() == null){
			doctor.setSpeciality(Speciality.GENERAL_PHYSICIAN);
		}

		Address address = new Address();
		address.setId(doctor.getAddress().getId());
		address.setAddressLine1(doctor.getAddress().getAddressLine1());
		address.setAddressLine2(doctor.getAddress().getAddressLine2());
		address.setCity(doctor.getAddress().getCity());
		address.setState(doctor.getAddress().getState());
		address.setPostcode(doctor.getAddress().getPostcode());

		Address storedAddress = addressRepository.save(address);
		doctor.setAddress(storedAddress);
		doctorRepository.save(doctor);
		return doctor;
	}

	//Work
	public Doctor getDoctor(String id){
		Doctor doctor = Optional.ofNullable(doctorRepository.findById(id))
				.orElseThrow(() -> new ResourceUnAvailableException()).get();
		return doctor;
	}

	public List<Doctor> getAllDoctorsWithFilters(String speciality) {

		if (speciality != null && !speciality.isEmpty()) {
			return doctorRepository.findBySpecialityOrderByRatingDesc(Speciality.valueOf(speciality));
		}
		return getActiveDoctorsSortedByRating();
	}

	@Cacheable(value = "doctorListByRating")
	private List<Doctor> getActiveDoctorsSortedByRating() {
		log.info("Fetching doctor list from the database");
		return doctorRepository.findAllByOrderByRatingDesc()
				.stream()
				.limit(20)
				.collect(Collectors.toList());
	}

	public TimeSlot getTimeSlots(String doctorId, String date) {

		TimeSlot timeSlot = new TimeSlot(doctorId, date);
		timeSlot.setTimeSlot(timeSlot.getTimeSlot()
				.stream()
				.filter(slot -> {
					return appointmentRepository
							.findByDoctorIdAndTimeSlotAndAppointmentDate(timeSlot.getDoctorId(), slot, timeSlot.getAvailableDate()) == null;

				})
				.collect(Collectors.toList()));

		return timeSlot;

	}
}
