package tr.org.lkd.lyk2015.camp.controllers.validators;

import java.util.HashSet;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import tr.org.lkd.lyk2015.camp.models.Application.WorkStatus;
import tr.org.lkd.lyk2015.camp.models.Student;
import tr.org.lkd.lyk2015.camp.models.dto.ApplicationFormDto;
import tr.org.lkd.lyk2015.camp.service.BlacklistValidationService;
import tr.org.lkd.lyk2015.camp.service.ExamValidationService;
import tr.org.lkd.lyk2015.camp.service.TcknValidationService;

@Component
public class ApplicationFormValidator implements Validator {

	@Autowired
	TcknValidationService tcknValidationService;

	@Autowired
	BlacklistValidationService blacklistValidationService;

	@Autowired
	ExamValidationService examValidationService;

	@Override
	public boolean supports(Class<?> clazz) {

		return clazz.equals(ApplicationFormDto.class);
	}

	@Override
	public void validate(Object target, Errors errors) {

		ApplicationFormDto application = (ApplicationFormDto) target;

		System.out.println("====================Validation=========================");

		System.out.println(application);

		// prevent inconsistent working status
		if (application.getApplication().getWorkStatus() == WorkStatus.NOT_WORKING
				&& application.getApplication().getOfficer() == true) {

			errors.rejectValue("workStatus", "error.notWorkingOfficer", "Çalışmayan Memur olmaz...");
		}

		// check course selection size
		// application.getPreferredCourseIds().retainAll(Collections.singleton(null));

		if (application.getPreferredCourseIds().size() == 0) {
			errors.rejectValue("preferredCourseIds", "error.preferredCourseNoSelection",
					"En az bir kurs seçmelisiniz...");
		}

		// prevent same course selection
		int listSize = application.getPreferredCourseIds().size();

		Set<Long> set = new HashSet<>(application.getPreferredCourseIds());

		int setSize = set.size();

		if (listSize != setSize) {
			errors.rejectValue("preferredCourseIds", "error.preferredCourseIds", "Aynı kurs iki kez seçilemez...");
		}

		// validate tckn from web service
		Student student = application.getStudent();

		boolean tcknValid = this.tcknValidationService.validate(student.getName(), student.getSurname(),
				student.getTckn());

		if (!tcknValid) {

			errors.rejectValue("student.tckn", "error.tcknInvalid", "TC Kimlik no hatalı...");

		}

		// validate blacklist
		boolean blackListValidation = this.blacklistValidationService.validate(student.getTckn(), student.getEmail(),
				student.getName(), student.getSurname());

		if (!blackListValidation) {

			errors.rejectValue("student.tckn", "error.tcknBlacklist", "TC Kimlik No kara listededir.");
			errors.rejectValue("student.email", "error.emailInBlacklist", "Email kara listededir.");

		}

		// validate exam
		boolean examValidation = this.examValidationService.validate(student.getTckn(), student.getEmail());

		if (!examValidation) {

			errors.rejectValue("student.tckn", "error.tcknExamValidation", "Sınavı tamamlayınız.");
			errors.rejectValue("student.email", "error.emailExamValidaton", "Sınavı tamamlayınız.");

		}

	}

}
