export type YesNoUnknown = "" | "YES" | "NO" | "UNKNOWN";
import { todayLocal } from "./validation";

export interface SurveyMetadata {
  participantCode: string;
  surveyMethod: "ONLINE" | "PHONE" | "OTHER";
  surveyMethodOther: string;
  surveyDate: string;
  contactPhone: string;
  contactTime: string;
}

export interface Eligibility {
  voluntaryConsent: "" | "YES" | "NO";
  birthDate: string;
  over55: "" | "YES" | "NO";
  femaleAtBirth: "" | "YES" | "NO" | "DECLINE";
  exclusionDisease: "" | "NONE" | "EXCLUSION" | "UNKNOWN";
}

export interface BasicInfo {
  region: string;
  regionOther: string;
  education: string;
  educationOther: string;
  householdIncome: string;
  householdIncomeOther: string;
  employed: "" | "YES" | "NO" | "OTHER";
  employmentOther: string;
  heightCm: string;
  weightKg: string;
  menopauseAge: string;
  menopauseAgeUnknown: boolean;
  hormoneTreatment: string;
  hormoneDurationYears: string;
  hormoneDurationMonths: string;
  hormoneDurationUnknown: boolean;
}

export interface ProductDetails {
  currentStatus: string;
  startRecall: string;
  startYearMonth: string;
  startYear: string;
  startReasons: string[];
  startReasonOther: string;
  priorityReasons: string[];
  interruptionStatus: string;
  interruptionDetails: string;
  evidencePeriodType: string;
  evidenceStartYearMonth: string;
  evidenceEndYearMonth: string;
}

export interface ProductHistory {
  clientId: string;
  productName: string;
  ingredients: string[];
  totalIntakeMonths: string;
  averageFrequency: string;
  currentlyTaking: YesNoUnknown;
}

export interface FractureHistory {
  clientId: string;
  occurredYearMonth: string;
  fractureSite: string;
  fallRelated: YesNoUnknown;
  primaryCause: string;
  recordAvailability: string;
  treatment: string;
  timingRelativeToProduct: string;
}

export interface FoodAnswer {
  foodCode: string;
  foodName: string;
  frequency: string;
  amount: string;
}

export const SOURCE_FOOD_ITEMS = [
  ["RICE", "쌀밥"],
  ["MIXED_RICE", "잡곡밥(현미밥, 보리밥 등)"],
  ["BEAN_RICE", "콩밥"]
] as const;

export interface SurveyForm {
  surveyVersion: string;
  privacyConsent: boolean;
  contactConsent: boolean;
  metadata: SurveyMetadata;
  eligibility: Eligibility;
  basicInfo: BasicInfo;
  productExperience: string;
  productDetails: ProductDetails;
  products: ProductHistory[];
  fractureExperience: YesNoUnknown;
  fractureTotalCount: string;
  fractureCountUnknown: boolean;
  fractures: FractureHistory[];
  mealsPerDay: string;
  foodAnswers: FoodAnswer[];
}

export interface SubmissionResponse {
  submissionId: string;
  submissionNumber: string;
  status: "DRAFT" | "SUBMITTED";
  updatedAt: string;
  submittedAt: string | null;
}

export const newProduct = (): ProductHistory => ({
  clientId: crypto.randomUUID(),
  productName: "",
  ingredients: [],
  totalIntakeMonths: "",
  averageFrequency: "",
  currentlyTaking: ""
});

export const newFracture = (): FractureHistory => ({
  clientId: crypto.randomUUID(),
  occurredYearMonth: "",
  fractureSite: "",
  fallRelated: "",
  primaryCause: "",
  recordAvailability: "",
  treatment: "",
  timingRelativeToProduct: ""
});

export const initialSurveyForm = (): SurveyForm => ({
  surveyVersion: "2.4",
  privacyConsent: false,
  contactConsent: false,
  metadata: {
    participantCode: "",
    surveyMethod: "ONLINE",
    surveyMethodOther: "",
    surveyDate: todayLocal(),
    contactPhone: "",
    contactTime: ""
  },
  eligibility: {
    voluntaryConsent: "",
    birthDate: "",
    over55: "",
    femaleAtBirth: "",
    exclusionDisease: ""
  },
  basicInfo: {
    region: "",
    regionOther: "",
    education: "",
    educationOther: "",
    householdIncome: "",
    householdIncomeOther: "",
    employed: "",
    employmentOther: "",
    heightCm: "",
    weightKg: "",
    menopauseAge: "",
    menopauseAgeUnknown: false,
    hormoneTreatment: "",
    hormoneDurationYears: "",
    hormoneDurationMonths: "",
    hormoneDurationUnknown: false
  },
  productExperience: "",
  productDetails: {
    currentStatus: "",
    startRecall: "",
    startYearMonth: "",
    startYear: "",
    startReasons: [],
    startReasonOther: "",
    priorityReasons: [],
    interruptionStatus: "",
    interruptionDetails: "",
    evidencePeriodType: "",
    evidenceStartYearMonth: "",
    evidenceEndYearMonth: ""
  },
  products: [newProduct()],
  fractureExperience: "",
  fractureTotalCount: "",
  fractureCountUnknown: false,
  fractures: [newFracture()],
  mealsPerDay: "",
  foodAnswers: []
});
