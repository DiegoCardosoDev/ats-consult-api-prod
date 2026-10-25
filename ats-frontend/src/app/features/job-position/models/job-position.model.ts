import { TechnologyModel } from '../../technology/models/technology.model';
import { JobStatus, WorkModel } from './job-position-enums.model';

export interface LocationModel {
  city: string;
  state: string;
  country: string;
}

export interface JobClientModel {
  id: number;
  name: string;
  code: string;
}

export interface JobPosition {
  id?: number;
  client?: JobClientModel;
  title: string;
  description: string;
  status: JobStatus;
  workModel: WorkModel;
  contractType: string;
  educationLevel?: string;
  englishLevel?: string;
  minSalary?: number;
  maxSalary?: number;
  location: LocationModel;
  technologyIds?: number[];
  technologies?: TechnologyModel[];
}