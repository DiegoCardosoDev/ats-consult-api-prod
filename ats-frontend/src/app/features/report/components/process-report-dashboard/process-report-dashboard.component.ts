import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { ReportService } from '../../services/report.service';
import { ProcessDashboard } from '../../models/process-dashboard.model';
import {
  CandidateApplicationStatus,
  SelectionProcessStatus
} from '../../../selection-process/models/selection-process.model';
import { AuthService } from '../../../../services/auth.service';
import { extractApiErrorMessage } from '../../../../core/utils/api-error.util';

interface PhaseDefinition {
  status: CandidateApplicationStatus;
  label: string;
  icon: string;
  color: string;
  softColor: string;
}

interface PhaseStat extends PhaseDefinition {
  total: number;
  percentage: number;
}

const TIMELINE_WIDTH = 600;
const TIMELINE_HEIGHT = 180;

@Component({
  selector: 'app-process-report-dashboard',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './process-report-dashboard.component.html',
  styleUrl: './process-report-dashboard.component.scss'
})
export class ProcessReportDashboardComponent implements OnInit {
  private reportService = inject(ReportService);
  private authService = inject(AuthService);
  private route = inject(ActivatedRoute);

  dashboard?: ProcessDashboard;
  isLoading = true;
  errorMessage = '';
  processId = 0;

  readonly phaseDefinitions: PhaseDefinition[] = [
    { status: 'TRIAGEM', label: 'Triagem', icon: 'bi-search', color: '#1d4ed8', softColor: '#dbeafe' },
    { status: 'ENTRAR_EM_CONTATO', label: 'Entrar em Contato', icon: 'bi-telephone', color: '#0e7490', softColor: '#cffafe' },
    { status: 'ENTREVISTA_TECH_RECRUITER', label: 'Entrevista Tech Recruiter', icon: 'bi-person-badge', color: '#6d28d9', softColor: '#ede9fe' },
    { status: 'APROVADO_ENTREVISTA_CLIENTE', label: 'Aprovado Entrevista Cliente', icon: 'bi-building', color: '#4338ca', softColor: '#e0e7ff' },
    { status: 'APROVADO_CLIENTE', label: 'Aprovado pelo Cliente', icon: 'bi-check-circle', color: '#15803d', softColor: '#dcfce7' },
    { status: 'CONTRATADO', label: 'Contratado', icon: 'bi-patch-check-fill', color: '#047857', softColor: '#d1fae5' },
    { status: 'REPROVADO', label: 'Reprovado', icon: 'bi-x-circle', color: '#b91c1c', softColor: '#fee2e2' },
    { status: 'DECLINOU', label: 'Declinou', icon: 'bi-hand-thumbs-down', color: '#c2410c', softColor: '#ffedd5' },
    { status: 'STAND_BY', label: 'Stand By', icon: 'bi-pause-circle', color: '#475569', softColor: '#e2e8f0' }
  ];

  ngOnInit(): void {
    this.processId = Number(this.route.snapshot.paramMap.get('id'));
    if (!Number.isFinite(this.processId) || this.processId <= 0) {
      this.errorMessage = 'Processo seletivo inválido.';
      this.isLoading = false;
      return;
    }
    this.loadDashboard();
  }

  loadDashboard(): void {
    this.isLoading = true;
    this.errorMessage = '';
    this.reportService.findProcessDashboard(this.processId).subscribe({
      next: (dashboard) => {
        this.dashboard = dashboard;
        this.isLoading = false;
      },
      error: (error: unknown) => {
        this.isLoading = false;
        this.errorMessage = extractApiErrorMessage(error, 'Não foi possível carregar o dashboard do processo.');
      }
    });
  }

  get phaseStats(): PhaseStat[] {
    if (!this.dashboard) {
      return [];
    }
    const total = this.dashboard.totalApplications;
    return this.phaseDefinitions.map((definition) => {
      const value = this.dashboard?.statusCounts?.[definition.status] ?? 0;
      return {
        ...definition,
        total: value,
        percentage: total > 0 ? (value * 100) / total : 0
      };
    });
  }

  get maxPhaseTotal(): number {
    return Math.max(1, ...this.phaseStats.map((phase) => phase.total));
  }

  get donutGradient(): string {
    const phases = this.phaseStats.filter((phase) => phase.total > 0);
    if (phases.length === 0) {
      return 'conic-gradient(#e2e8f0 0% 100%)';
    }
    let cursor = 0;
    const stops = phases.map((phase) => {
      const start = cursor;
      cursor += phase.percentage;
      return `${phase.color} ${start}% ${cursor}%`;
    });
    if (cursor < 100) {
      stops.push(`#e2e8f0 ${cursor}% 100%`);
    }
    return `conic-gradient(${stops.join(', ')})`;
  }

  get adherenceGradient(): string {
    const value = Math.min(100, Math.max(0, this.dashboard?.averageAdherence ?? 0));
    const color = value >= 75 ? '#22c55e' : value >= 50 ? '#f59e0b' : '#ef4444';
    return `conic-gradient(${color} 0% ${value}%, #e2e8f0 ${value}% 100%)`;
  }

  get timelinePoints(): { x: number; y: number }[] {
    const data = this.dashboard?.timeline ?? [];
    const max = Math.max(1, ...data.map((point) => point.total));
    return data.map((point, index) => ({
      x: data.length <= 1 ? TIMELINE_WIDTH / 2 : (index / (data.length - 1)) * TIMELINE_WIDTH,
      y: TIMELINE_HEIGHT - 20 - (point.total / max) * (TIMELINE_HEIGHT - 40)
    }));
  }

  get timelinePolyline(): string {
    return this.timelinePoints
      .map((point) => `${point.x.toFixed(1)},${point.y.toFixed(1)}`)
      .join(' ');
  }

  get timelineAreaPath(): string {
    const points = this.timelinePoints;
    if (points.length === 0) {
      return '';
    }
    const line = points.map((point) => `${point.x.toFixed(1)},${point.y.toFixed(1)}`).join(' L ');
    return `M ${points[0].x.toFixed(1)},${TIMELINE_HEIGHT - 20} L ${line} L ${points[points.length - 1].x.toFixed(1)},${TIMELINE_HEIGHT - 20} Z`;
  }

  get timelineStartLabel(): string {
    const first = this.dashboard?.timeline?.[0];
    return first ? this.shortDate(first.date) : '';
  }

  get timelineEndLabel(): string {
    const data = this.dashboard?.timeline ?? [];
    const last = data[data.length - 1];
    return last ? this.shortDate(last.date) : '';
  }

  get timelinePeak(): number {
    return Math.max(0, ...(this.dashboard?.timeline ?? []).map((point) => point.total));
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
      case 'OPEN': return 'status-pill--open';
      case 'PAUSED': return 'status-pill--paused';
      case 'CLOSED': return 'status-pill--closed';
      case 'CANCELLED': return 'status-pill--cancelled';
    }
  }

  phaseFor(status: CandidateApplicationStatus): PhaseDefinition {
    return this.phaseDefinitions.find((phase) => phase.status === status) ?? this.phaseDefinitions[0];
  }

  initials(name: string | null): string {
    if (!name) {
      return '?';
    }
    return name.trim().split(/\s+/).slice(0, 2).map((part) => part[0]?.toUpperCase() ?? '').join('');
  }

  shortDate(value: string): string {
    const date = new Date(value);
    return Number.isNaN(date.getTime()) ? value : date.toLocaleDateString('pt-PT', { day: '2-digit', month: 'short' });
  }

  logout(): void {
    this.authService.logout();
  }
}
