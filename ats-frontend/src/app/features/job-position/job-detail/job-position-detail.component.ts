import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { JobPosition } from '../models/job-position.model';
import { JobPositionService } from '../services/job-position.service';
import { AuthService } from '../../../services/auth.service';
import { extractApiErrorMessage } from '../../../core/utils/api-error.util';
import { CandidateApplicationModalComponent } from '../../selection-process/components/candidate-application-modal/candidate-application-modal.component';
import { ApplyCandidateResponse } from '../../selection-process/models/selection-process.model';
import { ApplicationFormModel } from '../../application-form/models/application-form.model';
import { ApplicationFormService } from '../../application-form/services/application-form.service';
import { ApplicationFormEditorComponent } from '../../application-form/components/application-form-editor/application-form-editor.component';

@Component({
  selector: 'app-job-position-detail',
  standalone: true,
  imports: [CommonModule, RouterLink, CandidateApplicationModalComponent, ApplicationFormEditorComponent],
  templateUrl: './job-position-detail.component.html',
  styleUrl: './job-position-detail.component.scss'
})
export class JobPositionDetailComponent implements OnInit {
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private jobPositionService = inject(JobPositionService);
  readonly authService = inject(AuthService);
  private applicationFormService = inject(ApplicationFormService);

  job?: JobPosition;
  isLoading = true;
  errorMessage = '';

  applicationForm?: ApplicationFormModel;
  isLoadingForm = false;
  formErrorMessage = '';
  showFormEditor = false;

  // 🟢 Variável de controle do modal personalizado
  isDeleteModalOpen = false;

  statusModalOpen = false;
  statusModalTarget: 'CLOSED' | 'FROZEN' | null = null;
  statusReasonInput = '';
  statusChangeError = '';
  statusSuccessMessage = '';
  isChangingStatus = false;

  showApplicationModal = false;
  applicationSuccessMessage = '';
  appliedProcessId?: number;

  ngOnInit(): void {
    const idParam = this.route.snapshot.paramMap.get('id');
    if (idParam) {
      this.loadJobDetail(+idParam);
      this.loadApplicationForm(+idParam);
    } else {
      this.errorMessage = 'ID da vaga inválido.';
      this.isLoading = false;
    }
  }

  loadJobDetail(id: number): void {
    this.isLoading = true;
    this.jobPositionService.findById(id).subscribe({
      next: (data) => {
        this.job = data;
        this.isLoading = false;
      },
      error: (err) => {
        console.error('Erro ao carregar detalhes da vaga:', err);
        this.errorMessage = extractApiErrorMessage(err, 'Não foi possível carregar os detalhes da vaga.');
        this.isLoading = false;
      }
    });
  }

  getWorkModelLabel(model?: string): string {
    switch (model) {
      case 'HYBRID': return 'Híbrido';
      case 'REMOTE': return 'Remoto';
      case 'ON_SITE': return 'Presencial';
      default: return model || '';
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
      'PAUSED': 'Pausada',
      'FROZEN': 'Congelada',
      'DRAFT': 'Rascunho'
    };
    return status ? (labels[status] || status) : '';
  }

  statusBadgeClass(status?: string): string {
    switch (status) {
      case 'OPEN': return 'bg-success text-white';
      case 'CLOSED': return 'bg-danger text-white';
      case 'FROZEN': return 'bg-warning text-dark';
      case 'PAUSED': return 'bg-warning-subtle text-warning-emphasis';
      case 'DRAFT': return 'bg-secondary text-white';
      default: return 'bg-secondary text-white';
    }
  }

  statusIcon(status?: string): string {
    switch (status) {
      case 'OPEN': return 'bi-check-circle-fill';
      case 'CLOSED': return 'bi-x-circle-fill';
      case 'FROZEN': return 'bi-snow';
      case 'PAUSED': return 'bi-pause-circle-fill';
      case 'DRAFT': return 'bi-pencil-fill';
      default: return 'bi-circle-fill';
    }
  }

  get canCloseJob(): boolean {
    return !!this.job && this.job.status !== 'CLOSED';
  }

  get canFreezeJob(): boolean {
    return !!this.job && this.job.status !== 'FROZEN' && this.job.status !== 'CLOSED';
  }

  get canReopenJob(): boolean {
    return this.job?.status === 'CLOSED' || this.job?.status === 'FROZEN';
  }

  openStatusModal(status: 'CLOSED' | 'FROZEN'): void {
    this.statusModalTarget = status;
    this.statusReasonInput = '';
    this.statusChangeError = '';
    this.statusModalOpen = true;
  }

  closeStatusModal(): void {
    if (this.isChangingStatus) {
      return;
    }
    this.statusModalOpen = false;
    this.statusModalTarget = null;
    this.statusReasonInput = '';
    this.statusChangeError = '';
  }

  confirmStatusChange(): void {
    if (!this.job?.id || !this.statusModalTarget) {
      return;
    }
    const reason = this.statusReasonInput.trim();
    if (reason.length < 3) {
      this.statusChangeError = 'Descreva o motivo com pelo menos 3 caracteres.';
      return;
    }
    if (reason.length > 500) {
      this.statusChangeError = 'O motivo não pode exceder 500 caracteres.';
      return;
    }

    this.isChangingStatus = true;
    this.statusChangeError = '';
    this.jobPositionService.changeStatus(this.job.id, this.statusModalTarget, reason).subscribe({
      next: (updated) => {
        this.isChangingStatus = false;
        this.statusModalOpen = false;
        this.statusModalTarget = null;
        this.statusReasonInput = '';
        this.job = updated;
        this.statusSuccessMessage = updated.status === 'CLOSED'
          ? 'Vaga fechada com sucesso.'
          : 'Vaga congelada com sucesso.';
      },
      error: (err: unknown) => {
        this.isChangingStatus = false;
        this.statusChangeError = extractApiErrorMessage(err, 'Não foi possível atualizar o estado da vaga.');
      }
    });
  }

  reopenJob(): void {
    if (!this.job?.id) {
      return;
    }
    if (!confirm('Deseja reabrir esta vaga? Ela voltará a receber candidaturas.')) {
      return;
    }

    this.isChangingStatus = true;
    this.statusChangeError = '';
    this.jobPositionService.changeStatus(this.job.id, 'OPEN').subscribe({
      next: (updated) => {
        this.isChangingStatus = false;
        this.job = updated;
        this.statusSuccessMessage = 'Vaga reaberta com sucesso.';
      },
      error: (err: unknown) => {
        this.isChangingStatus = false;
        this.statusChangeError = extractApiErrorMessage(err, 'Não foi possível reabrir a vaga.');
      }
    });
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

  logout(): void {
    this.authService.logout();
  }

  loadApplicationForm(jobId: number): void {
    this.isLoadingForm = true;
    this.formErrorMessage = '';
    this.applicationFormService.findByJob(jobId).subscribe({
      next: (form) => {
        this.applicationForm = form ?? undefined;
        this.isLoadingForm = false;
      },
      error: (error) => {
        this.isLoadingForm = false;
        this.formErrorMessage = extractApiErrorMessage(error, 'Não foi possível carregar o formulário da vaga.');
      }
    });
  }

  openFormEditor(): void {
    this.showFormEditor = true;
  }

  closeFormEditor(): void {
    this.showFormEditor = false;
  }

  onFormSaved(form: ApplicationFormModel): void {
    this.applicationForm = form;
    this.showFormEditor = false;
  }

  restoreDefaultApplicationForm(): void {
    if (!this.job?.id) {
      return;
    }
    if (!confirm('Restaurar o formulário padrão desta vaga? As perguntas atuais serão substituídas.')) {
      return;
    }
    this.isLoadingForm = true;
    this.formErrorMessage = '';
    this.applicationFormService.generate(this.job.id).subscribe({
      next: (form) => {
        this.applicationForm = form;
        this.isLoadingForm = false;
      },
      error: (error) => {
        this.isLoadingForm = false;
        this.formErrorMessage = extractApiErrorMessage(
          error, 'Não foi possível restaurar o formulário padrão da vaga.');
      }
    });
  }

  openApplicationModal(): void {
    this.showApplicationModal = true;
    this.applicationSuccessMessage = '';
  }

  closeApplicationModal(): void {
    this.showApplicationModal = false;
  }

  onApplicationApplied(result: ApplyCandidateResponse): void {
    this.showApplicationModal = false;
    this.appliedProcessId = result.selectionProcessId;
    this.applicationSuccessMessage = `Candidatura de ${result.application.candidate.name} registada no processo #${result.selectionProcessId}.`;
  }

  onEdit(id: number): void {
    this.router.navigate(['/jobs/edit', id]);
  }

  // 🟢 Abre o modal personalizado
  onDelete(id: number): void {
    this.isDeleteModalOpen = true;
  }

  // 🟢 Fecha o modal sem apagar
  closeDeleteModal(): void {
    this.isDeleteModalOpen = false;
  }

  // 🟢 Confirma e executa a deleção na API
  confirmDelete(): void {
    if (!this.job?.id) return;

    this.jobPositionService.delete(this.job.id).subscribe({
      next: () => {
        this.isDeleteModalOpen = false;
        this.router.navigate(['/jobs']);
      },
      error: (err) => {
        this.isDeleteModalOpen = false;
        console.error('Erro ao apagar vaga', err);
        alert(extractApiErrorMessage(err, 'Não foi possível apagar a vaga.'));
      }
    });
  }
}