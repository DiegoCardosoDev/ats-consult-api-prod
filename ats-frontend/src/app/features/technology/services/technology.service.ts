import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { TechnologyModel } from '../models/technology.model';
import { environment } from '../../../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class TechnologyService {
  private http = inject(HttpClient);
  private apiUrl = `${environment.apiBaseUrl}/api/v1/technologies`;

  findAll(): Observable<TechnologyModel[]> {
    return this.http.get<TechnologyModel[]>(this.apiUrl);
  }
}
