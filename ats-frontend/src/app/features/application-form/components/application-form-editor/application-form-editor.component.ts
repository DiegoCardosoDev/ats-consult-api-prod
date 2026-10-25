import { Component, EventEmitter, Input, OnInit, Output, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormArray, FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ApplicationFormModel, QuestionType } from '../../models/application-form.model';
import { ApplicationFormService } from '../../services/application-form.service';
import { extractApiErrorMessage } from '../../../../core/utils/api-error.util';
import { TechnologyService } from '../../../technology/services/technology.service';
import { TechnologyModel } from '../../../technology/models/technology.model';
import {
  formOptionsFromText,
  formOptionsText,
  technologyCategoryLabel
} from '../../../technology/technology-category.util';

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
  private technologyService = inject(TechnologyService);

  isLoading = false;
  isSaving = false;
  isRegenerating = false;
  errorMessage = '';
  successMessage = '';
  technologies: TechnologyModel[] = [];

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
    this.loadTechnologies();
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

  loadTechnologies(): void {
    this.technologyService.findAll().subscribe({
      next: (technologies) => this.technologies = technologies,
      error: () => this.technologies = []
    });
  }

  addQuestion(type: QuestionType = 'TEXT'): void {
    this.questions.push(this.fb.group({
      prompt: ['', [Validators.required, Validators.maxLength(500)]],
      type: [type, [Validators.required]],
      required: [false],
      optionsText: [''],
      questionKey: [null],
      technologyCategory: [null],
      parentTechnologyId: [null],
      maxSelections: [null],
      systemGenerated: [false]
    }));
  }

  removeQuestion(index: number): void {
    const questionKey = this.questions.at(index)?.get('questionKey')?.value as string | null;
    if (this.isProtectedQuestion(questionKey)) {
      return;
    }
    this.questions.removeAt(index);
  }

  isCoreQuestion(questionKey?: string | null): boolean {
    return !!questionKey && ['FULL_NAME', 'AGE', 'ROLE', 'EMAIL', 'PHONE'].includes(questionKey);
  }

  isTechnologyQuestion(questionKey?: string | null): boolean {
    return !!questionKey
      && (questionKey.startsWith('TECH_CATEGORY_') || questionKey.startsWith('TECH_CHILDREN_'));
  }

  isProtectedQuestion(questionKey?: string | null): boolean {
    return this.isCoreQuestion(questionKey) || this.isTechnologyQuestion(questionKey);
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

  isMultipleChoice(type: QuestionType): boolean {
    return type === 'MULTIPLE_CHOICE';
  }

  categoryLabel(category?: string): string {
    return technologyCategoryLabel(category);
  }

  parentTechnologyName(parentId?: number): string {
    if (parentId == null) {
      return '';
    }
    return this.technologies.find(technology => technology.id === parentId)?.name ?? `#${parentId}`;
  }

  regenerate(): void {
    if (!confirm('Regenerar o formulário com o modelo padrão? As perguntas atuais serão substituídas.')) {
      return;
    }
    this.isRegenerating = true;
    this.errorMessage = '';
    this.formService.generate(this.jobId).subscribe({
      next: (generated) => {
        this.isRegenerating = false;
        this.populate(generated);
        this.successMessage = 'Formulário regenerado com o modelo padrão.';
      },
      error: (error) => {
        this.isRegenerating = false;
        this.errorMessage = extractApiErrorMessage(error, 'Não foi possível regenerar o formulário.');
      }
    });
  }

  typeOf(index: number): QuestionType {
    return this.questionGroups[index]?.get('type')?.value as QuestionType;
  }

  typeLabel(index: number): string {
    const type = this.typeOf(index);
    return this.questionTypes.find(option => option.value === type)?.label ?? type;
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
        const questionKey = question.get('questionKey')?.value as string | null | undefined;
        const options = formOptionsFromText(questionKey, question.get('optionsText')?.value);
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
        const questionKey = question.get('questionKey')?.value as string | null | undefined;
        return {
          prompt: question.get('prompt')?.value ?? '',
          type,
          required: question.get('required')?.value ?? false,
          options: this.isChoiceType(type)
            ? formOptionsFromText(questionKey, question.get('optionsText')?.value)
            : [],
          position: index,
          questionKey: question.get('questionKey')?.value || undefined,
          technologyCategory: question.get('technologyCategory')?.value || undefined,
          parentTechnologyId: question.get('parentTechnologyId')?.value ?? undefined,
          maxSelections: this.isMultipleChoice(type)
            ? question.get('maxSelections')?.value ?? undefined
            : undefined,
          systemGenerated: question.get('systemGenerated')?.value ?? false
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
        optionsText: [formOptionsText(question.questionKey, question.options)],
        questionKey: [question.questionKey ?? null],
        technologyCategory: [question.technologyCategory ?? null],
        parentTechnologyId: [question.parentTechnologyId ?? null],
        maxSelections: [question.maxSelections ?? null],
        systemGenerated: [question.systemGenerated ?? false]
      }));
    }
  }

}
