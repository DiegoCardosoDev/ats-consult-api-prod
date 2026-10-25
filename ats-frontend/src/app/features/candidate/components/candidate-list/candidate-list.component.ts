import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { CandidateService } from '../../services/candidate.service';
import { CandidateModel } from '../../models/candidate.model';
import { AuthService } from '../../../../services/auth.service';
import { extractApiErrorMessage } from '../../../../core/utils/api-error.util';

@Component({
  selector: 'app-candidate-list',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  templateUrl: './candidate-list.component.html'
})
export class CandidateListComponent implements OnInit {
  private candidateService = inject(CandidateService);
  private authService = inject(AuthService);
  private fb = inject(FormBuilder);

  candidates: CandidateModel[] = [];
  isLoading = true;
  isSubmitting = false;
  errorMessage = '';
  successMessage = '';
  isModalOpen = false;

  page = 0;
  size = 10;
  totalElements = 0;
  totalPages = 0;
  first = true;
  last = true;

  filterForm = this.fb.group({
    name: [''],
    role: ['']
  });

  candidateForm = this.fb.group({
    name: ['', [Validators.required, Validators.maxLength(150)]],
    age: [null as number | null, [Validators.required, Validators.min(16), Validators.max(120)]],
    role: ['', [Validators.required, Validators.maxLength(150)]],
    linkedinUrl: ['', [Validators.pattern(/^$|^https?:\/\/.+/), Validators.maxLength(500)]],
    email: ['', [Validators.required, Validators.email, Validators.maxLength(255)]],
    phone: ['', [Validators.required, Validators.minLength(8), Validators.maxLength(30)]],
    address: this.fb.group({
      street: [''],
      number: [''],
      complement: [''],
      neighborhood: [''],
      city: [''],
      state: [''],
      zipCode: [''],
      country: ['']
    })
  });

  ngOnInit(): void {
    this.loadCandidates(0);
  }

  loadCandidates(page = this.page): void {
    this.isLoading = true;
    this.errorMessage = '';

    this.candidateService.search(
      this.filterForm.value.name ?? '',
      this.filterForm.value.role ?? '',
      page,
      this.size
    ).subscribe({
      next: (data) => {
        this.candidates = data.content;
        this.page = data.page;
        this.totalElements = data.totalElements;
        this.totalPages = data.totalPages;
        this.first = data.first;
        this.last = data.last;
        this.isLoading = false;
      },
      error: (error: unknown) => {
        this.isLoading = false;
        this.errorMessage = extractApiErrorMessage(error, 'Não foi possível pesquisar candidatos.');
      }
    });
  }

  search(): void {
    this.loadCandidates(0);
  }

  clearFilters(): void {
    this.filterForm.reset({ name: '', role: '' });
    this.loadCandidates(0);
  }

  nextPage(): void {
    if (!this.last && !this.isLoading) {
      this.loadCandidates(this.page + 1);
    }
  }

  previousPage(): void {
    if (!this.first && !this.isLoading) {
      this.loadCandidates(this.page - 1);
    }
  }

  openCreateModal(): void {
    this.candidateForm.reset();
    this.errorMessage = '';
    this.successMessage = '';
    this.isModalOpen = true;
  }

  closeModal(): void {
    this.isModalOpen = false;
    this.isSubmitting = false;
  }

  submit(): void {
    if (this.candidateForm.invalid) {
      this.candidateForm.markAllAsTouched();
      return;
    }

    const raw = this.candidateForm.getRawValue();
    const candidate: CandidateModel = {
      name: raw.name ?? '',
      age: raw.age ?? 0,
      role: raw.role ?? '',
      linkedinUrl: raw.linkedinUrl || undefined,
      email: raw.email ?? '',
      phone: raw.phone ?? '',
      address: {
        street: raw.address.street || undefined,
        number: raw.address.number || undefined,
        complement: raw.address.complement || undefined,
        neighborhood: raw.address.neighborhood || undefined,
        city: raw.address.city || undefined,
        state: raw.address.state || undefined,
        zipCode: raw.address.zipCode || undefined,
        country: raw.address.country || undefined
      }
    };

    this.isSubmitting = true;
    this.errorMessage = '';

    this.candidateService.create(candidate).subscribe({
      next: (created) => {
        this.isSubmitting = false;
        this.isModalOpen = false;
        this.successMessage = `Candidato ${created.name} criado com sucesso.`;
        this.loadCandidates(0);
      },
      error: (error: unknown) => {
        this.isSubmitting = false;
        this.errorMessage = extractApiErrorMessage(error, 'Não foi possível criar o candidato.');
      }
    });
  }

  logout(): void {
    this.authService.logout();
  }
}
