import { HttpErrorResponse } from '@angular/common/http';
import { ApiError } from '../models/api-error.model';

export function extractApiErrorMessage(error: unknown, fallback: string): string {
  if (error instanceof HttpErrorResponse) {
    const body = error.error as ApiError | null;
    if (body && typeof body.message === 'string' && body.message.trim()) {
      return body.message;
    }

    if (error.status === 0) {
      return 'Não foi possível contactar a API. Verifique a ligação.';
    }
    if (error.status === 401) {
      return 'Sessão inválida ou expirada. Faça login novamente.';
    }
    if (error.status === 403) {
      return 'Não tem permissão para executar esta operação.';
    }
    if (error.status === 429) {
      return 'Demasiadas tentativas. Aguarde alguns instantes.';
    }
  }

  return fallback;
}
