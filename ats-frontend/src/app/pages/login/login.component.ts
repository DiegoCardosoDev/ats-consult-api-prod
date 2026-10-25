import { Component } from '@angular/core';
import { CommonModule } from '@angular/common'; // 👈 Necessário para diretivas básicas
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms'; // 👈 Importar ReactiveFormsModule
import { Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { extractApiErrorMessage } from '../../core/utils/api-error.util';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule], // 👈 Registar os módulos aqui
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.scss']
})
export class LoginComponent {
  loginForm: FormGroup;
  errorMessage: string = '';

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private router: Router
  ) {
    this.loginForm = this.fb.group({
      email: ['', [Validators.required, Validators.email]],
      password: ['', [Validators.required, Validators.minLength(6)]]
    });
  }

  onSubmit(): void {
    if (this.loginForm.valid) {
      this.authService.login(this.loginForm.value).subscribe({
        next: () => {
          this.router.navigate(['/home']);
        },
        error: (err) => {
          this.errorMessage = extractApiErrorMessage(err, 'Credenciais inválidas. Verifique o seu e-mail e palavra-passe.');
          console.error(err);
        }
      });
    }
  }
}