import { CandidateModel } from '../../candidate/models/candidate.model';

export type SelectionProcessStatus = 'OPEN' | 'PAUSED' | 'CLOSED' | 'CANCELLED';

export type CandidateApplicationStatus =
  | 'TRIAGEM'
  | 'ENTRAR_EM_CONTATO'
  | 'ENTREVISTA_TECH_RECRUITER'
  | 'APROVADO_ENTREVISTA_CLIENTE'
  | 'APROVADO_CLIENTE'
  | 'REPROVADO'
  | 'DECLINOU'
  | 'STAND_BY';

export interface SelectionProcessClientSummary {
  id: number;
  name: string;
  code: string;
}

export interface SelectionProcessJobSummary {
  id: number;
  title: string;
}

export interface ResumeModel {
  filename: string;
  contentType: string;
  sizeBytes: number;
}

export interface FormAnswerModel {
  questionId: number;
  questionKey?: string;
  questionPrompt: string;
  answer: string;
  position?: number;
}

export interface ApplicationCommentModel {
  id: number;
  applicationId: number;
  authorId?: number | null;
  authorEmail: string;
  message: string;
  createdAt: string;
}

export interface PrivacyConsentModel {
  accepted: boolean;
  acceptedAt?: string;
  version?: string;
}

export interface AdherenceCriterionModel {
  key: string;
  label: string;
  percentage: number;
  weight: number;
  detail: string;
}

export interface AdherenceModel {
  percentage: number;
  criteria: AdherenceCriterionModel[];
}

export interface CandidateApplicationModel {
  id: number;
  candidate: CandidateModel;
  resume: ResumeModel;
  status: CandidateApplicationStatus;
  consent?: PrivacyConsentModel;
  adherence?: AdherenceModel;
  answers?: FormAnswerModel[];
  comments?: ApplicationCommentModel[];
  appliedAt: string;
}

export interface SelectionProcessSummaryModel {
  id: number;
  client: SelectionProcessClientSummary;
  jobPosition: SelectionProcessJobSummary;
  status: SelectionProcessStatus;
  openedAt: string;
}

export interface SelectionProcessDetailModel extends SelectionProcessSummaryModel {
  candidates: CandidateApplicationModel[];
}

export interface CreateSelectionProcessPayload {
  clientId: number;
  jobPositionId: number;
  status?: SelectionProcessStatus;
  openedAt?: string;
}

export interface ApplyCandidateResponse {
  selectionProcessId: number;
  application: CandidateApplicationModel;
}

export interface UpdateApplicationStatusPayload {
  status: CandidateApplicationStatus;
}

export interface ApplicationStatusChange {
  application: CandidateApplicationModel;
  previousStatus: CandidateApplicationStatus;
  status: CandidateApplicationStatus;
}
