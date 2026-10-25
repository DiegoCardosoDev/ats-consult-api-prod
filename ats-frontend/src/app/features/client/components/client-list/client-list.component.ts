import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AbstractControl, FormBuilder, ReactiveFormsModule, ValidationErrors, Validators } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { ClientService } from '../../services/client.service';
import { ClientModel } from '../../models/client.model';
import { AuthService } from '../../../../services/auth.service';
import { extractApiErrorMessage } from '../../../../core/utils/api-error.util';

function cnpjValidator(control: AbstractControl): ValidationErrors | null {
  const digits = String(control.value ?? '').replace(/\D/g, '');
  return digits.length === 14 ? null : { cnpj: true };
}

@Component({
  selector: 'app-client-list',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  templateUrl: './client-list.component.html'
})
export class ClientListComponent implements OnInit {
  private clientService = inject(ClientService);
  private authService = inject(AuthService);
  private fb = inject(FormBuilder);

  clients: ClientModel[] = [];
  isLoading = true;
  isSubmitting = false;
  errorMessage = '';
  successMessage = '';
  isModalOpen = false;

  clientForm = this.fb.group({
    name: ['', [Validators.required, Validators.maxLength(150)]],
    cnpj: ['', [Validators.required, cnpjValidator]]
  });

  ngOnInit(): void {
    this.loadClients();
  }

  loadClients(): void {
    this.isLoading = true;
    this.errorMessage = '';
    this.clientService.findAll().subscribe({
      next: (clients) => {
        this.clients = clients;
        this.isLoading = false;
      },
      error: (error: unknown) => {
        this.errorMessage = extractApiErrorMessage(error, 'Não foi possível carregar os clientes.');
        this.isLoading = false;
      }
    });
  }

  openCreateModal(): void {
    this.clientForm.reset();
    this.errorMessage = '';
    this.successMessage = '';
    this.isModalOpen = true;
  }

  closeModal(): void {
    this.isModalOpen = false;
    this.isSubmitting = false;
  }

  onCnpjInput(event: Event): void {
    const input = event.target as HTMLInputElement;
    const digits = input.value.replace(/\D/g, '').slice(0, 14);
    const formatted = this.formatCnpj(digits);
    input.value = formatted;
    this.clientForm.controls.cnpj.setValue(formatted);
  }

  submit(): void {
    if (this.clientForm.invalid) {
      this.clientForm.markAllAsTouched();
      return;
    }

    this.isSubmitting = true;
    this.errorMessage = '';

    this.clientService.create({
      name: this.clientForm.value.name ?? '',
      cnpj: this.clientForm.value.cnpj ?? ''
    }).subscribe({
      next: (client) => {
        this.isSubmitting = false;
        this.isModalOpen = false;
        this.successMessage = `Cliente ${client.name} criado com sucesso. Código: ${client.code}.`;
        this.loadClients();
      },
      error: (error: unknown) => {
        this.isSubmitting = false;
        this.errorMessage = extractApiErrorMessage(error, 'Não foi possível criar o cliente.');
      }
    });
  }

  private formatCnpj(digits: string): string {
    if (digits.length <= 2) {
      return digits;
    }
    if (digits.length <= 5) {
      return `${digits.slice(0, 2)}.${digits.slice(2)}`;
    }
    if (digits.length <= 8) {
      return `${digits.slice(0, 2)}.${digits.slice(2, 5)}.${digits.slice(5)}`;
    }
    if (digits.length <= 12) {
      return `${digits.slice(0, 2)}.${digits.slice(2, 5)}.${digits.slice(5, 8)}/${digits.slice(8)}`;
    }
    return `${digits.slice(0, 2)}.${digits.slice(2, 5)}.${digits.slice(5, 8)}/${digits.slice(8, 12)}-${digits.slice(12)}`;
  }

  logout(): void {
    this.authService.logout();
  }
}
