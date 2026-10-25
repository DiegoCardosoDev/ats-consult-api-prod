import {
  Component,
  ElementRef,
  EventEmitter,
  Input,
  OnChanges,
  OnInit,
  Output,
  SimpleChanges,
  ViewChild,
  inject
} from '@angular/core';
import { CommonModule } from '@angular/common';
import { AbstractControl, FormBuilder, ReactiveFormsModule, ValidationErrors, Validators } from '@angular/forms';
import { CandidateModel } from '../../../candidate/models/candidate.model';
import { ApplyCandidateResponse } from '../../models/selection-process.model';
import { SelectionProcessService } from '../../services/selection-process.service';
import { extractApiErrorMessage } from '../../../../core/utils/api-error.util';
import {
  PRIVACY_POLICY_CONTACT,
  PRIVACY_POLICY_VERSION
} from '../../../../core/constants/privacy-policy.constants';
import {
  ApplicationFormModel,
  ApplicationFormQuestionModel,
  FormAnswerPayload,
  QuestionType
} from '../../../application-form/models/application-form.model';
import {
  formOptionLabel,
  technologyCategoryLabel
} from '../../../technology/technology-category.util';

const REQUIRED_CORE_KEYS = ['FULL_NAME', 'AGE', 'ROLE', 'EMAIL', 'PHONE'];

@Component({
  selector: 'app-candidate-application-modal',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './candidate-application-modal.component.html',
  styleUrl: './candidate-application-modal.component.scss'
})
export class CandidateApplicationModalComponent implements OnInit, OnChanges {
  @Input({ required: true }) mode!: 'job' | 'process';
  @Input({ required: true }) targetId!: number;
  @Input() applicationForm?: ApplicationFormModel;

  @Output() closed = new EventEmitter<void>();
  @Output() applied = new EventEmitter<ApplyCandidateResponse>();

  @ViewChild('fileInput') fileInput?: ElementRef<HTMLInputElement>;

  private processService = inject(SelectionProcessService);
  private fb = inject(FormBuilder);

  readonly privacyPolicyVersion = PRIVACY_POLICY_VERSION;
  readonly privacyPolicyContact = PRIVACY_POLICY_CONTACT;
  privacyConsentControl = this.fb.control(false, Validators.requiredTrue);
  isSubmitting = false;
  isSuccess = false;
  errorMessage = '';
  selectedResume: File | null = null;
  successResult?: ApplyCandidateResponse;

  answersForm = this.fb.group({});

  ngOnInit(): void {
    if (this.hasQuestions) {
      this.buildAnswersForm();
    }
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (!changes['applicationForm'] || changes['applicationForm'].firstChange) {
      return;
    }
    this.rebuildAnswersForm();
  }

  get questions(): ApplicationFormQuestionModel[] {
    const all = [...(this.applicationForm?.questions ?? [])];
    const standard = all
      .filter(question => !question.technologyCategory && question.parentTechnologyId == null)
      .sort((a, b) => (a.position ?? 0) - (b.position ?? 0));
    const technology = all
      .filter(question => question.technologyCategory || question.parentTechnologyId != null)
      .sort((a, b) => {
        const categoryCompare = (a.technologyCategory ?? '').localeCompare(b.technologyCategory ?? '');
        if (categoryCompare !== 0) {
          return categoryCompare;
        }
        const parentCompare = (a.parentTechnologyId == null ? 0 : 1) - (b.parentTechnologyId == null ? 0 : 1);
        if (parentCompare !== 0) {
          return parentCompare;
        }
        return (a.position ?? 0) - (b.position ?? 0);
      });
    return [...standard, ...technology];
  }

  isFirstOfCategory(index: number): boolean {
    if (index === 0) {
      return !!this.questions[index].technologyCategory;
    }
    return this.questions[index].technologyCategory !== this.questions[index - 1].technologyCategory;
  }

  categoryLabel(category?: string): string {
    return technologyCategoryLabel(category);
  }

  optionLabel(question: ApplicationFormQuestionModel, option: string): string {
    return formOptionLabel(question.questionKey, option);
  }

  isOptionSelected(question: ApplicationFormQuestionModel, option: string): boolean {
    const value = this.answerControl(question)?.value;
    return Array.isArray(value) && value.includes(option);
  }

  toggleOption(question: ApplicationFormQuestionModel, option: string, event: Event): void {
    const input = event.target as HTMLInputElement;
    const control = this.answerControl(question);
    if (!control) {
      return;
    }
    let current: string[] = Array.isArray(control.value) ? [...control.value] : [];
    const noTechnologyOption = 'Não domino';
    if (input.checked) {
      if (option === noTechnologyOption) {
        current = [option];
      } else {
        current = current.filter(value => value !== noTechnologyOption);
        if (!current.includes(option)) {
          if (question.maxSelections != null && current.length >= question.maxSelections) {
            input.checked = false;
            this.errorMessage = `A pergunta '${question.prompt}' permite no máximo ${question.maxSelections} seleções.`;
            return;
          }
          current.push(option);
        }
      }
    } else {
      current = current.filter(value => value !== option);
    }
    control.setValue(current);
    control.markAsTouched();
    control.updateValueAndValidity();
    this.errorMessage = '';
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

  questionError(question: ApplicationFormQuestionModel): string {
    switch (question.questionKey) {
      case 'EMAIL':
        return 'Informe um e-mail válido.';
      case 'AGE':
        return 'Informe uma idade entre 16 e 120.';
      case 'PHONE':
        return 'Informe um telefone válido.';
      case 'LINKEDIN_URL':
        return 'Informe um URL válido.';
      default:
        return this.isChoiceType(question.type)
          ? 'Selecione pelo menos uma opção.'
          : 'Esta pergunta é obrigatória.';
    }
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

  onPrivacyConsentChange(event: Event): void {
    if ((event.target as HTMLInputElement).checked) {
      this.errorMessage = '';
    }
  }

  submit(): void {
    this.errorMessage = '';

    if (!this.hasQuestions) {
      this.errorMessage = 'O formulário de candidatura desta vaga não está disponível.';
      return;
    }
    if (!this.hasCoreQuestions) {
      this.errorMessage = 'O formulário da vaga está incompleto. Contacte o recrutador.';
      return;
    }
    if (this.answersForm.invalid) {
      this.answersForm.markAllAsTouched();
      this.errorMessage = 'Responda às perguntas obrigatórias antes de enviar a candidatura.';
      return;
    }
    if (this.privacyConsentControl.invalid) {
      this.privacyConsentControl.markAsTouched();
      this.errorMessage = 'É obrigatório aceitar a Política de Privacidade para enviar a candidatura.';
      return;
    }

    const resume = this.selectedResume;
    if (!resume) {
      this.errorMessage = 'Selecione o currículo em PDF.';
      return;
    }

    const candidate = this.buildCandidateFromAnswers();
    this.isSubmitting = true;

    const answers = this.collectAnswers();
    const request$ = this.mode === 'job'
      ? this.processService.applyToJob(this.targetId, candidate, resume, answers)
      : this.processService.applyToProcess(this.targetId, candidate, resume, answers);

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

  private get hasCoreQuestions(): boolean {
    return REQUIRED_CORE_KEYS.every(key => this.questions.some(question => question.questionKey === key));
  }

  private answerValue(questionKey: string): string {
    const question = this.questions.find(item => item.questionKey === questionKey);
    if (!question || question.id == null) {
      return '';
    }
    const value = this.answersForm.get(this.answerControlName(question))?.value;
    if (Array.isArray(value)) {
      return value.join('; ').trim();
    }
    return value == null ? '' : String(value).trim();
  }

  private buildCandidateFromAnswers(): CandidateModel {
    const age = Number(this.answerValue('AGE'));
    return {
      name: this.answerValue('FULL_NAME'),
      age: Number.isFinite(age) ? age : 0,
      role: this.answerValue('ROLE'),
      linkedinUrl: this.answerValue('LINKEDIN_URL') || undefined,
      email: this.answerValue('EMAIL'),
      phone: this.answerValue('PHONE'),
      educationLevel: this.answerValue('EDUCATION_LEVEL') || undefined,
      englishLevel: this.answerValue('ENGLISH_LEVEL') || undefined,
      address: {
        street: this.answerValue('STREET') || undefined,
        number: this.answerValue('ADDRESS_NUMBER') || undefined,
        complement: this.answerValue('COMPLEMENT') || undefined,
        neighborhood: this.answerValue('NEIGHBORHOOD') || undefined,
        city: this.answerValue('CITY') || undefined,
        state: this.answerValue('STATE') || undefined,
        zipCode: this.answerValue('ZIP_CODE') || undefined,
        country: this.answerValue('COUNTRY') || undefined
      }
    };
  }

  private rebuildAnswersForm(): void {
    const previousValues: Record<string, unknown> = this.answersForm.getRawValue();
    this.answersForm = this.fb.group({});
    if (!this.hasQuestions) {
      return;
    }
    this.buildAnswersForm();
    for (const question of this.questions) {
      const controlName = this.answerControlName(question);
      if (Object.prototype.hasOwnProperty.call(previousValues, controlName)) {
        this.answersForm.get(controlName)?.setValue(previousValues[controlName]);
      }
    }
  }

  private buildAnswersForm(): void {
    for (const question of this.questions) {
      if (question.id == null) {
        continue;
      }
      const validators = question.required
        ? [question.type === 'MULTIPLE_CHOICE' ? this.multipleChoiceRequired : Validators.required]
        : [];
      switch (question.questionKey) {
        case 'EMAIL':
          validators.push(Validators.email);
          break;
        case 'AGE':
          validators.push(Validators.min(16), Validators.max(120));
          break;
        case 'PHONE':
          validators.push(Validators.pattern(/^[0-9+()\-\s]{8,30}$/));
          break;
        case 'LINKEDIN_URL':
          validators.push(Validators.pattern(/^$|^https?:\/\/.+/));
          break;
      }
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
    this.answersForm.reset();
    this.privacyConsentControl.reset(false);
    this.selectedResume = null;
    this.isSuccess = false;
    this.successResult = undefined;
    if (this.fileInput) {
      this.fileInput.nativeElement.value = '';
    }
  }
}
