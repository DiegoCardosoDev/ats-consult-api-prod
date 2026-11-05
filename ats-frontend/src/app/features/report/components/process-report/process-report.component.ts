import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { ReportService } from '../../services/report.service';
import { ProcessReportItem, ProcessReportPage } from '../../models/process-report.model';
import { ClientService } from '../../../client/services/client.service';
import { ClientModel } from '../../../client/models/client.model';
import { JobPositionService } from '../../../job-position/services/job-position.service';
import { JobPosition } from '../../../job-position/models/job-position.model';
import {
  CandidateApplicationStatus,
  SelectionProcessStatus
} from '../../../selection-process/models/selection-process.model';
import { AuthService } from '../../../../services/auth.service';
import { extractApiErrorMessage } from '../../../../core/utils/api-error.util';

@Component({
  selector: 'app-process-report',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  templateUrl: './process-report.component.html'
})
export class ProcessReportComponent implements OnInit {
  private reportService = inject(ReportService);
  private clientService = inject(ClientService);
  private jobService = inject(JobPositionService);
  private authService = inject(AuthService);
  private fb = inject(FormBuilder);

  report?: ProcessReportPage;
  clients: ClientModel[] = [];
  jobs: JobPosition[] = [];
  isLoading = true;
  errorMessage = '';
  page = 0;
  size = 20;
  expandedProcessId: number | null = null;

  readonly applicationStatusColumns: { status: CandidateApplicationStatus; label: string; className: string }[] = [
    { status: 'TRIAGEM', label: 'Triagem', className: 'text-primary' },
    { status: 'ENTRAR_EM_CONTATO', label: 'Entrar em Contato', className: 'text-info' },
    { status: 'ENTREVISTA_TECH_RECRUITER', label: 'Entrevista Tech Recruiter', className: 'text-primary' },
    { status: 'APROVADO_ENTREVISTA_CLIENTE', label: 'Aprovado Entrevista Cliente', className: 'text-info' },
    { status: 'APROVADO_CLIENTE', label: 'Aprovado pelo Cliente', className: 'text-success' },
    { status: 'CONTRATADO', label: 'Contratado', className: 'text-success-emphasis' },
    { status: 'REPROVADO', label: 'Reprovado', className: 'text-danger' },
    { status: 'DECLINOU', label: 'Declinou', className: 'text-warning-emphasis' },
    { status: 'STAND_BY', label: 'Stand By', className: 'text-secondary' }
  ];

  filterForm = this.fb.group({
    clientId: [null as number | null],
    jobPositionId: [null as number | null],
    status: [null as SelectionProcessStatus | null]
  });

  ngOnInit(): void {
    this.loadClients();
    this.loadJobs();
    this.loadReport(0);
  }

  loadReport(page = this.page): void {
    this.isLoading = true;
    this.errorMessage = '';
    const filters = this.filterForm.getRawValue();

    this.reportService.findProcessReport({
      clientId: filters.clientId ?? undefined,
      jobPositionId: filters.jobPositionId ?? undefined,
      status: filters.status ?? undefined,
      page,
      size: this.size
    }).subscribe({
      next: (report) => {
        this.report = report;
        this.page = report.processes.page;
        this.expandedProcessId = null;
        this.isLoading = false;
      },
      error: (error: unknown) => {
        this.isLoading = false;
        this.errorMessage = extractApiErrorMessage(error, 'Não foi possível gerar o relatório de processos.');
      }
    });
  }

  applyFilters(): void {
    this.loadReport(0);
  }

  clearFilters(): void {
    this.filterForm.reset({ clientId: null, jobPositionId: null, status: null });
    this.loadReport(0);
  }

  nextPage(): void {
    if (this.report && !this.report.processes.last && !this.isLoading) {
      this.loadReport(this.page + 1);
    }
  }

  previousPage(): void {
    if (this.report && !this.report.processes.first && !this.isLoading) {
      this.loadReport(this.page - 1);
    }
  }

  toggleDetails(processId: number): void {
    this.expandedProcessId = this.expandedProcessId === processId ? null : processId;
  }

  isExpanded(processId: number): boolean {
    return this.expandedProcessId === processId;
  }

  applicationCount(item: ProcessReportItem, status: CandidateApplicationStatus): number {
    return item.statusCounts?.[status] ?? 0;
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

  private loadClients(): void {
    this.clientService.findAll().subscribe({
      next: (clients) => this.clients = clients,
      error: () => this.errorMessage = 'Não foi possível carregar os clientes.'
    });
  }

  private loadJobs(): void {
    this.jobService.findAll(undefined, 0, 100).subscribe({
      next: (page) => this.jobs = page.content,
      error: () => this.errorMessage = 'Não foi possível carregar as vagas.'
    });
  }
}
