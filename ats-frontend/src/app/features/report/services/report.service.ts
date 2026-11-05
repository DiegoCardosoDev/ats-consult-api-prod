import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';
import { ProcessReportFilters, ProcessReportPage } from '../models/process-report.model';
import { ProcessDashboard } from '../models/process-dashboard.model';

@Injectable({ providedIn: 'root' })
export class ReportService {
  private http = inject(HttpClient);
  private apiUrl = `${environment.apiBaseUrl}/api/v1/reports/selection-processes`;

  findProcessReport(filters: ProcessReportFilters = {}): Observable<ProcessReportPage> {
    let params = new HttpParams()
      .set('page', filters.page ?? 0)
      .set('size', filters.size ?? 20);

    if (filters.clientId) {
      params = params.set('clientId', filters.clientId);
    }
    if (filters.jobPositionId) {
      params = params.set('jobPositionId', filters.jobPositionId);
    }
    if (filters.status) {
      params = params.set('status', filters.status);
    }

    return this.http.get<ProcessReportPage>(this.apiUrl, { params });
  }

  findProcessDashboard(processId: number): Observable<ProcessDashboard> {
    return this.http.get<ProcessDashboard>(`${this.apiUrl}/${processId}`);
  }
}
