export interface ClientModel {
  id?: number;
  name: string;
  cnpj: string;
  code: string;
}

export interface CreateClientPayload {
  name: string;
  cnpj: string;
  code?: string;
}
