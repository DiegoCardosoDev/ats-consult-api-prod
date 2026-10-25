import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { CandidateModel } from '../models/candidate.model';
import { PageResponse } from '../../../core/models/page-response.model';
import { environment } from '../../../../environments/environment';

@Injectable({ providedIn: 'root' })
export class CandidateService {
  private http = inject(HttpClient);
  private apiUrl = `${environment.apiBaseUrl}/api/v1/candidates`;

  search(name?: string, role?: string, page = 0, size = 10): Observable<PageResponse<CandidateModel>> {
    let params = new HttpParams()
      .set('page', page)
      .set('size', size);

    if (name?.trim()) {
      params = params.set('name', name.trim());
    }
    if (role?.trim()) {
      params = params.set('role', role.trim());
    }

    return this.http.get<PageResponse<CandidateModel>>(this.apiUrl, { params });
  }

  create(candidate: CandidateModel): Observable<CandidateModel> {
    return this.http.post<CandidateModel>(this.apiUrl, candidate);
  }

  findById(id: number): Observable<CandidateModel> {
    return this.http.get<CandidateModel>(`${this.apiUrl}/${id}`);
  }
}
