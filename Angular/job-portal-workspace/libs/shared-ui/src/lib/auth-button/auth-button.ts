import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatButtonModule } from '@angular/material/button';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';

@Component({
  selector: 'ui-auth-button',
  imports: [CommonModule, MatButtonModule, MatProgressSpinnerModule],
  templateUrl: './auth-button.html',
  styleUrl: './auth-button.css',
})
export class AuthButton {
  @Input() label: string = 'Submit';
  @Input() loading: boolean = false;
  @Input() disabled: boolean = false;

  get isDisabled(): boolean {
    return this.disabled || this.loading;
  }
}
