import { Component, EventEmitter, HostBinding, HostListener, inject, Input, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatButtonModule } from '@angular/material/button';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { Theme } from '../services/theme';

@Component({
  selector: 'ui-auth-button',
  imports: [CommonModule, MatButtonModule, MatProgressSpinnerModule],
  templateUrl: './auth-button.html',
  styleUrl: './auth-button.css',
  host: {
    'role': 'button',
    '[class.is-active]': 'active',
    '[class.is-disabled]': 'disabled',
    '(click)': 'onClick()',
    '(mouseenter)': 'onMouseEnter()'
  },
  providers: [Theme]
})
export class AuthButton {

  // field Injector
  private theme = inject(Theme);

  @Input() label: string = 'Submit';
  @Input() loading: boolean = false;
  @Input() disabled: boolean = false;
  @Input() isSubmit: boolean = false;
  @Output() onButtonClick = new EventEmitter();
  active: boolean = false;

  ngOninit() {
    this.theme.color = "Dark Mode"; // color update will not effect other screens
  }
  get isDisabled(): boolean {
    return this.disabled || this.loading;
  }

  onClick() {
    console.log("Button Clicked ");
    this.onButtonClick.emit();
  }

  onMouseEnter() {
    console.log("On Mouse Enter");
  }

  // Legacy hostbinding 
  @HostBinding('class.is-active') isActive = false

  // legacy hostlistner
  // @HostListener('click', ['$event']) 
  // handleClick(event: MouseEvent) {
  //   console.log("legacy mouse event clicked")
  // }

}
