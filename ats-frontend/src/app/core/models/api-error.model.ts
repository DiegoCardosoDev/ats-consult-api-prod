export interface ApiErrorField {
  field: string;
  message: string;
}

export interface ApiError {
  timestamp: string;
  status: number;
  error: string;
  message: string;
  path: string;
  errors?: ApiErrorField[];
  traceId?: string;
}
