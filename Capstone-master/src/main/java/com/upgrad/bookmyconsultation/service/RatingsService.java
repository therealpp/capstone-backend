package com.upgrad.bookmyconsultation.service;

import com.upgrad.bookmyconsultation.entity.Doctor;
import com.upgrad.bookmyconsultation.entity.Rating;
import com.upgrad.bookmyconsultation.repository.DoctorRepository;
import com.upgrad.bookmyconsultation.repository.RatingsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;


@Service
public class RatingsService {

	@Autowired
	private ApplicationEventPublisher publisher;

	@Autowired
	private RatingsRepository ratingsRepository;

	@Autowired
	private DoctorRepository doctorRepository;

	public void submitRatings(Rating rating){
		rating.setId(UUID.randomUUID().toString());
		ratingsRepository.save(rating);
		//Optional<Doctor> doc = Optional.ofNullable(doctorRepository.findById(rating.getDoctorId())).get();
		Doctor doc = doctorRepository.findById(rating.getDoctorId()).get();
		doc.setRating((doc.getRating() + rating.getRating())/2);
		doctorRepository.save(doc);
	}
}
