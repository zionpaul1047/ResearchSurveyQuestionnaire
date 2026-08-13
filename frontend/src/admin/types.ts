export type AdminUser = {
  username: string;
  roles: string[];
};

export type MetricPoint = {
  label: string;
  value: number;
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
  };
  dailySubmissions: MetricPoint[];
  ageGroups: MetricPoint[];
  regions: MetricPoint[];
  productExperiences: MetricPoint[];
  fractureExperiences: MetricPoint[];
  foodDistributions: FoodMetricPoint[];
};
