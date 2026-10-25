import { Component, EventEmitter, Input, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { CdkDragDrop, DragDropModule } from '@angular/cdk/drag-drop';
import {
  ApplicationStatusChange,
  CandidateApplicationModel,
  CandidateApplicationStatus
} from '../../models/selection-process.model';

interface KanbanColumn {
  status: CandidateApplicationStatus;
  label: string;
  icon: string;
  color: string;
  softColor: string;
  description: string;
}

@Component({
  selector: 'app-kanban-board',
  standalone: true,
  imports: [CommonModule, DragDropModule],
  templateUrl: './kanban-board.component.html',
  styleUrl: './kanban-board.component.scss'
})
export class KanbanBoardComponent {
  @Input() applications: CandidateApplicationModel[] = [];
  @Output() statusChanged = new EventEmitter<ApplicationStatusChange>();
  @Output() cardSelected = new EventEmitter<CandidateApplicationModel>();

  readonly columns: KanbanColumn[] = [
    {
      status: 'TRIAGEM',
      label: 'Triagem',
      icon: 'bi-search',
      color: '#1d4ed8',
      softColor: '#dbeafe',
      description: 'Currículos em análise inicial'
    },
    {
      status: 'ENTRAR_EM_CONTATO',
      label: 'Entrar em Contato',
      icon: 'bi-telephone',
      color: '#0e7490',
      softColor: '#cffafe',
      description: 'Contacto ativo com o candidato'
    },
    {
      status: 'ENTREVISTA_TECH_RECRUITER',
      label: 'Entrevista Tech Recruiter',
      icon: 'bi-person-badge',
      color: '#6d28d9',
      softColor: '#ede9fe',
      description: 'Entrevista com o recrutador técnico'
    },
    {
      status: 'APROVADO_ENTREVISTA_CLIENTE',
      label: 'Aprovado para Entrevista Cliente',
      icon: 'bi-building',
      color: '#4338ca',
      softColor: '#e0e7ff',
      description: 'Pronto para apresentar ao cliente'
    },
    {
      status: 'APROVADO_CLIENTE',
      label: 'Aprovado pelo Cliente',
      icon: 'bi-check-circle',
      color: '#15803d',
      softColor: '#dcfce7',
      description: 'Aprovação final do cliente'
    },
    {
      status: 'REPROVADO',
      label: 'Reprovado',
      icon: 'bi-x-circle',
      color: '#b91c1c',
      softColor: '#fee2e2',
      description: 'Candidato não avançou'
    },
    {
      status: 'DECLINOU',
      label: 'Declinou',
      icon: 'bi-hand-thumbs-down',
      color: '#c2410c',
      softColor: '#ffedd5',
      description: 'Candidato recusou a oportunidade'
    },
    {
      status: 'STAND_BY',
      label: 'Stand By',
      icon: 'bi-pause-circle',
      color: '#475569',
      softColor: '#e2e8f0',
      description: 'Processo suspenso temporariamente'
    }
  ];

  readonly columnIds = this.columns.map(column => column.status);

  applicationsFor(status: CandidateApplicationStatus): CandidateApplicationModel[] {
    return this.applications.filter(application => application.status === status);
  }

  drop(event: CdkDragDrop<CandidateApplicationModel[]>): void {
    const status = event.container.id as CandidateApplicationStatus;
    const application = event.item.data as CandidateApplicationModel | undefined;

    if (!application || application.status === status) {
      return;
    }

    const previousStatus = application.status;
    application.status = status;
    this.statusChanged.emit({ application, previousStatus, status });
  }

  select(application: CandidateApplicationModel): void {
    this.cardSelected.emit(application);
  }

  commentCount(application: CandidateApplicationModel): number {
    return application.comments?.length ?? 0;
  }

  trackById(_: number, application: CandidateApplicationModel): number {
    return application.id;
  }
}
