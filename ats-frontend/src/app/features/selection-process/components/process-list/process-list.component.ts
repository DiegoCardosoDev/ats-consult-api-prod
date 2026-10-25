import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { SelectionProcessService } from '../../services/selection-process.service';
import { SelectionProcessStatus, SelectionProcessSummaryModel } from '../../models/selection-process.model';
import { ClientService } from '../../../client/services/client.service';
import { ClientModel } from '../../../client/models/client.model';
import { JobPositionService } from '../../../job-position/services/job-position.service';
import { JobPosition } from '../../../job-position/models/job-position.model';
import { AuthService } from '../../../../services/auth.service';
import { extractApiErrorMessage } from '../../../../core/utils/api-error.util';

@Component({
  selector: 'app-process-list',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  templateUrl: './process-list.component.html'
})
export class ProcessListComponent implements OnInit {
  private processService = inject(SelectionProcessService);
  private clientService = inject(ClientService);
  private jobService = inject(JobPositionService);
  private authService = inject(AuthService);
  private fb = inject(FormBuilder);

  processes: SelectionProcessSummaryModel[] = [];
  clients: ClientModel[] = [];
  jobs: JobPosition[] = [];
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

  processForm = this.fb.group({
    clientId: [null as number | null, [Validators.required]],
    jobPositionId: [null as number | null, [Validators.required]],
    status: ['OPEN' as SelectionProcessStatus, [Validators.required]],
    openedAt: [this.today(), [Validators.required]]
  });

  ngOnInit(): void {
    this.loadProcesses(0);
    this.loadFormData();
  }

  loadProcesses(page = this.page): void {
    this.isLoading = true;
    this.errorMessage = '';

    this.processService.findAll(page, this.size).subscribe({
      next: (data) => {
        this.processes = data.content;
        this.page = data.page;
        this.totalElements = data.totalElements;
        this.totalPages = data.totalPages;
        this.first = data.first;
        this.last = data.last;
        this.isLoading = false;
      },
      error: (error: unknown) => {
        this.isLoading = false;
        this.errorMessage = extractApiErrorMessage(error, 'Não foi possível carregar os processos seletivos.');
      }
    });
  }

  private loadFormData(): void {
    this.clientService.findAll().subscribe({
      next: (clients) => this.clients = clients,
      error: () => this.errorMessage = 'Não foi possível carregar os clientes.'
    });

    this.jobService.findAll(undefined, 0, 100).subscribe({
      next: (page) => this.jobs = page.content,
      error: () => this.errorMessage = 'Não foi possível carregar as vagas.'
    });
  }

  openCreateModal(): void {
    this.processForm.reset({
      clientId: null,
      jobPositionId: null,
      status: 'OPEN',
      openedAt: this.today()
    });
    this.errorMessage = '';
    this.successMessage = '';
    this.isModalOpen = true;
  }

  closeModal(): void {
    this.isModalOpen = false;
    this.isSubmitting = false;
  }

  onJobChange(): void {
    const jobId = this.processForm.controls.jobPositionId.value;
    const job = this.jobs.find(item => item.id === jobId);
    this.processForm.patchValue({ clientId: job?.client?.id ?? null });
  }

  submit(): void {
    if (this.processForm.invalid) {
      this.processForm.markAllAsTouched();
      return;
    }

    const raw = this.processForm.getRawValue();
    if (raw.clientId == null || raw.jobPositionId == null) {
      return;
    }

    this.isSubmitting = true;
    this.errorMessage = '';

    this.processService.create({
      clientId: raw.clientId,
      jobPositionId: raw.jobPositionId,
      status: raw.status ?? 'OPEN',
      openedAt: raw.openedAt ?? this.today()
    }).subscribe({
      next: (process) => {
        this.isSubmitting = false;
        this.isModalOpen = false;
        this.successMessage = `Processo #${process.id} criado com sucesso.`;
        this.loadProcesses(0);
      },
      error: (error: unknown) => {
        this.isSubmitting = false;
        this.errorMessage = extractApiErrorMessage(error, 'Não foi possível criar o processo seletivo.');
      }
    });
  }

  nextPage(): void {
    if (!this.last && !this.isLoading) {
      this.loadProcesses(this.page + 1);
    }
  }

  previousPage(): void {
    if (!this.first && !this.isLoading) {
      this.loadProcesses(this.page - 1);
    }
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

  private today(): string {
    return new Date().toISOString().substring(0, 10);
  }
}
