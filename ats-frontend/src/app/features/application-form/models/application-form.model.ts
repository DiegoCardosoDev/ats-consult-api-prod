export type QuestionType =
  | 'TEXT'
  | 'TEXTAREA'
  | 'YES_NO'
  | 'NUMBER'
  | 'SINGLE_CHOICE'
  | 'MULTIPLE_CHOICE';

export interface ApplicationFormQuestionModel {
  id?: number;
  prompt: string;
  type: QuestionType;
  required: boolean;
  options: string[];
  position?: number;
}

export interface ApplicationFormModel {
  id?: number;
  jobPositionId: number;
  title: string;
  description?: string;
  active: boolean;
  questions: ApplicationFormQuestionModel[];
}

export interface FormAnswerModel {
  questionId: number;
  questionPrompt?: string;
  answer: string;
  position?: number;
}

export interface FormAnswerPayload {
  questionId: number;
  answer: string;
}
