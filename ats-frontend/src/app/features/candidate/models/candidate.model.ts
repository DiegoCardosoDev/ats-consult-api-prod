export interface CandidateAddressModel {
  street?: string;
  number?: string;
  complement?: string;
  neighborhood?: string;
  city?: string;
  state?: string;
  zipCode?: string;
  country?: string;
}

import { TechnologyModel } from '../../technology/models/technology.model';

export interface CandidateModel {
  id?: number;
  name: string;
  age: number;
  role: string;
  linkedinUrl?: string;
  email: string;
  phone: string;
  educationLevel?: string;
  englishLevel?: string;
  technologyIds?: number[];
  technologies?: TechnologyModel[];
  address?: CandidateAddressModel;
}
