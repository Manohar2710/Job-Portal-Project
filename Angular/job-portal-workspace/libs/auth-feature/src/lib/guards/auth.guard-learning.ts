import { CanActivateFn, Router } from "@angular/router";
import { inject } from "@angular/core";
import { AuthService } from "../auth.service";


export const authGuardLearning: CanActivateFn = (routes, state) => {
    const authService = inject(AuthService);
    const router = inject(Router);

    // redirect to requested path by returning true
    if(authService.isAuthenticated()) {
        return true;
    }

    // redirect unathenticated user
    return router.parseUrl('/login')
}