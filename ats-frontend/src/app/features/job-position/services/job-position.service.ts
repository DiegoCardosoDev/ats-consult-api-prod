import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { JobPosition } from '../models/job-position.model';
import { PageResponse } from '../../../core/models/page-response.model';
import { environment } from '../../../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class JobPositionService {
  private http = inject(HttpClient);
  private apiUrl = `${environment.apiBaseUrl}/api/v1/job-positions`;

  findAll(technologyId?: number, page = 0, size = 10): Observable<PageResponse<JobPosition>> {
    let params = new HttpParams()
      .set('page', page)
      .set('size', size);

    if (technologyId) {
      params = params.set('technologyId', technologyId);
    }

    return this.http.get<PageResponse<JobPosition>>(this.apiUrl, { params });
  }

  findById(id: number): Observable<JobPosition> {
    return this.http.get<JobPosition>(`${this.apiUrl}/${id}`);
  }

  create(jobPosition: unknown): Observable<JobPosition> {
    return this.http.post<JobPosition>(this.apiUrl, jobPosition);
  }

  update(id: number, job: unknown): Observable<JobPosition> {
    return this.http.put<JobPosition>(`${this.apiUrl}/${id}`, job);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }
}
