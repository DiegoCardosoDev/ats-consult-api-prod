export type UserRole = 'ADMIN' | 'TECH_RECRUITER' | 'USER';

export interface UserModel {
  id: number;
  name: string | null;
  email: string;
  role: UserRole;
  enabled: boolean;
}

export interface CreateUserAdminRequest {
  name: string;
  email: string;
  password: string;
  role: UserRole;
}
