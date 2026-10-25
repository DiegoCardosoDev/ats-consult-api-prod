import { Component, EventEmitter, Input, OnInit, Output, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormArray, FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ApplicationFormModel, QuestionType } from '../../models/application-form.model';
import { ApplicationFormService } from '../../services/application-form.service';
import { extractApiErrorMessage } from '../../../../core/utils/api-error.util';

interface QuestionTypeOption {
  value: QuestionType;
  label: string;
}

@Component({
  selector: 'app-application-form-editor',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './application-form-editor.component.html',
  styleUrl: './application-form-editor.component.scss'
})
export class ApplicationFormEditorComponent implements OnInit {
  @Input({ required: true }) jobId!: number;
  @Input() jobTitle = '';
  @Output() closed = new EventEmitter<void>();
  @Output() saved = new EventEmitter<ApplicationFormModel>();

  private fb = inject(FormBuilder);
  private formService = inject(ApplicationFormService);

  isLoading = false;
  isSaving = false;
  errorMessage = '';
  successMessage = '';

  readonly questionTypes: QuestionTypeOption[] = [
    { value: 'TEXT', label: 'Texto curto' },
    { value: 'TEXTAREA', label: 'Texto longo' },
    { value: 'YES_NO', label: 'Sim / Não' },
    { value: 'NUMBER', label: 'Número' },
    { value: 'SINGLE_CHOICE', label: 'Escolha única' },
    { value: 'MULTIPLE_CHOICE', label: 'Múltipla escolha' }
  ];

  form = this.fb.group({
    title: ['Formulário de candidatura', [Validators.required, Validators.maxLength(255)]],
    description: ['', [Validators.maxLength(5000)]],
    active: [true],
    questions: this.fb.array([])
  });

  ngOnInit(): void {
    this.isLoading = true;
    this.formService.findByJob(this.jobId).subscribe({
      next: (existing) => {
        this.isLoading = false;
        if (existing) {
          this.populate(existing);
        } else {
          this.addQuestion('TEXT');
        }
      },
      error: (error) => {
        this.isLoading = false;
        this.errorMessage = extractApiErrorMessage(error, 'Não foi possível carregar o formulário da vaga.');
        this.addQuestion('TEXT');
      }
    });
  }

  get questions(): FormArray {
    return this.form.get('questions') as FormArray;
  }

  get questionGroups(): FormGroup[] {
    return this.questions.controls as FormGroup[];
  }

  addQuestion(type: QuestionType = 'TEXT'): void {
    this.questions.push(this.fb.group({
      prompt: ['', [Validators.required, Validators.maxLength(500)]],
      type: [type, [Validators.required]],
      required: [false],
      optionsText: ['']
    }));
  }

  removeQuestion(index: number): void {
    this.questions.removeAt(index);
  }

  moveQuestion(index: number, delta: number): void {
    const target = index + delta;
    if (target < 0 || target >= this.questions.length) {
      return;
    }
    const control = this.questions.at(index);
    this.questions.removeAt(index);
    this.questions.insert(target, control);
  }

  isChoiceType(type: QuestionType): boolean {
    return type === 'SINGLE_CHOICE' || type === 'MULTIPLE_CHOICE';
  }

  typeOf(index: number): QuestionType {
    return this.questionGroups[index]?.get('type')?.value as QuestionType;
  }

  close(): void {
    if (this.isSaving) {
      return;
    }
    this.closed.emit();
  }

  save(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    if (this.questions.length === 0) {
      this.errorMessage = 'Adicione pelo menos uma pergunta ao formulário.';
      return;
    }

    for (const question of this.questionGroups) {
      const type = question.get('type')?.value as QuestionType;
      if (this.isChoiceType(type)) {
        const options = this.parseOptions(question.get('optionsText')?.value);
        if (options.length < 2) {
          this.errorMessage = 'Perguntas de escolha precisam de pelo menos duas opções.';
          return;
        }
      }
    }

    const payload: ApplicationFormModel = {
      jobPositionId: this.jobId,
      title: this.form.get('title')?.value ?? '',
      description: this.form.get('description')?.value ?? '',
      active: this.form.get('active')?.value ?? true,
      questions: this.questionGroups.map((question, index) => {
        const type = question.get('type')?.value as QuestionType;
        return {
          prompt: question.get('prompt')?.value ?? '',
          type,
          required: question.get('required')?.value ?? false,
          options: this.isChoiceType(type) ? this.parseOptions(question.get('optionsText')?.value) : [],
          position: index
        };
      })
    };

    this.isSaving = true;
    this.errorMessage = '';
    this.formService.save(this.jobId, payload).subscribe({
      next: (saved) => {
        this.isSaving = false;
        this.successMessage = 'Formulário guardado com sucesso.';
        this.saved.emit(saved);
      },
      error: (error) => {
        this.isSaving = false;
        this.errorMessage = extractApiErrorMessage(error, 'Não foi possível guardar o formulário.');
      }
    });
  }

  private populate(existing: ApplicationFormModel): void {
    this.form.patchValue({
      title: existing.title,
      description: existing.description ?? '',
      active: existing.active
    });
    this.questions.clear();
    const questions = [...(existing.questions ?? [])]
      .sort((a, b) => (a.position ?? 0) - (b.position ?? 0));
    if (questions.length === 0) {
      this.addQuestion('TEXT');
      return;
    }
    for (const question of questions) {
      this.questions.push(this.fb.group({
        prompt: [question.prompt, [Validators.required, Validators.maxLength(500)]],
        type: [question.type, [Validators.required]],
        required: [question.required],
        optionsText: [(question.options ?? []).join(', ')]
      }));
    }
  }

  private parseOptions(value: string | null | undefined): string[] {
    if (!value) {
      return [];
    }
    return value.split(',').map(option => option.trim()).filter(option => option.length > 0);
  }
}
