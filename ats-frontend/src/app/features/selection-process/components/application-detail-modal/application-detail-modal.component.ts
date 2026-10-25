import { Component, EventEmitter, HostListener, Input, OnInit, Output, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import {
  ApplicationCommentModel,
  CandidateApplicationModel,
  CandidateApplicationStatus
} from '../../models/selection-process.model';
import { SelectionProcessService } from '../../services/selection-process.service';
import { extractApiErrorMessage } from '../../../../core/utils/api-error.util';
import { formAnswerLabel } from '../../../technology/technology-category.util';

const STATUS_LABELS: Record<CandidateApplicationStatus, string> = {
  TRIAGEM: 'Triagem',
  ENTRAR_EM_CONTATO: 'Entrar em Contato',
  ENTREVISTA_TECH_RECRUITER: 'Entrevista Tech Recruiter',
  APROVADO_ENTREVISTA_CLIENTE: 'Aprovado para Entrevista Cliente',
  APROVADO_CLIENTE: 'Aprovado pelo Cliente',
  REPROVADO: 'Reprovado',
  DECLINOU: 'Declinou',
  STAND_BY: 'Stand By'
};

@Component({
  selector: 'app-application-detail-modal',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './application-detail-modal.component.html',
  styleUrl: './application-detail-modal.component.scss'
})
export class ApplicationDetailModalComponent implements OnInit {
  @Input({ required: true }) application!: CandidateApplicationModel;
  @Output() closed = new EventEmitter<void>();
  @Output() downloadRequested = new EventEmitter<CandidateApplicationModel>();

  private processService = inject(SelectionProcessService);
  private fb = inject(FormBuilder);

  isSubmittingComment = false;
  errorMessage = '';
  successMessage = '';

  commentForm = this.fb.group({
    message: ['', [Validators.required, Validators.maxLength(2000)]]
  });

  ngOnInit(): void {
    this.loadComments();
  }

  @HostListener('document:keydown.escape')
  onEscape(): void {
    this.close();
  }

  get comments(): ApplicationCommentModel[] {
    return [...(this.application.comments ?? [])].sort((a, b) => {
      const dateDiff = new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime();
      return dateDiff !== 0 ? dateDiff : b.id - a.id;
    });
  }

  get messageLength(): number {
    return this.commentForm.controls.message.value?.length ?? 0;
  }

  statusLabel(status: CandidateApplicationStatus): string {
    return STATUS_LABELS[status] ?? status;
  }

  statusClass(status: CandidateApplicationStatus): string {
    return `status-badge--${status.toLowerCase().replaceAll('_', '-')}`;
  }

  scoreClass(percentage: number): string {
    if (percentage >= 75) {
      return 'is-high';
    }
    return percentage >= 50 ? 'is-medium' : 'is-low';
  }

  scoreLabel(percentage: number): string {
    if (percentage >= 75) {
      return 'Alta';
    }
    return percentage >= 50 ? 'Média' : 'Baixa';
  }

  answerLabel(answer: { questionKey?: string; questionPrompt: string; answer: string }): string {
    return formAnswerLabel(answer.questionKey, answer.answer, answer.questionPrompt);
  }

  loadComments(): void {
    this.processService.findApplicationComments(this.application.id).subscribe({
      next: (comments) => this.application.comments = comments,
      error: () => {
        this.errorMessage = 'Não foi possível atualizar os comentários.';
      }
    });
  }

  submitComment(): void {
    if (this.commentForm.invalid || this.isSubmittingComment) {
      this.commentForm.markAllAsTouched();
      return;
    }

    const message = (this.commentForm.controls.message.value ?? '').trim();
    if (!message) {
      return;
    }

    this.isSubmittingComment = true;
    this.errorMessage = '';
    this.successMessage = '';

    this.processService.addApplicationComment(this.application.id, message).subscribe({
      next: (comment) => {
        this.application.comments = [comment, ...(this.application.comments ?? [])];
        this.commentForm.reset({ message: '' });
        this.isSubmittingComment = false;
        this.successMessage = 'Comentário registado.';
      },
      error: (error: unknown) => {
        this.isSubmittingComment = false;
        this.errorMessage = extractApiErrorMessage(error, 'Não foi possível registar o comentário.');
      }
    });
  }

  downloadResume(): void {
    this.downloadRequested.emit(this.application);
  }

  close(): void {
    this.closed.emit();
  }
}
