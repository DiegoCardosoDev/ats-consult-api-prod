import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { CandidateService } from '../../services/candidate.service';
import { CandidateModel } from '../../models/candidate.model';
import { AuthService } from '../../../../services/auth.service';
import { extractApiErrorMessage } from '../../../../core/utils/api-error.util';
import { TechnologyService } from '../../../technology/services/technology.service';
import { TechnologyModel } from '../../../technology/models/technology.model';
import {
  EDUCATION_LEVELS,
  ENGLISH_LEVELS,
  educationLevelLabel,
  englishLevelLabel,
  groupTechnologies,
  TechnologyGroup
} from '../../../technology/technology-category.util';

@Component({
  selector: 'app-candidate-list',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  templateUrl: './candidate-list.component.html'
})
export class CandidateListComponent implements OnInit {
  private candidateService = inject(CandidateService);
  private authService = inject(AuthService);
  private technologyService = inject(TechnologyService);
  private fb = inject(FormBuilder);

  candidates: CandidateModel[] = [];
  technologies: TechnologyModel[] = [];
  technologyGroups: TechnologyGroup[] = [];
  selectedTechIds: number[] = [];
  readonly educationLevels = EDUCATION_LEVELS;
  readonly englishLevels = ENGLISH_LEVELS;
  isLoading = true;
  isSubmitting = false;
  isLoadingProfile = false;
  errorMessage = '';
  successMessage = '';
  isModalOpen = false;
  isEditMode = false;
  editingId?: number;

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
    educationLevel: [null as string | null],
    englishLevel: [null as string | null],
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
    this.loadTechnologies();
  }

  loadTechnologies(): void {
    this.technologyService.findAll().subscribe({
      next: (technologies) => {
        this.technologies = technologies;
        this.technologyGroups = groupTechnologies(technologies);
      },
      error: (error: unknown) => {
        this.errorMessage = extractApiErrorMessage(error, 'Não foi possível carregar as tecnologias.');
      }
    });
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
    this.selectedTechIds = [];
    this.isEditMode = false;
    this.editingId = undefined;
    this.errorMessage = '';
    this.successMessage = '';
    this.isModalOpen = true;
  }

  openEditModal(candidate: CandidateModel): void {
    if (!candidate.id) {
      return;
    }
    this.isEditMode = true;
    this.editingId = candidate.id;
    this.errorMessage = '';
    this.successMessage = '';
    this.isModalOpen = true;
    this.isLoadingProfile = true;

    this.candidateService.findById(candidate.id).subscribe({
      next: (profile) => {
        this.isLoadingProfile = false;
        this.candidateForm.patchValue({
          name: profile.name,
          age: profile.age,
          role: profile.role,
          linkedinUrl: profile.linkedinUrl ?? '',
          email: profile.email,
          phone: profile.phone,
          educationLevel: profile.educationLevel ?? null,
          englishLevel: profile.englishLevel ?? null,
          address: profile.address ?? {}
        });
        this.selectedTechIds = profile.technologies?.map(technology => technology.id) ?? [];
      },
      error: (error: unknown) => {
        this.isLoadingProfile = false;
        this.errorMessage = extractApiErrorMessage(error, 'Não foi possível carregar o perfil do candidato.');
      }
    });
  }

  closeModal(): void {
    this.isModalOpen = false;
    this.isSubmitting = false;
    this.isLoadingProfile = false;
  }

  onTechToggle(technologyId: number, event: Event): void {
    const checked = (event.target as HTMLInputElement).checked;
    if (checked && !this.selectedTechIds.includes(technologyId)) {
      this.selectedTechIds = [...this.selectedTechIds, technologyId];
    } else if (!checked) {
      this.selectedTechIds = this.selectedTechIds.filter(id => id !== technologyId);
    }
  }

  isTechSelected(technologyId: number): boolean {
    return this.selectedTechIds.includes(technologyId);
  }

  get candidateTechnologies(): TechnologyModel[] {
    return this.technologies.filter(technology => this.selectedTechIds.includes(technology.id));
  }

  educationLabel(level?: string): string {
    return educationLevelLabel(level);
  }

  englishLabel(level?: string): string {
    return englishLevelLabel(level);
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
      educationLevel: raw.educationLevel || undefined,
      englishLevel: raw.englishLevel || undefined,
      technologyIds: this.selectedTechIds,
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

    const request$ = this.isEditMode && this.editingId
      ? this.candidateService.update(this.editingId, candidate)
      : this.candidateService.create(candidate);

    request$.subscribe({
      next: (saved) => {
        this.isSubmitting = false;
        this.isModalOpen = false;
        this.successMessage = this.isEditMode
          ? `Perfil de ${saved.name} atualizado com sucesso.`
          : `Candidato ${saved.name} criado com sucesso.`;
        this.loadCandidates(this.isEditMode ? this.page : 0);
      },
      error: (error: unknown) => {
        this.isSubmitting = false;
        this.errorMessage = extractApiErrorMessage(error, this.isEditMode
          ? 'Não foi possível atualizar o candidato.'
          : 'Não foi possível criar o candidato.');
      }
    });
  }

  logout(): void {
    this.authService.logout();
  }
}
