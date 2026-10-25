import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ClientModel, CreateClientPayload } from '../models/client.model';
import { environment } from '../../../../environments/environment';

@Injectable({ providedIn: 'root' })
export class ClientService {
  private http = inject(HttpClient);
  private apiUrl = `${environment.apiBaseUrl}/api/v1/clients`;

  findAll(): Observable<ClientModel[]> {
    return this.http.get<ClientModel[]>(this.apiUrl);
  }

  create(payload: CreateClientPayload): Observable<ClientModel> {
    return this.http.post<ClientModel>(this.apiUrl, payload);
  }

  findById(id: number): Observable<ClientModel> {
    return this.http.get<ClientModel>(`${this.apiUrl}/${id}`);
  }
}
