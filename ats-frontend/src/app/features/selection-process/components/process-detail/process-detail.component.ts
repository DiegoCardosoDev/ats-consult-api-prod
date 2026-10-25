import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { SelectionProcessService } from '../../services/selection-process.service';
import {
  ApplicationStatusChange,
  ApplyCandidateResponse,
  CandidateApplicationModel,
  SelectionProcessDetailModel,
  SelectionProcessStatus
} from '../../models/selection-process.model';
import { CandidateApplicationModalComponent } from '../candidate-application-modal/candidate-application-modal.component';
import { KanbanBoardComponent } from '../kanban-board/kanban-board.component';
import { ApplicationDetailModalComponent } from '../application-detail-modal/application-detail-modal.component';
import { ApplicationFormModel } from '../../../application-form/models/application-form.model';
import { ApplicationFormService } from '../../../application-form/services/application-form.service';
import { AuthService } from '../../../../services/auth.service';
import { extractApiErrorMessage } from '../../../../core/utils/api-error.util';

@Component({
  selector: 'app-process-detail',
  standalone: true,
  imports: [CommonModule, RouterLink, CandidateApplicationModalComponent, KanbanBoardComponent,
    ApplicationDetailModalComponent],
  templateUrl: './process-detail.component.html',
  styleUrl: './process-detail.component.scss'
})
export class ProcessDetailComponent implements OnInit {
  private route = inject(ActivatedRoute);
  private processService = inject(SelectionProcessService);
  private applicationFormService = inject(ApplicationFormService);
  private authService = inject(AuthService);

  process?: SelectionProcessDetailModel;
  applicationForm?: ApplicationFormModel;
  isLoading = true;
  errorMessage = '';
  successMessage = '';
  showApplicationModal = false;
  downloadingApplicationId: number | null = null;
  updatingApplicationId: number | null = null;
  selectedApplication?: CandidateApplicationModel;

  ngOnInit(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    if (!Number.isFinite(id) || id <= 0) {
      this.errorMessage = 'Processo seletivo inválido.';
      this.isLoading = false;
      return;
    }
    this.loadProcess(id);
  }

  loadProcess(id: number): void {
    this.isLoading = true;
    this.errorMessage = '';
    this.processService.findById(id).subscribe({
      next: (process) => {
        this.process = process;
        this.isLoading = false;
        this.loadApplicationForm(process.jobPosition.id);
      },
      error: (error: unknown) => {
        this.isLoading = false;
        this.errorMessage = extractApiErrorMessage(error, 'Não foi possível carregar o processo seletivo.');
      }
    });
  }

  private loadApplicationForm(jobId: number): void {
    this.applicationFormService.findByJob(jobId).subscribe({
      next: (form) => this.applicationForm = form ?? undefined,
      error: () => this.applicationForm = undefined
    });
  }

  openApplicationModal(): void {
    this.showApplicationModal = true;
    this.errorMessage = '';
  }

  closeApplicationModal(): void {
    this.showApplicationModal = false;
  }

  onApplied(result: ApplyCandidateResponse): void {
    this.showApplicationModal = false;
    this.successMessage = `Candidatura de ${result.application.candidate.name} registada com sucesso.`;
    if (this.process) {
      this.loadProcess(this.process.id);
    }
  }

  onStatusChanged(change: ApplicationStatusChange): void {
    this.updatingApplicationId = change.application.id;
    this.errorMessage = '';
    this.successMessage = '';

    this.processService.updateApplicationStatus(change.application.id, change.status).subscribe({
      next: (updated) => {
        change.application.status = updated.status;
        this.updatingApplicationId = null;
        this.successMessage = `${change.application.candidate.name} movido para ${this.applicationStatusLabel(updated.status)}.`;
      },
      error: (error: unknown) => {
        change.application.status = change.previousStatus;
        this.updatingApplicationId = null;
        this.errorMessage = extractApiErrorMessage(error, 'Não foi possível atualizar a fase do candidato.');
      }
    });
  }

  onCardSelected(application: CandidateApplicationModel): void {
    this.selectedApplication = application;
  }

  closeApplicationDetails(): void {
    this.selectedApplication = undefined;
  }

  downloadResume(application: CandidateApplicationModel): void {
    this.downloadingApplicationId = application.id;
    this.processService.downloadResume(application.id).subscribe({
      next: (blob) => {
        const url = URL.createObjectURL(blob);
        const link = document.createElement('a');
        link.href = url;
        link.download = application.resume.filename;
        link.click();
        URL.revokeObjectURL(url);
        this.downloadingApplicationId = null;
      },
      error: (error: unknown) => {
        this.downloadingApplicationId = null;
        this.errorMessage = extractApiErrorMessage(error, 'Não foi possível descarregar o currículo.');
      }
    });
  }

  statusLabel(status: SelectionProcessStatus): string {
    const labels: Record<SelectionProcessStatus, string> = {
      OPEN: 'Aberto',
      PAUSED: 'Pausado',
      CLOSED: 'Fechado',
      CANCELLED: 'Cancelado'
    };
    return labels[status];
  }

  applicationStatusLabel(status: CandidateApplicationModel['status']): string {
    const labels: Record<CandidateApplicationModel['status'], string> = {
      TRIAGEM: 'Triagem',
      ENTRAR_EM_CONTATO: 'Entrar em Contato',
      ENTREVISTA_TECH_RECRUITER: 'Entrevista Tech Recruiter',
      APROVADO_ENTREVISTA_CLIENTE: 'Aprovado para Entrevista Cliente',
      APROVADO_CLIENTE: 'Aprovado pelo Cliente',
      REPROVADO: 'Reprovado',
      DECLINOU: 'Declinou',
      STAND_BY: 'Stand By'
    };
    return labels[status];
  }

  statusClass(status: SelectionProcessStatus): string {
    switch (status) {
      case 'OPEN': return 'bg-success-subtle text-success';
      case 'PAUSED': return 'bg-warning-subtle text-warning-emphasis';
      case 'CLOSED': return 'bg-secondary-subtle text-secondary';
      case 'CANCELLED': return 'bg-danger-subtle text-danger';
    }
  }

  logout(): void {
    this.authService.logout();
  }
}
