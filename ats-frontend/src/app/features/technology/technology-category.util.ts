import { TechnologyModel } from './models/technology.model';

export interface TechnologyCategoryOption {
  value: string;
  label: string;
}

export interface TechnologyGroup {
  category: string;
  label: string;
  technologies: TechnologyModel[];
}

export const TECHNOLOGY_CATEGORIES: TechnologyCategoryOption[] = [
  { value: 'LANGUAGE', label: 'Linguagens de programação' },
  { value: 'FRAMEWORK', label: 'Frameworks' },
  { value: 'DATABASE', label: 'Bancos de dados' },
  { value: 'DEVOPS_INFRA', label: 'DevOps e infraestrutura' },
  { value: 'CLOUD_PROVIDER', label: 'Cloud' },
  { value: 'AI_LLM', label: 'IA e LLMs' },
  { value: 'TESTING_TOOL', label: 'Ferramentas de teste' },
  { value: 'ARCHITECTURE_STYLE', label: 'Arquitetura' },
  { value: 'STORAGE', label: 'Armazenamento' },
  { value: 'SERVERLESS', label: 'Serverless' },
  { value: 'VERSION_CONTROL', label: 'Versionamento' },
  { value: 'OTHER', label: 'Outras' }
];

export const WORK_MODELS: TechnologyCategoryOption[] = [
  { value: 'REMOTE', label: 'Remoto' },
  { value: 'HYBRID', label: 'Híbrido' },
  { value: 'ON_SITE', label: 'Presencial' }
];

export const EDUCATION_LEVELS: TechnologyCategoryOption[] = [
  { value: 'HIGH_SCHOOL', label: 'Ensino Médio' },
  { value: 'ASSOCIATE', label: 'Tecnólogo / Superior Curto' },
  { value: 'BACHELORS', label: 'Superior Completo' },
  { value: 'SPECIALIZATION', label: 'Especialização / Pós-graduação' },
  { value: 'MASTERS', label: 'Mestrado' },
  { value: 'DOCTORATE', label: 'Doutorado' }
];

export const ENGLISH_LEVELS: TechnologyCategoryOption[] = [
  { value: 'NONE', label: 'Não tenho inglês' },
  { value: 'BASIC', label: 'Básico' },
  { value: 'INTERMEDIATE', label: 'Intermédio' },
  { value: 'ADVANCED', label: 'Avançado' },
  { value: 'FLUENT', label: 'Fluente' }
];

export function technologyCategoryLabel(category?: string): string {
  return TECHNOLOGY_CATEGORIES.find(item => item.value === category)?.label ?? category ?? '';
}

export function workModelLabel(model?: string): string {
  return WORK_MODELS.find(item => item.value === model)?.label ?? model ?? '';
}

export function educationLevelLabel(level?: string): string {
  return EDUCATION_LEVELS.find(item => item.value === level)?.label ?? level ?? '';
}

export function englishLevelLabel(level?: string): string {
  return ENGLISH_LEVELS.find(item => item.value === level)?.label ?? level ?? '';
}

function questionOptions(questionKey?: string | null): TechnologyCategoryOption[] | null {
  switch (questionKey) {
    case 'WORK_MODEL':
      return WORK_MODELS;
    case 'EDUCATION_LEVEL':
      return EDUCATION_LEVELS;
    case 'ENGLISH_LEVEL':
      return ENGLISH_LEVELS;
    default:
      return null;
  }
}

export function formOptionLabel(questionKey: string | null | undefined, option: string): string {
  const options = questionOptions(questionKey);
  return options?.find(item => item.value === option)?.label ?? option;
}

export function formOptionValue(questionKey: string | null | undefined, value: string): string {
  const options = questionOptions(questionKey);
  const normalized = value.trim().toLowerCase();
  return options?.find(item =>
    item.value.toLowerCase() === normalized || item.label.toLowerCase() === normalized
  )?.value ?? value;
}

export function formOptionsText(questionKey: string | null | undefined, options?: string[] | null): string {
  return (options ?? []).map(option => formOptionLabel(questionKey, option)).join(', ');
}

export function formOptionsFromText(questionKey: string | null | undefined, value?: string | null): string[] {
  if (!value) {
    return [];
  }
  return value
    .split(',')
    .map(option => option.trim())
    .filter(option => option.length > 0)
    .map(option => formOptionValue(questionKey, option));
}

function questionKeyFromPrompt(prompt?: string | null): string | null {
  const normalized = prompt?.trim().toLowerCase();
  if (!normalized) {
    return null;
  }
  if (normalized.startsWith('modelo de trabalho')) {
    return 'WORK_MODEL';
  }
  if (normalized === 'formação acadêmica') {
    return 'EDUCATION_LEVEL';
  }
  if (normalized === 'nível de inglês') {
    return 'ENGLISH_LEVEL';
  }
  return null;
}

export function formAnswerLabel(
  questionKey: string | null | undefined,
  answer: string,
  questionPrompt?: string | null
): string {
  const resolvedKey = questionOptions(questionKey) ? questionKey : questionKeyFromPrompt(questionPrompt);
  const options = questionOptions(resolvedKey);
  if (!options) {
    return answer;
  }
  return answer
    .split(';')
    .map(selection => selection.trim())
    .filter(selection => selection.length > 0)
    .map(selection => formOptionLabel(resolvedKey, selection))
    .join('; ');
}

export function groupTechnologies(technologies: TechnologyModel[]): TechnologyGroup[] {
  return TECHNOLOGY_CATEGORIES
    .map(category => ({
      category: category.value,
      label: category.label,
      technologies: technologies
        .filter(technology => technology.category === category.value)
        .sort((a, b) => a.name.localeCompare(b.name))
    }))
    .filter(group => group.technologies.length > 0);
}
