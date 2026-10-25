import { Component, ElementRef, EventEmitter, Input, OnInit, Output, ViewChild, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AbstractControl, FormBuilder, ReactiveFormsModule, ValidationErrors, Validators } from '@angular/forms';
import { CandidateModel } from '../../../candidate/models/candidate.model';
import { ApplyCandidateResponse } from '../../models/selection-process.model';
import { SelectionProcessService } from '../../services/selection-process.service';
import { extractApiErrorMessage } from '../../../../core/utils/api-error.util';
import {
  ApplicationFormModel,
  ApplicationFormQuestionModel,
  FormAnswerPayload,
  QuestionType
} from '../../../application-form/models/application-form.model';

@Component({
  selector: 'app-candidate-application-modal',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './candidate-application-modal.component.html',
  styleUrl: './candidate-application-modal.component.scss'
})
export class CandidateApplicationModalComponent implements OnInit {
  @Input({ required: true }) mode!: 'job' | 'process';
  @Input({ required: true }) targetId!: number;
  @Input() applicationForm?: ApplicationFormModel;

  @Output() closed = new EventEmitter<void>();
  @Output() applied = new EventEmitter<ApplyCandidateResponse>();

  @ViewChild('fileInput') fileInput?: ElementRef<HTMLInputElement>;

  private processService = inject(SelectionProcessService);
  private fb = inject(FormBuilder);

  isSubmitting = false;
  isSuccess = false;
  errorMessage = '';
  selectedResume: File | null = null;
  successResult?: ApplyCandidateResponse;

  step: 'questions' | 'candidate' = 'candidate';
  answersForm = this.fb.group({});

  applicationFormGroup = this.fb.group({
    name: ['', [Validators.required, Validators.maxLength(150)]],
    age: [null as number | null, [Validators.required, Validators.min(16), Validators.max(120)]],
    role: ['', [Validators.required, Validators.maxLength(150)]],
    linkedinUrl: ['', [Validators.pattern(/^$|^https?:\/\/.+/), Validators.maxLength(500)]],
    email: ['', [Validators.required, Validators.email, Validators.maxLength(255)]],
    phone: ['', [Validators.required, Validators.minLength(8), Validators.maxLength(30)]],
    street: [''],
    number: [''],
    complement: [''],
    neighborhood: [''],
    city: [''],
    state: [''],
    zipCode: [''],
    country: ['']
  });

  ngOnInit(): void {
    if (this.hasQuestions) {
      this.buildAnswersForm();
      this.step = 'questions';
    }
  }

  get questions(): ApplicationFormQuestionModel[] {
    return [...(this.applicationForm?.questions ?? [])]
      .sort((a, b) => (a.position ?? 0) - (b.position ?? 0));
  }

  get hasQuestions(): boolean {
    return !!this.applicationForm?.active && this.questions.length > 0;
  }

  answerControlName(question: ApplicationFormQuestionModel): string {
    return `q_${question.id}`;
  }

  answerControl(question: ApplicationFormQuestionModel): AbstractControl | null {
    return this.answersForm.get(this.answerControlName(question));
  }

  isChoiceType(type: QuestionType): boolean {
    return type === 'SINGLE_CHOICE' || type === 'MULTIPLE_CHOICE';
  }

  onFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    const file = input.files?.[0] ?? null;

    if (!file) {
      this.selectedResume = null;
      return;
    }
    if (!file.name.toLowerCase().endsWith('.pdf')) {
      this.selectedResume = null;
      input.value = '';
      this.errorMessage = 'O currículo deve estar no formato PDF.';
      return;
    }
    if (file.size > 10 * 1024 * 1024) {
      this.selectedResume = null;
      input.value = '';
      this.errorMessage = 'O currículo não pode exceder 10 MB.';
      return;
    }

    this.errorMessage = '';
    this.selectedResume = file;
  }

  continueToCandidate(): void {
    if (this.answersForm.invalid) {
      this.answersForm.markAllAsTouched();
      this.errorMessage = 'Responda às perguntas obrigatórias antes de continuar.';
      return;
    }
    this.errorMessage = '';
    this.step = 'candidate';
  }

  backToQuestions(): void {
    this.step = 'questions';
    this.errorMessage = '';
  }

  submit(): void {
    if (this.hasQuestions && this.step === 'questions') {
      this.continueToCandidate();
      return;
    }

    if (this.applicationFormGroup.invalid) {
      this.applicationFormGroup.markAllAsTouched();
      return;
    }
    if (!this.selectedResume) {
      this.errorMessage = 'Selecione o currículo em PDF.';
      return;
    }

    const raw = this.applicationFormGroup.getRawValue();
    const candidate: CandidateModel = {
      name: raw.name ?? '',
      age: raw.age ?? 0,
      role: raw.role ?? '',
      linkedinUrl: raw.linkedinUrl || undefined,
      email: raw.email ?? '',
      phone: raw.phone ?? '',
      address: {
        street: raw.street || undefined,
        number: raw.number || undefined,
        complement: raw.complement || undefined,
        neighborhood: raw.neighborhood || undefined,
        city: raw.city || undefined,
        state: raw.state || undefined,
        zipCode: raw.zipCode || undefined,
        country: raw.country || undefined
      }
    };

    this.isSubmitting = true;
    this.errorMessage = '';

    const answers = this.collectAnswers();
    const request$ = this.mode === 'job'
      ? this.processService.applyToJob(this.targetId, candidate, this.selectedResume, answers)
      : this.processService.applyToProcess(this.targetId, candidate, this.selectedResume, answers);

    request$.subscribe({
      next: (result) => {
        this.isSubmitting = false;
        this.successResult = result;
        this.isSuccess = true;
      },
      error: (error: unknown) => {
        this.isSubmitting = false;
        this.errorMessage = extractApiErrorMessage(error, 'Não foi possível registar a candidatura.');
      }
    });
  }

  finishSuccess(): void {
    if (!this.successResult) {
      return;
    }
    const result = this.successResult;
    this.resetForm();
    this.applied.emit(result);
  }

  close(): void {
    if (this.isSubmitting) {
      return;
    }
    if (this.isSuccess) {
      this.finishSuccess();
      return;
    }
    this.resetForm();
    this.closed.emit();
  }

  private buildAnswersForm(): void {
    for (const question of this.questions) {
      if (question.id == null) {
        continue;
      }
      const validators = question.required
        ? [question.type === 'MULTIPLE_CHOICE' ? this.multipleChoiceRequired : Validators.required]
        : [];
      this.answersForm.addControl(
        this.answerControlName(question),
        this.fb.control(question.type === 'MULTIPLE_CHOICE' ? [] : '', validators)
      );
    }
  }

  private multipleChoiceRequired(control: AbstractControl): ValidationErrors | null {
    return Array.isArray(control.value) && control.value.length > 0
      ? null
      : { required: true };
  }

  private collectAnswers(): FormAnswerPayload[] {
    if (!this.hasQuestions) {
      return [];
    }
    const answers: FormAnswerPayload[] = [];
    for (const question of this.questions) {
      if (question.id == null) {
        continue;
      }
      let value = this.answersForm.get(this.answerControlName(question))?.value;
      if (Array.isArray(value)) {
        value = value.join('; ');
      }
      if (value != null && String(value).trim() !== '') {
        answers.push({ questionId: question.id, answer: String(value).trim() });
      }
    }
    return answers;
  }

  private resetForm(): void {
    this.applicationFormGroup.reset();
    this.answersForm.reset();
    this.selectedResume = null;
    this.isSuccess = false;
    this.successResult = undefined;
    this.step = this.hasQuestions ? 'questions' : 'candidate';
    if (this.fileInput) {
      this.fileInput.nativeElement.value = '';
    }
  }
}
