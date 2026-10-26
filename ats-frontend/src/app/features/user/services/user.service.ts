import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { CreateUserAdminRequest, UserModel } from '../models/user.model';
import { environment } from '../../../../environments/environment';

@Injectable({ providedIn: 'root' })
export class UserService {
  private http = inject(HttpClient);
  private apiUrl = `${environment.apiBaseUrl}/api/v1/admin/users`;

  findAll(): Observable<UserModel[]> {
    return this.http.get<UserModel[]>(this.apiUrl);
  }

  create(user: CreateUserAdminRequest): Observable<UserModel> {
    return this.http.post<UserModel>(this.apiUrl, user);
  }
}
