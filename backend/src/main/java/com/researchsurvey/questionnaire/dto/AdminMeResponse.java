package com.researchsurvey.questionnaire.dto;

import java.util.List;

public record AdminMeResponse(String username, List<String> roles) {}
