import {
  formAnswerLabel,
  formOptionLabel,
  formOptionValue,
  formOptionsFromText,
  formOptionsText,
  workModelLabel
} from './technology-category.util';

describe('technology-category.util', () => {
  it('translates standard form option values', () => {
    expect(workModelLabel('REMOTE')).toBe('Remoto');
    expect(formOptionLabel('WORK_MODEL', 'ON_SITE')).toBe('Presencial');
    expect(formOptionLabel('EDUCATION_LEVEL', 'BACHELORS')).toBe('Superior Completo');
    expect(formOptionLabel('ENGLISH_LEVEL', 'ADVANCED')).toBe('Avançado');
  });

  it('keeps custom and technology options unchanged', () => {
    expect(formOptionLabel('WORK_MODEL', 'Home office')).toBe('Home office');
    expect(formOptionLabel('TECH_CATEGORY_LANGUAGE', 'Java 21')).toBe('Java 21');
  });

  it('converts labels back to technical values when saving', () => {
    expect(formOptionValue('WORK_MODEL', 'Remoto')).toBe('REMOTE');
    expect(formOptionValue('EDUCATION_LEVEL', 'Superior Completo')).toBe('BACHELORS');
    expect(formOptionsFromText('WORK_MODEL', 'Remoto, Presencial')).toEqual(['REMOTE', 'ON_SITE']);
    expect(formOptionsFromText('TECH_CATEGORY_LANGUAGE', 'Java 21, Go')).toEqual(['Java 21', 'Go']);
  });

  it('renders translated option lists and answers', () => {
    expect(formOptionsText('EDUCATION_LEVEL', ['BACHELORS', 'MASTERS']))
      .toBe('Superior Completo, Mestrado');
    expect(formAnswerLabel('WORK_MODEL', 'REMOTE; HYBRID')).toBe('Remoto; Híbrido');
    expect(formAnswerLabel('TECH_CATEGORY_LANGUAGE', 'Java 21; Go')).toBe('Java 21; Go');
    expect(formAnswerLabel(undefined, 'REMOTE; HYBRID',
      'Modelo de trabalho aceito (selecione até 2)')).toBe('Remoto; Híbrido');
  });
});
