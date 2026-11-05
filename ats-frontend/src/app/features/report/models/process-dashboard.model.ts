import {
  CandidateApplicationStatus,
  SelectionProcessStatus
} from '../../selection-process/models/selection-process.model';

export interface ProcessDashboardTimelinePoint {
  date: string;
  total: number;
}

export interface ProcessDashboardCandidate {
  applicationId: number;
  candidateId: number | null;
  candidateName: string | null;
  status: CandidateApplicationStatus;
  adherence: number;
  appliedAt: string;
}

export interface ProcessDashboard {
  processId: number;
  client: {
    id: number;
    name: string;
    code: string;
  };
  jobPosition: {
    id: number;
    title: string;
  };
  status: SelectionProcessStatus;
  openedAt: string;
  daysOpen: number;
  totalApplications: number;
  activeApplications: number;
  inTriage: number;
  approvedByClient: number;
  hired: number;
  rejected: number;
  declined: number;
  standBy: number;
  averageAdherence: number;
  highAdherenceCount: number;
  conversionRate: number;
  statusCounts: Partial<Record<CandidateApplicationStatus, number>>;
  timeline: ProcessDashboardTimelinePoint[];
  topCandidates: ProcessDashboardCandidate[];
}
