package usc_catalogue_converter;


public class Course implements Comparable<Course>{
	private static String CONCURRENT_ENROLLMENT_PREFIX =  "Concurrent enrollment";
	private static String LATEX_SEPERATOR = " | ";
	
	private String course_type_;
	private String prefix_;
	private String code_;
	private String suffix_;
	private String name_;
	private String units_;
	private String max_units_;
	private String terms_offered_;
	private String description_;
	private String prerequisite_str_;
	private String recommend_prep_;
	private String corequisite_str_;
	private String concurrent_enrollment_; //Neet to check whether "concurrent enrollment" string exist
	private String sat_new_ge_req_;
	private String sat_global_per_;
	private String sat_old_ge_req_;
	private String reg_restriction_; 
	private String credit_restriction_;
	private String duplicate_credit_;
	private String instruction_mode_;
	private String grading_options_;
	private String crosslist_as_;
	public Course(String course_type_, String prefix_, String code_, String suffix_, String name_, String units_, String max_units_,
			String terms_offered_, String description_, String prerequisite_str_, String recommend_prep_,
			String corequisite_str_, String concurrent_enrollment_, String sat_new_ge_req_, String sat_global_per_,
			String sat_old_ge_req_, String reg_restriction_, String credit_restriction_, String duplicate_credit_,
			String instruction_mode_, String grading_options_, String crosslist_as_) {
		super();
		this.course_type_ = course_type_;
		this.prefix_ = prefix_;
		this.code_ = code_;
		this.suffix_ = suffix_;
		this.name_ = name_;
		this.units_ = units_;
		this.max_units_ = max_units_;
		this.terms_offered_ = terms_offered_;
		this.description_ = description_;
		this.prerequisite_str_ = prerequisite_str_;
		this.recommend_prep_ = recommend_prep_;
		this.corequisite_str_ = corequisite_str_;
		this.concurrent_enrollment_ = concurrent_enrollment_;
		this.sat_new_ge_req_ = sat_new_ge_req_;
		this.sat_global_per_ = sat_global_per_;
		this.sat_old_ge_req_ = sat_old_ge_req_;
		this.reg_restriction_ = reg_restriction_;
		this.credit_restriction_ = credit_restriction_;
		this.duplicate_credit_ = duplicate_credit_;
		this.instruction_mode_ = instruction_mode_;
		this.grading_options_ = grading_options_;
		this.crosslist_as_ = crosslist_as_;
	}
	
	public String GetCourseType() {
		return course_type_;
	}
	
	public String GetTitle() {
		return prefix_ + " " + code_ + suffix_ + " " + name_;
	}
	
	public String GetUnitsStr() {
		return (units_.isEmpty() ? "" : ("Units: " + units_+ LATEX_SEPERATOR) );
	}
	
	public String GetTermsOfferedStr() {
		return (terms_offered_.isEmpty() ? "" : ("Terms Offered: " + terms_offered_+ LATEX_SEPERATOR) );
	}
	
	public String GetMaxUnitsStr() {
		return (max_units_.isEmpty() ? "" : ("Max Units: " + max_units_+ LATEX_SEPERATOR) );
	}
	
	public String GetDescriptionStr() {
		return description_.isEmpty() ?  "" : (description_+ LATEX_SEPERATOR);
	}
	
	public String GetPrerequisiteStr() {
		return (prerequisite_str_.isEmpty() ? "" : ("Prequisite: " + prerequisite_str_+ LATEX_SEPERATOR) );
	}

	public String GetRecommenPrepStr() {
		return (recommend_prep_.isEmpty() ? "" : ("Recommended Preparation: " + recommend_prep_+ LATEX_SEPERATOR) );
	}	
	
	public String GetCorequisiteStr() {
		return (corequisite_str_.isEmpty() ? "" : ("Corequisite: " + corequisite_str_+ LATEX_SEPERATOR) );
	}	
	
	public String GetConcurrentEnrollmentStr() {
		return (concurrent_enrollment_.isEmpty() ? 
				"" : ((concurrent_enrollment_.toLowerCase().contains(CONCURRENT_ENROLLMENT_PREFIX.toLowerCase()) ? 
						concurrent_enrollment_ :  "Concurrent Enrollment: " + concurrent_enrollment_)+ LATEX_SEPERATOR) );
	}	
	
	public String GetSatNewGeReqStr() {
		return (sat_new_ge_req_.isEmpty() ? "" : ("Satisfies New General Education in " + sat_new_ge_req_+ LATEX_SEPERATOR));
	}	
	
	public String GetGlobalPerStr() {
		return (sat_global_per_.isEmpty() ? "" : ("Satisfies Global Perspective in " + sat_global_per_+ LATEX_SEPERATOR));
	}	
	
	public String GetSatOldGeReqStr() {
		return (sat_old_ge_req_.isEmpty() ? "" : ("Satisfies Old General Education in " + sat_old_ge_req_+ LATEX_SEPERATOR));
	}	
	
	public String GetRegRestrictionStr() {
		return (reg_restriction_.isEmpty() ? "" : ("Registration Restriction: " + reg_restriction_+ LATEX_SEPERATOR));
	}	
	
	public String GetCreditRestrictionStr() {
		return (credit_restriction_.isEmpty() ? "" : ("Credit Restriction: " + credit_restriction_+ LATEX_SEPERATOR));
	}	

	public String GetDuplicateCreditStr() {
		return (duplicate_credit_.isEmpty() ? "" : ("Duplicates Credit in " + duplicate_credit_+ LATEX_SEPERATOR));
	}	
	
	public String GetInstructionModeStr() {
		return (instruction_mode_.isEmpty() ? "" : ("Instruction Mode: " + instruction_mode_+ LATEX_SEPERATOR));
	}	
	
	public String GetGradingOptionsStr() {
		return (grading_options_.isEmpty() ? "" : ("Grading Options: " + grading_options_+ LATEX_SEPERATOR));
	}	

	public String GetCrossListAsStr() {
		return (crosslist_as_.isEmpty() ? "" : ("Crosslisted as " + crosslist_as_+ LATEX_SEPERATOR));
	}	
	
	public String GetParagraph() {
		String p = GetUnitsStr() + GetTermsOfferedStr() + 
				GetMaxUnitsStr() + GetDescriptionStr() + 
				GetPrerequisiteStr()  + GetRecommenPrepStr()  + 
				GetCorequisiteStr() + GetConcurrentEnrollmentStr()  + 
				GetSatNewGeReqStr() + GetGlobalPerStr()  + 
				GetSatOldGeReqStr() + GetRegRestrictionStr()  + 
				GetCreditRestrictionStr() + GetDuplicateCreditStr() + 
				GetInstructionModeStr() + GetGradingOptionsStr() + 
				GetCrossListAsStr();
		int endIndex = p.lastIndexOf(LATEX_SEPERATOR);
		if(endIndex != -1)
			return p.substring(0, endIndex);
		else
			return p;
	}
	
	
	@Override
	public String toString() {
		return "Course [prefix_=" + prefix_ + ", code_=" + code_ + ", Suffix_=" + suffix_ + ", name_=" + name_
				+ ", units_=" + units_ + ", max_units_=" + max_units_ + ", terms_offered_=" + terms_offered_
				+ ", description_=" + description_ + ", prerequisite_str_=" + prerequisite_str_ + ", recommend_prep_="
				+ recommend_prep_ + ", corequisite_str_=" + corequisite_str_ + ", concurrent_enrollment_="
				+ concurrent_enrollment_ + ", sat_new_ge_req_=" + sat_new_ge_req_ + ", sat_global_per_="
				+ sat_global_per_ + ", sat_old_ge_req_=" + sat_old_ge_req_ + ", reg_restriction_=" + reg_restriction_
				+ ", credit_restriction_=" + credit_restriction_ + ", duplicate_credit_=" + duplicate_credit_
				+ ", instruction_mode_=" + instruction_mode_ + ", grading_options_=" + grading_options_
				+ ", crosslist_as_=" + crosslist_as_ + "]";
	}
	
	@Override
	public int compareTo(Course o) {
		return GetTitle().compareTo(o.GetTitle());
	}
}
