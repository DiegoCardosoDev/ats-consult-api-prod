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

export interface CandidateModel {
  id?: number;
  name: string;
  age: number;
  role: string;
  linkedinUrl?: string;
  email: string;
  phone: string;
  address?: CandidateAddressModel;
}
