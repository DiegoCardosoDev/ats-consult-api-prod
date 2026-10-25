import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { CandidateModel } from '../../candidate/models/candidate.model';
import { FormAnswerPayload } from '../../application-form/models/application-form.model';
import {
  ApplicationCommentModel,
  ApplyCandidateResponse,
  CandidateApplicationModel,
  CandidateApplicationStatus,
  CreateSelectionProcessPayload,
  SelectionProcessDetailModel,
  SelectionProcessSummaryModel
} from '../models/selection-process.model';
import { PageResponse } from '../../../core/models/page-response.model';
import { environment } from '../../../../environments/environment';
import { PRIVACY_POLICY_VERSION } from '../../../core/constants/privacy-policy.constants';

@Injectable({ providedIn: 'root' })
export class SelectionProcessService {
  private http = inject(HttpClient);
  private apiUrl = `${environment.apiBaseUrl}/api/v1/selection-processes`;

  findAll(page = 0, size = 10): Observable<PageResponse<SelectionProcessSummaryModel>> {
    const params = new HttpParams().set('page', page).set('size', size);
    return this.http.get<PageResponse<SelectionProcessSummaryModel>>(this.apiUrl, { params });
  }

  findById(id: number): Observable<SelectionProcessDetailModel> {
    return this.http.get<SelectionProcessDetailModel>(`${this.apiUrl}/${id}`);
  }

  create(payload: CreateSelectionProcessPayload): Observable<SelectionProcessDetailModel> {
    return this.http.post<SelectionProcessDetailModel>(this.apiUrl, payload);
  }

  applyToProcess(processId: number, candidate: CandidateModel, resume: File,
                 answers: FormAnswerPayload[] = []): Observable<ApplyCandidateResponse> {
    return this.http.post<ApplyCandidateResponse>(
      `${this.apiUrl}/${processId}/candidates`,
      this.buildApplicationData(candidate, resume, answers)
    );
  }

  applyToJob(jobId: number, candidate: CandidateModel, resume: File,
             answers: FormAnswerPayload[] = []): Observable<ApplyCandidateResponse> {
    return this.http.post<ApplyCandidateResponse>(
      `${environment.apiBaseUrl}/api/v1/job-positions/${jobId}/applications`,
      this.buildApplicationData(candidate, resume, answers)
    );
  }

  updateApplicationStatus(applicationId: number,
                          status: CandidateApplicationStatus): Observable<CandidateApplicationModel> {
    return this.http.patch<CandidateApplicationModel>(
      `${environment.apiBaseUrl}/api/v1/applications/${applicationId}/status`,
      { status }
    );
  }

  addApplicationComment(applicationId: number, message: string): Observable<ApplicationCommentModel> {
    return this.http.post<ApplicationCommentModel>(
      `${environment.apiBaseUrl}/api/v1/applications/${applicationId}/comments`,
      { message }
    );
  }

  findApplicationComments(applicationId: number): Observable<ApplicationCommentModel[]> {
    return this.http.get<ApplicationCommentModel[]>(
      `${environment.apiBaseUrl}/api/v1/applications/${applicationId}/comments`
    );
  }

  downloadResume(applicationId: number): Observable<Blob> {
    return this.http.get(`${environment.apiBaseUrl}/api/v1/applications/${applicationId}/resume`,
      { responseType: 'blob' });
  }

  private buildApplicationData(candidate: CandidateModel, resume: File,
                               answers: FormAnswerPayload[] = []): FormData {
    const data = new FormData();
    data.append('candidate', new Blob([JSON.stringify(candidate)], { type: 'application/json' }));
    data.append('resume', resume, resume.name);
    if (answers.length > 0) {
      data.append('answers',
        new Blob([JSON.stringify({ answers })], { type: 'application/json' }));
    }
    data.append('consent', new Blob([JSON.stringify({
      accepted: true,
      version: PRIVACY_POLICY_VERSION
    })], { type: 'application/json' }));
    return data;
  }
}
