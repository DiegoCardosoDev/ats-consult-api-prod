import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { map } from 'rxjs';
import { AuthService } from '../../services/auth.service';

export const roleGuard: CanActivateFn = (route, state) => {
  const authService = inject(AuthService);
  const router = inject(Router);
  const allowedRoles = (route.data?.['roles'] as string[] | undefined) ?? [];

  return authService.ensureSession().pipe(
    map(authenticated => {
      if (!authenticated) {
        return router.createUrlTree(['/login'], { queryParams: { returnUrl: state.url } });
      }

      const role = authService.currentUser()?.role;
      if (allowedRoles.length > 0 && (!role || !allowedRoles.includes(role))) {
        return router.createUrlTree(['/home']);
      }

      return true;
    })
  );
};
