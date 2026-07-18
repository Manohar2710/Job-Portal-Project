import { inject, Injectable, OnDestroy } from '@angular/core';
import { Router } from '@angular/router';
import { Observable, tap } from 'rxjs';

import {
  AuthenticationService,
  AuthReponse,
  LoginRequest,
} from 'job-portal-api';

// ─── Storage key constants ────────────────────────────────────────────────────
// Centralised here so every read/write uses the same literal string.
const KEY_ACCESS_TOKEN  = 'jp_access_token';
const KEY_REFRESH_TOKEN = 'jp_refresh_token';
const KEY_TOKEN_EXPIRY  = 'jp_token_expiry';   // Unix ms timestamp

// Fire refresh 60 seconds before the access token actually expires
const REFRESH_BUFFER_MS = 60_000;

@Injectable({ providedIn: 'root' })
export class AuthService implements OnDestroy {

  private readonly api    = inject(AuthenticationService);
  private readonly router = inject(Router);

  /** Handle returned by setTimeout — kept so we can cancel it on logout */
  private refreshTimerId: ReturnType<typeof setTimeout> | null = null;

  // ─── Public API ─────────────────────────────────────────────────────────────

  /**
   * Calls POST /api/auth/login.
   * On success: persists both tokens + expiry, then schedules a silent refresh.
   */
  login(req: LoginRequest): Observable<AuthReponse> {
    return this.api.login(req).pipe(
      tap((res) => this.handleAuthResponse(res))
    );
  }

  /**
   * Clears all stored tokens, cancels the refresh timer, and redirects to /login.
   * Also calls POST /api/auth/logout to invalidate the refresh token server-side
   * (fire-and-forget — we never wait for the response before redirecting).
   */
  logout(): void {
    const refreshToken = this.getRefreshToken();

    // Revoke server-side — fire and forget; errors are silently ignored
    // because we always clear the client state regardless of server outcome.
    if (refreshToken) {
      this.api.logout({ refreshToken }).subscribe({ error: () => {} });
    }

    this.clearSession();
    this.router.navigate(['/login']);
  }

  /**
   * Returns true when a non-expired access token is present in localStorage.
   * Does NOT make a network request — purely a synchronous timestamp check.
   */
  isAuthenticated(): boolean {
    const expiry = localStorage.getItem(KEY_TOKEN_EXPIRY);
    if (!expiry) return false;
    return Date.now() < Number(expiry);
  }

  /**
   * Returns the raw JWT string from localStorage, or null if absent.
   * Consumers should treat this as opaque — never decode it in the client.
   */
  getAccessToken(): string | null {
    return localStorage.getItem(KEY_ACCESS_TOKEN);
  }

  // ─── Token refresh ──────────────────────────────────────────────────────────

  /**
   * Schedules a silent token refresh to fire 60 s before the access token expires.
   * Any existing timer is cancelled first to prevent double-scheduling.
   *
   * @param expiresIn  milliseconds until the access token expires (from the server response)
   */
  scheduleTokenRefresh(expiresIn: number): void {
    this.cancelRefreshTimer();

    const delay = expiresIn - REFRESH_BUFFER_MS;
    if (delay <= 0) {
      // Token is already about to expire — refresh immediately
      this.doRefresh();
      return;
    }

    this.refreshTimerId = setTimeout(() => this.doRefresh(), delay);
  }

  /**
   * Attempts a silent token refresh using the stored refresh token.
   * On success: stores the new token pair and re-schedules the next refresh.
   * On failure (expired / revoked): falls back to logout().
   */
  doRefresh(): void {
    const refreshToken = this.getRefreshToken();

    if (!refreshToken) {
      this.logout();
      return;
    }

    this.api.refresh({ refreshToken }).subscribe({
      next:  (res) => this.handleAuthResponse(res),
      error: ()    => this.logout(),
    });
  }

  // ─── Lifecycle ──────────────────────────────────────────────────────────────

  ngOnDestroy(): void {
    this.cancelRefreshTimer();
  }

  // ─── Private helpers ────────────────────────────────────────────────────────

  /**
   * Persists the token pair returned by login or refresh, then schedules
   * the next silent refresh based on the server-reported expiry.
   */
  private handleAuthResponse(res: AuthReponse): void {
    if (res.accessToken)  localStorage.setItem(KEY_ACCESS_TOKEN,  res.accessToken);
    if (res.refreshToken) localStorage.setItem(KEY_REFRESH_TOKEN, res.refreshToken);

    // expiresIn is in milliseconds (matches security.jwt.expiration in application.yaml)
    if (res.expiresIn) {
      const expiryMs = Date.now() + res.expiresIn;
      localStorage.setItem(KEY_TOKEN_EXPIRY, String(expiryMs));
      this.scheduleTokenRefresh(res.expiresIn);
    }
  }

  /** Reads the opaque refresh token from localStorage */
  private getRefreshToken(): string | null {
    return localStorage.getItem(KEY_REFRESH_TOKEN);
  }

  /** Removes all three token keys from localStorage */
  private clearSession(): void {
    localStorage.removeItem(KEY_ACCESS_TOKEN);
    localStorage.removeItem(KEY_REFRESH_TOKEN);
    localStorage.removeItem(KEY_TOKEN_EXPIRY);
    this.cancelRefreshTimer();
  }

  /** Cancels a pending refresh timer if one exists */
  private cancelRefreshTimer(): void {
    if (this.refreshTimerId !== null) {
      clearTimeout(this.refreshTimerId);
      this.refreshTimerId = null;
    }
  }
}
