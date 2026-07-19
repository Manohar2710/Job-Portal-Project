import { HttpErrorResponse, HttpHandlerFn, HttpInterceptorFn, HttpRequest } from "@angular/common/http";
import { inject } from "@angular/core";
import { AuthService } from "./auth.service";
import { catchError, switchMap, throwError } from "rxjs";


/**
 * Endpoints that never need an Authorization header.
 * Requests to these URLs are passed through untouched.
 */
const PUBLIC_ENDPOINTS = [
  '/api/auth/login',
  '/api/auth/register',
  '/api/auth/refresh',   // carries its own refresh token in the body
  '/api/auth/logout',    // ditto
];

function isPublicEndpoint(reg: HttpRequest<unknown>): boolean {
    return PUBLIC_ENDPOINTS.some((path) => reg.url.includes(path));
}

function attachToken(req: HttpRequest<unknown>, token: string): HttpRequest<unknown> {
    console.log("authInterceptor 6")
  return req.clone({
    setHeaders: { Authorization: `Bearer ${token}` },
  });
}

/**
 * Functional HTTP interceptor — token attachment and 401 retry.
 *
 * Flow for every non-public request:
 *  1. Read access token from AuthService
 *  2. Attach  Authorization: Bearer <token>
 *  3. On 401 → call doRefresh$() → retry original request with new token (switchMap)
 *  4. If retry also 401 → logout() and rethrow
 */

export const authInterceptor: HttpInterceptorFn = (
    req: HttpRequest<unknown>,
    next: HttpHandlerFn
) => {
    console.log("authInterceptor 1")

    const authService = inject(AuthService);

    // return req if no access token required
    if(isPublicEndpoint(req)) {
        return next(req);
    }

    // attach access token to req
    const token = authService.getAccessToken();
    const authReq = token ? attachToken(req, token): req;

    return next(authReq).pipe(
        catchError((err: unknown) => {
                console.log("authInterceptor 2")

            if(!(err instanceof HttpErrorResponse) || err.status !== 401) {
                console.log("authInterceptor 3")

                console.log("dsdddsdssd",err)
                return throwError(() => err);
            }
            console.log("authInterceptor 5")
            // ── 4. Silent refresh then retry ─────────────────────────────────────
            return authService.doRefresh$().pipe(
                switchMap((newToken: string) =>
                    next(attachToken(req, newToken))      // retry with fresh token
                ),
                catchError((refreshErr: unknown) => {
                    console.log("authInterceptor 5")
                    authService.logout();                 // refresh failed → force logout
                    return throwError(() => refreshErr);
                })
            );
        })
    )
}