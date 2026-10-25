import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ApplicationFormModel } from '../models/application-form.model';
import { environment } from '../../../../environments/environment';

@Injectable({ providedIn: 'root' })
export class ApplicationFormService {
  private http = inject(HttpClient);

  private url(jobId: number): string {
    return `${environment.apiBaseUrl}/api/v1/job-positions/${jobId}/application-form`;
  }

  findByJob(jobId: number): Observable<ApplicationFormModel | null> {
    return this.http.get<ApplicationFormModel | null>(this.url(jobId));
  }

  save(jobId: number, form: ApplicationFormModel): Observable<ApplicationFormModel> {
    return this.http.put<ApplicationFormModel>(this.url(jobId), form);
  }

  generate(jobId: number): Observable<ApplicationFormModel> {
    return this.http.post<ApplicationFormModel>(`${this.url(jobId)}/generate`, {});
  }

  delete(jobId: number): Observable<void> {
    return this.http.delete<void>(this.url(jobId));
  }
}
