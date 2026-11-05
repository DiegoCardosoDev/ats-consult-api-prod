import { PageResponse } from '../../../core/models/page-response.model';
import {
  CandidateApplicationStatus,
  SelectionProcessStatus
} from '../../selection-process/models/selection-process.model';

export interface ProcessReportSummary {
  totalProcesses: number;
  totalApplications: number;
  inTriage: number;
  rejected: number;
  approvedByClient: number;
  hired: number;
  declined: number;
  standBy: number;
  statusCounts: Partial<Record<CandidateApplicationStatus, number>>;
}

export interface ProcessReportItem {
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
  processStatus: SelectionProcessStatus;
  openedAt: string;
  totalApplications: number;
  inTriage: number;
  rejected: number;
  approvedByClient: number;
  hired: number;
  declined: number;
  standBy: number;
  statusCounts: Partial<Record<CandidateApplicationStatus, number>>;
}

export interface ProcessReportPage {
  summary: ProcessReportSummary;
  processes: PageResponse<ProcessReportItem>;
}

export interface ProcessReportFilters {
  clientId?: number;
  jobPositionId?: number;
  status?: SelectionProcessStatus;
  page?: number;
  size?: number;
}
