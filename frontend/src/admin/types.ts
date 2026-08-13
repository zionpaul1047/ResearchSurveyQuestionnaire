export type AdminUser = {
  username: string;
  roles: string[];
};

export type MetricPoint = {
  label: string;
  value: number | null;
  suppressed: boolean;
};

export type FoodMetricPoint = MetricPoint & {
  foodCode: string;
  foodName: string;
  frequency: string;
};

export type AnalyticsSummary = {
  overview: {
    totalResponses: number;
    submittedResponses: number;
    draftResponses: number;
    submissionRate: number;
    averageCompletionSeconds: number | null;
    averageCompletionSuppressed: boolean;
  };
  minimumGroupSize: number;
  dailySubmissions: MetricPoint[];
  ageGroups: MetricPoint[];
  regions: MetricPoint[];
  productExperiences: MetricPoint[];
  fractureExperiences: MetricPoint[];
  foodDistributions: FoodMetricPoint[];
};
