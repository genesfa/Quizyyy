import { Injectable } from '@angular/core';
import { CanActivate, Router, ActivatedRouteSnapshot } from '@angular/router';

@Injectable({
    providedIn: 'root'
})
export class PasswordGuard implements CanActivate {
    constructor(private router: Router) {}

    canActivate(route: ActivatedRouteSnapshot): boolean {
        const password = route.queryParamMap.get('pw');
        if (password === 'Tucanon13') {
            return true;
        }
        this.router.navigate(['/']); // Redirect to home or another route if access is denied
        return false;
    }
}