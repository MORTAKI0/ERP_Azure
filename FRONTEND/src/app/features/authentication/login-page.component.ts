import { CommonModule } from '@angular/common';
import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { map } from 'rxjs/operators';
import { ApiFieldError, AppApiError } from '../../core/auth/auth.models';
import { AuthService } from '../../core/auth/auth.service';
import { AuthStore } from '../../core/auth/auth-store';

@Component({
  selector: 'app-login-page',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './login-page.component.html',
  styleUrl: './login-page.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class LoginPageComponent {
  private readonly fb = inject(FormBuilder);
  private readonly authService = inject(AuthService);
  private readonly authStore = inject(AuthStore);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);

  readonly loginForm = this.fb.nonNullable.group({
    email: ['', [Validators.required, Validators.email]],
    password: ['', [Validators.required]]
  });

  readonly loading = this.authStore.loading;
  readonly sessionExpired = toSignal(
    this.route.queryParamMap.pipe(map((params) => params.get('reason') === 'session_expired')),
    { initialValue: false }
  );

  readonly formError = signal<string | null>(null);
  readonly correlationId = signal<string | null>(null);

  submit(): void {
    this.formError.set(null);
    this.correlationId.set(null);
    this.clearBackendErrors();

    if (this.loginForm.invalid) {
      this.loginForm.markAllAsTouched();
      return;
    }

    const { email, password } = this.loginForm.getRawValue();
    this.authService.login(email, password).subscribe({
      next: () => {
        void this.router.navigateByUrl(this.route.snapshot.queryParamMap.get('returnUrl') || '/dashboard');
      },
      error: (error: unknown) => this.handleSubmitError(error as AppApiError)
    });
  }

  private clearBackendErrors(): void {
    Object.values(this.loginForm.controls).forEach((control) => {
      if (!control.errors?.['backend']) {
        return;
      }

      const { backend, ...remainingErrors } = control.errors;
      control.setErrors(Object.keys(remainingErrors).length ? remainingErrors : null);
    });
  }

  private handleSubmitError(error: AppApiError): void {
    if (error.kind === 'network') {
      this.formError.set(error.message);
      return;
    }

    const envelope = error.envelope;
    if (!envelope) {
      this.formError.set(error.message);
      return;
    }

    this.correlationId.set(envelope.correlationId ?? null);

    if (envelope.error === 'invalid_credentials') {
      this.formError.set(envelope.message || 'Invalid email or password.');
      return;
    }

    if (envelope.error === 'validation_failed' && envelope.fieldErrors?.length) {
      this.applyFieldErrors(envelope.fieldErrors);
      this.formError.set(envelope.message || 'Please correct the highlighted fields.');
      return;
    }

    this.formError.set(envelope.message || 'Unable to login right now.');
  }

  private applyFieldErrors(fieldErrors: ApiFieldError[]): void {
    fieldErrors.forEach((fieldError) => {
      const control = this.loginForm.get(fieldError.field);
      if (!control) {
        return;
      }

      const currentErrors = control.errors ?? {};
      control.setErrors({ ...currentErrors, backend: fieldError.message });
      control.markAsTouched();
    });
  }
}
