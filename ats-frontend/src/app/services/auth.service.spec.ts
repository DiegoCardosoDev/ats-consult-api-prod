import { Component } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideRouter } from '@angular/router';
import { AuthService, AuthUser } from './auth.service';

@Component({ standalone: true, template: '' })
class StubComponent {}

describe('AuthService', () => {
  let service: AuthService;
  let http: HttpTestingController;

  const user: AuthUser = {
    id: 1,
    email: 'admin@example.com',
    role: 'ADMIN'
  };

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        provideRouter([
          { path: 'login', component: StubComponent },
          { path: 'home', component: StubComponent }
        ])
      ]
    });
    service = TestBed.inject(AuthService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('autentica e valida a sessão no backend', () => {
    let authenticatedUser: AuthUser | undefined;

    service.login({ email: user.email, password: 'secret' }).subscribe(result => {
      authenticatedUser = result;
    });

    http.expectOne(request => request.url.endsWith('/auth/csrf') && request.method === 'GET')
      .flush(null);
    http.expectOne(request => request.url.endsWith('/auth/login') && request.method === 'POST')
      .flush({ message: 'Login efetuado com sucesso' });
    http.expectOne(request => request.url.endsWith('/auth/me') && request.method === 'GET')
      .flush(user);

    expect(authenticatedUser).toEqual(user);
    expect(service.currentUser()).toEqual(user);
    expect(service.isAuthenticated()).toBeTrue();
  });

  it('não considera a sessão válida quando o backend responde 401', () => {
    let authenticated = true;

    service.ensureSession().subscribe(result => {
      authenticated = result;
    });

    http.expectOne(request => request.url.endsWith('/auth/me'))
      .flush({ message: 'Autenticação necessária' }, { status: 401, statusText: 'Unauthorized' });

    expect(authenticated).toBeFalse();
    expect(service.currentUser()).toBeNull();
  });

  it('limpa o estado local mesmo quando o logout falha', () => {
    service.clearSession();
    service.logout();

    http.expectOne(request => request.url.endsWith('/auth/logout'))
      .flush({ message: 'Erro' }, { status: 500, statusText: 'Server Error' });

    expect(service.currentUser()).toBeNull();
    expect(service.isAuthenticated()).toBeFalse();
  });
});
