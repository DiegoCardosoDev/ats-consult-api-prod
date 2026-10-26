import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { UserService } from '../../services/user.service';
import { UserModel, UserRole } from '../../models/user.model';
import { AuthService } from '../../../../services/auth.service';
import { extractApiErrorMessage } from '../../../../core/utils/api-error.util';

const STRONG_PASSWORD = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[^A-Za-z0-9]).{8,72}$/;

@Component({
  selector: 'app-user-management',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  templateUrl: './user-management.component.html'
})
export class UserManagementComponent implements OnInit {
  private userService = inject(UserService);
  private authService = inject(AuthService);
  private fb = inject(FormBuilder);

  users: UserModel[] = [];
  isLoading = true;
  isSubmitting = false;
  isModalOpen = false;
  errorMessage = '';
  successMessage = '';

  readonly roles: { value: UserRole; label: string }[] = [
    { value: 'USER', label: 'Candidato' },
    { value: 'TECH_RECRUITER', label: 'Tech Recruiter' },
    { value: 'ADMIN', label: 'Administrador' }
  ];

  userForm = this.fb.group({
    name: ['', [Validators.required, Validators.minLength(3), Validators.maxLength(150)]],
    email: ['', [Validators.required, Validators.email, Validators.maxLength(255)]],
    password: ['', [Validators.required, Validators.pattern(STRONG_PASSWORD)]],
    role: ['USER' as UserRole, [Validators.required]]
  });

  ngOnInit(): void {
    this.loadUsers();
  }

  loadUsers(): void {
    this.isLoading = true;
    this.errorMessage = '';

    this.userService.findAll().subscribe({
      next: (users) => {
        this.users = users;
        this.isLoading = false;
      },
      error: (error: unknown) => {
        this.isLoading = false;
        this.errorMessage = extractApiErrorMessage(error, 'Não foi possível carregar os utilizadores.');
      }
    });
  }

  openCreateModal(): void {
    this.userForm.reset({ name: '', email: '', password: '', role: 'USER' });
    this.errorMessage = '';
    this.successMessage = '';
    this.isModalOpen = true;
  }

  closeModal(): void {
    this.isModalOpen = false;
    this.isSubmitting = false;
  }

  submit(): void {
    if (this.userForm.invalid) {
      this.userForm.markAllAsTouched();
      return;
    }

    this.isSubmitting = true;
    this.errorMessage = '';

    this.userService.create(this.userForm.getRawValue() as {
      name: string;
      email: string;
      password: string;
      role: UserRole;
    }).subscribe({
      next: (created) => {
        this.isSubmitting = false;
        this.isModalOpen = false;
        this.successMessage = `Utilizador ${created.name ?? created.email} criado com sucesso.`;
        this.loadUsers();
      },
      error: (error: unknown) => {
        this.isSubmitting = false;
        this.errorMessage = extractApiErrorMessage(error, 'Não foi possível criar o utilizador.');
      }
    });
  }

  roleLabel(role: UserRole): string {
    return this.roles.find(item => item.value === role)?.label ?? role;
  }

  roleBadgeClass(role: UserRole): string {
    switch (role) {
      case 'ADMIN':
        return 'bg-danger-subtle text-danger-emphasis';
      case 'TECH_RECRUITER':
        return 'bg-primary-subtle text-primary-emphasis';
      default:
        return 'bg-success-subtle text-success-emphasis';
    }
  }

  logout(): void {
    this.authService.logout();
  }
}
