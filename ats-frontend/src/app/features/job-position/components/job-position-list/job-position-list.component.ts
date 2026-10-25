import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';

import { JobPosition } from '../../models/job-position.model';
import { JobPositionService } from '../../services/job-position.service';
import { AuthService } from '../../../../services/auth.service';
import { extractApiErrorMessage } from '../../../../core/utils/api-error.util';

@Component({
  selector: 'app-job-position-list',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './job-position-list.component.html',
  styleUrl: './job-position-list.component.scss'
})
export class JobPositionListComponent implements OnInit {
  private jobPositionService = inject(JobPositionService);
  private authService = inject(AuthService);

  jobs: JobPosition[] = [];
  isLoading = true;
  errorMessage = '';

  page = 0;
  size = 10;
  totalElements = 0;
  totalPages = 0;
  first = true;
  last = true;

  ngOnInit(): void {
    this.loadJobs(0);
  }

  loadJobs(page = this.page): void {
    this.isLoading = true;
    this.errorMessage = '';

    this.jobPositionService.findAll(undefined, page, this.size).subscribe({
      next: (data) => {
        this.jobs = data.content;
        this.page = data.page;
        this.totalElements = data.totalElements;
        this.totalPages = data.totalPages;
        this.first = data.first;
        this.last = data.last;
        this.isLoading = false;
      },
      error: (err: unknown) => {
        console.error('Erro ao carregar vagas:', err);
        this.errorMessage = extractApiErrorMessage(err, 'Não foi possível carregar as vagas.');
        this.isLoading = false;
      }
    });
  }

  nextPage(): void {
    if (!this.last && !this.isLoading) {
      this.loadJobs(this.page + 1);
    }
  }

  previousPage(): void {
    if (!this.first && !this.isLoading) {
      this.loadJobs(this.page - 1);
    }
  }

  logout(): void {
    this.authService.logout();
  }

  getWorkModelLabel(model?: string): string {
    switch (model) {
      case 'HYBRID': return 'Híbrido';
      case 'REMOTE': return 'Remoto';
      case 'ON_SITE': return 'Presencial';
      default: return model || 'Presencial';
    }
  }

  getContractTypeLabel(type?: string): string {
    const labels: { [key: string]: string } = {
      'CLT': 'CLT',
      'PJ': 'PJ',
      'CLT_OR_PJ': 'CLT ou PJ',
      'FREELANCE': 'Freelance',
      'INTERNSHIP': 'Estágio',
      'TEMPORARY': 'Temporário'
    };
    return type ? (labels[type] || type) : '';
  }

  getStatusLabel(status?: string): string {
    const labels: { [key: string]: string } = {
      'OPEN': 'Aberta',
      'CLOSED': 'Fechada',
      'PAUSED': 'Pausada'
    };
    return status ? (labels[status] || status) : '';
  }

  getEducationLevelLabel(level?: string): string {
    const labels: { [key: string]: string } = {
      'HIGH_SCHOOL': 'Ensino Médio',
      'ASSOCIATE': 'Tecnólogo',
      'BACHELORS': 'Superior Completo',
      'SPECIALIZATION': 'Especialização / Pós',
      'MASTERS': 'Mestrado',
      'DOCTORATE': 'Doutorado'
    };
    return level ? (labels[level] || level) : '';
  }

  getEnglishLevelLabel(level?: string): string {
    const labels: { [key: string]: string } = {
      'NONE': 'Inglês não exigido',
      'BASIC': 'Inglês Básico',
      'INTERMEDIATE': 'Inglês Intermediário',
      'ADVANCED': 'Inglês Avançado',
      'FLUENT': 'Inglês Fluente'
    };
    return level ? (labels[level] || level) : '';
  }

  onDelete(id: number): void {
    if (confirm('Tem certeza de que deseja apagar esta vaga?')) {
      this.jobPositionService.delete(id).subscribe({
        next: () => {
          if (this.jobs.length === 1 && this.page > 0) {
            this.loadJobs(this.page - 1);
          } else {
            this.loadJobs(this.page);
          }
        },
        error: (err) => {
          console.error('Erro ao apagar vaga', err);
          alert(extractApiErrorMessage(err, 'Não foi possível apagar a vaga.'));
        }
      });
    }
  }
}
